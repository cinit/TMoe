package cc.ioctl.tmoe.hook.core;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.fragment.SettingsFragment;
import cc.ioctl.tmoe.lifecycle.Parasitics;
import cc.ioctl.tmoe.rtti.ProxyFragmentRttiHandler;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class SettingEntryHook implements Initializable {
    public static final SettingEntryHook INSTANCE = new SettingEntryHook();

    private SettingEntryHook() {
    }

    private static final String TMOE_SETTINGS_ROW_ID = "TMOE_SETTINGS_ROW_ID";

    private boolean mInitialized = false;
    private static Field fBaseFragment_arguments = null;
    private static Class<?> kBaseFragment = null;
    private static Class<?> kProfileActivity = null;
    private static Field fProfileActivity_notificationRow = null;
    private static boolean sListViewOnItemClickListenerHooked = false;

    @Override
    public boolean initialize() {
        if (mInitialized) {
            return true;
        }
        kProfileActivity = Initiator.load("org.telegram.ui.ProfileActivity");
        if (kProfileActivity == null) {
            Utils.loge("unable to load ProfileActivity");
            return false;
        }
        kBaseFragment = kProfileActivity.getSuperclass();
        assert kBaseFragment != null;
        try {
            fBaseFragment_arguments = kBaseFragment.getDeclaredField("arguments");
            fBaseFragment_arguments.setAccessible(true);
            fProfileActivity_notificationRow = kProfileActivity.getDeclaredField("notificationRow");
            fProfileActivity_notificationRow.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Utils.loge(e);
            return false;
        }
        try {
            Class<?> kProfileActivity_ListAdapter = kProfileActivity.getDeclaredField("listAdapter").getType();
            Method createView = kProfileActivity.getMethod("createView", Context.class);
            XposedBridge.hookMethod(createView, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!sListViewOnItemClickListenerHooked) {
                        ViewGroup listView = (ViewGroup) Reflex.getInstanceObjectOrNull(param.thisObject, "listView");
                        Object onItemClickListener = Reflex.getInstanceObjectOrNull(listView, "onItemClickListenerExtended");
                        Class<?> kOnItemClickListener = onItemClickListener.getClass();
                        Method onItemClick = null;
                        for (Method method : kOnItemClickListener.getDeclaredMethods()) {
                            if (method.getName().equals("onItemClick")) {
                                onItemClick = method;
                                break;
                            }
                        }
                        if (onItemClick == null) {
                            Utils.loge(new RuntimeException("unable to find onItemClick, class: " + kOnItemClickListener.getName()));
                            return;
                        }
                        XposedBridge.hookMethod(onItemClick, LIST_VIEW_ITEM_CLICK_HOOK);
                        sListViewOnItemClickListenerHooked = true;
                    }
                }
            });
            Method updateRowsIds = kProfileActivity.getDeclaredMethod("updateRowsIds");
            Method ListAdapter_onBindViewHolder = null;
            Method ListAdapter_isEnabled = null;
            for (Method method : kProfileActivity_ListAdapter.getDeclaredMethods()) {
                if (method.getName().equals("onBindViewHolder")) {
                    ListAdapter_onBindViewHolder = method;
                } else if (method.getName().equals("isEnabled")) {
                    ListAdapter_isEnabled = method;
                }
            }
            Method ListAdapter_getItemViewType = kProfileActivity_ListAdapter.getMethod("getItemViewType", int.class);
            XposedBridge.hookMethod(updateRowsIds, SETTINGS_ROW_ID_ALLOCATOR);
            XposedBridge.hookMethod(ListAdapter_isEnabled, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "this$0");
                    if (fragment != null) {
                        int notificationRow = fProfileActivity_notificationRow.getInt(fragment);
                        if (notificationRow != -1) {
                            Object holder = param.args[0];
                            int position = (int) Reflex.invokeVirtual(holder, "getAdapterPosition", int.class);
                            int myRowId = getMyRowCountId(fragment);
                            if (myRowId != -1 && position == myRowId) {
                                param.setResult(true);
                            }
                        }
                    }
                }
            });
            XposedBridge.hookMethod(ListAdapter_getItemViewType, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int position = (int) param.args[0];
                    Object fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "this$0");
                    int myRowId = getMyRowCountId(fragment);
                    if (myRowId != -1 && position == myRowId) {
                        param.setResult(4);
                    }
                }
            });
            XposedBridge.hookMethod(ListAdapter_onBindViewHolder, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int position = (int) param.args[1];
                    Object fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "this$0");
                    int myRowId = getMyRowCountId(fragment);
                    if (myRowId != -1 && position == myRowId) {
                        bindViewHolder(fragment, param.thisObject, param.args[0], (Integer) param.args[1]);
                    }
                }
            });
        } catch (ReflectiveOperationException e) {
            Utils.loge(e);
            return false;
        }
        mInitialized = true;
        return true;
    }

    private static void bindViewHolder(Object fragment, Object adapter, Object holder, int position)
            throws ReflectiveOperationException {
        FrameLayout textCell = (FrameLayout) Reflex.getInstanceObjectOrNull(holder, "itemView");
        if (textCell != null) {
            // color and theme is already set by Telegram, we only need to set the text and icon
            // textCell.setTextAndIcon(text, iconResId, true)
            // inject resources
            Parasitics.injectModuleResources(textCell.getContext().getResources());
            Parasitics.injectModuleResources(HostInfo.getApplication().getResources());
            String text = LocaleController.getString("TMoeSettings", R.string.TMoeSettings);
            int iconResId = R.drawable.ic_setting_hex_outline_24;
            Reflex.invokeVirtual(textCell, "setTextAndIcon", text, iconResId, true,
                    String.class, int.class, boolean.class, void.class);
        } else {
            Utils.loge(new IllegalStateException("textCell is null"));
        }
    }

    private static final XC_MethodHook LIST_VIEW_ITEM_CLICK_HOOK = new XC_MethodHook(49) {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            Object fragment;
            try {
                fragment = Reflex.getFirstByType(param.thisObject, kProfileActivity);
            } catch (NoSuchFieldException e) {
                // strange, but it happens if R8 was asked to repackage the app aggressively
                fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "f$0");
                // check runtime type
                if (fragment != null && !kProfileActivity.isInstance(fragment)) {
                    fragment = null;
                }
                if (fragment == null) {
                    fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "a", Object.class);
                }
                if (fragment == null) {
                    // unable to find the fragment, so we can't do anything
                    Utils.loge(e);
                    return;
                }
            }
            Objects.requireNonNull(fragment, "fragment is unexpectedly null");
            int position = (int) param.args[1];
            int myRowId = getMyRowCountId(fragment);
            if (myRowId != -1 && position == myRowId) {
                presentTMoeSettingsFragment(fragment);
                param.setResult(null);
            }
        }
    };

    private static final XC_MethodHook SETTINGS_ROW_ID_ALLOCATOR = new XC_MethodHook(51) {
        ArrayList<Field> mPossibleIds = null;

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (mPossibleIds == null) {
                // we only need to do this once
                Field fProfileActivity_rowCount = kProfileActivity.getDeclaredField("rowCount");
                fProfileActivity_rowCount.setAccessible(true);
                int currentRowCount = fProfileActivity_rowCount.getInt(param.thisObject);
                if (currentRowCount == 0) {
                    ArrayList<Field> possibleIds = new ArrayList<>();
                    // find all private int fields whose value is -1 or 0
                    for (Field f : kProfileActivity.getDeclaredFields()) {
                        if (f.getType() == int.class && !Modifier.isStatic(f.getModifiers())) {
                            f.setAccessible(true);
                            int value = f.getInt(param.thisObject);
                            if (value == -1 || value == 0) {
                                possibleIds.add(f);
                            }
                        }
                    }
                    mPossibleIds = possibleIds;
                }
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (mPossibleIds == null) {
                Utils.loge(new RuntimeException("mPossibleIds is null"));
                return;
            }
            Field fProfileActivity_languageRow = kProfileActivity.getDeclaredField("languageRow");
            fProfileActivity_languageRow.setAccessible(true);
            // put our fields into the list just before the language row
            int targetRow = fProfileActivity_languageRow.getInt(param.thisObject);
            if (targetRow <= 0) {
                // languageRow is not, not user setting, so we can't do anything
                return;
            }
            ArrayList<Field> fields = new ArrayList<>();
            for (Field f : mPossibleIds) {
                f.setAccessible(true);
                int currentValue = f.getInt(param.thisObject);
                // check the value, drop if value < 0 (not valid row id)
                if (currentValue >= 0) {
                    fields.add(f);
                }
            }
            Field fProfileActivity_rowCount = kProfileActivity.getDeclaredField("rowCount");
            fProfileActivity_rowCount.setAccessible(true);
            int currentRowCount = fProfileActivity_rowCount.getInt(param.thisObject);
            if (fields.isEmpty() || currentRowCount == 0) {
                return;
            }
            // increase all the other later rows' ids by 1
            ArrayList<Field> rowFieldsToIncrease = new ArrayList<>();
            HashMap<Integer, Field> currentValues = new HashMap<>(rowFieldsToIncrease.size());
            for (Field f : fields) {
                int currentValue = f.getInt(param.thisObject);
                if (currentValue >= targetRow && currentValue < currentRowCount) {
                    rowFieldsToIncrease.add(f);
                }
            }
            for (Field f : rowFieldsToIncrease) {
                int currentValue = f.getInt(param.thisObject);
                currentValues.put(currentValue, f);
            }
            // check all the later rows' ids are found
            if (rowFieldsToIncrease.size() != currentRowCount - targetRow) {
                Utils.loge(new RuntimeException("rowFieldsToIncrease.size() != currentRowCount - targetRow, " +
                        "rowFieldsToIncrease.size() = " + rowFieldsToIncrease.size() + ", currentRowCount = " + currentRowCount));
                return;
            }
            // check duplicated ids
            for (int i = targetRow; i < currentRowCount; i++) {
                if (!currentValues.containsKey(i)) {
                    Utils.loge(new RuntimeException("can't find id " + i));
                    return;
                }
            }
            // do increase
            for (Field f : rowFieldsToIncrease) {
                int currentValue = f.getInt(param.thisObject);
                f.setInt(param.thisObject, currentValue + 1);
            }
            fProfileActivity_rowCount.setInt(param.thisObject, currentRowCount + 1);
            // save our row id into arguments bundle
            Bundle args = (Bundle) fBaseFragment_arguments.get(param.thisObject);
            if (args == null) {
                args = new Bundle();
                fBaseFragment_arguments.set(param.thisObject, args);
            }
            args.putInt(TMOE_SETTINGS_ROW_ID, targetRow);
        }
    };

    private static int getMyRowCountId(@NonNull Object profileActivity) {
        try {
            Bundle args = (Bundle) fBaseFragment_arguments.get(profileActivity);
            if (args != null) {
                return args.getInt(TMOE_SETTINGS_ROW_ID, -1);
            } else {
                return -1;
            }
        } catch (ReflectiveOperationException e) {
            Utils.loge(e);
            return -1;
        }
    }

    private static void presentTMoeSettingsFragment(@NonNull Object parentFragment) {
        ViewGroup parentLayout = ProxyFragmentRttiHandler.staticGetParentLayout(parentFragment);
        if (parentLayout != null) {
            ProxyFragmentRttiHandler.staticPresentFragment(parentLayout, new SettingsFragment(), false);
        }
    }
}
