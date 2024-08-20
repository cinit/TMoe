package cc.ioctl.tmoe.hook.core;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ProfileActivityRowHook implements Initializable {
    public static final ProfileActivityRowHook INSTANCE = new ProfileActivityRowHook();
    private static final String PROFILE_ACTIVITY_EXTRA_ROWS = "TMOE_PROFILE_ACTIVITY_EXTRA_ROWS";

    static class RowInfo {
        static final int TYPE_KEY = 1;
        static final int TYPE_FIELD = 2;
        int type;
        String name;
        int index;
        int fIndex; // for field
    }

    public static final class RowManipulator {
        List<RowInfo> rows = new ArrayList<>();
        int keyCount = 0;

        public int getRowIdForField(String fieldName) {
            for (var a : rows) {
                if (a.type == RowInfo.TYPE_FIELD && fieldName.equals(a.name)) {
                    return a.index;
                }
            }
            return -1;
        }

        public int getRowIdForKey(String key) {
            for (var a : rows) {
                if (a.type == RowInfo.TYPE_KEY && key.equals(a.name)) {
                    return a.index;
                }
            }
            return -1;
        }

        public void insertRowAtPosition(String key, int position) {
            for (var a : rows) {
                if (a.type == RowInfo.TYPE_KEY && key.equals(a.name)) {
                    throw new IllegalArgumentException("duplicated row key " + key);
                }
            }
            for (var a : rows) {
                if (a.index >= position) {
                    a.index++;
                }
            }
            var row = new RowInfo();
            row.type = RowInfo.TYPE_KEY;
            row.index = position;
            row.name = key;
            rows.add(row);
            keyCount++;
        }

        public boolean insertRowAtField(String key, String fieldName) {
            var idx = getRowIdForField(fieldName);
            if (idx == -1) {
                return false;
            }
            insertRowAtPosition(key, idx);
            return true;
        }
    }

    public interface Callback {
        /**
         * @param key
         * @param adpater
         * @param holder
         * @param profileActivity
         * @return true if handled, otherwise false
         */
        boolean onBindViewHolder(@NonNull String key, @NonNull Object holder, @NonNull Object adpater, @NonNull Object profileActivity);

        /**
         * getItemViewType
         *
         * @param key
         * @param adapter
         * @param profileActivity
         * @return type, -1 to skip
         */
        int getItemViewType(@NonNull String key, @NonNull Object adapter, @NonNull Object profileActivity);

        /**
         * @param key
         * @param adapter
         * @return true if handled, otherwise false
         */
        boolean onItemClicked(@NonNull String key, @NonNull Object adapter, @NonNull Object profileActivity);

        void onInsertRow(@NonNull RowManipulator manipulator, @NonNull Object profileActivity);
    }

    private static final List<Callback> sCallbacks = new ArrayList<>();

    private ProfileActivityRowHook() {
    }

    private boolean mInitialized = false;
    private static Field fBaseFragment_arguments = null;
    private static Class<?> kBaseFragment = null;
    private static Class<?> kProfileActivity = null;
    private static Field fProfileActivity_notificationRow = null;
    private static boolean sListViewOnItemClickListenerHooked = false;
    private static ArrayList<Field> mPossibleIds = null;

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
                            var rowKey = getRowForKey(fragment, position);
                            if (rowKey == null) {
                                return;
                            }
                            param.setResult(true);
                        }
                    }
                }
            });
            XposedBridge.hookMethod(ListAdapter_getItemViewType, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int position = (int) param.args[0];
                    Object fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "this$0");
                    var rowKey = getRowForKey(fragment, position);
                    if (rowKey == null) {
                        return;
                    }
                    for (var c : sCallbacks) {
                        var type = c.getItemViewType(rowKey, param.thisObject, fragment);
                        if (type != -1) {
                            param.setResult(type);
                            break;
                        }
                    }
                }
            });
            XposedBridge.hookMethod(ListAdapter_onBindViewHolder, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int position = (int) param.args[1];
                    Object fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "this$0");
                    var rowKey = getRowForKey(fragment, position);
                    if (rowKey == null) {
                        return;
                    }
                    var holder = param.args[0];
                    for (var c : sCallbacks) {
                        if (c.onBindViewHolder(rowKey, holder, param.thisObject, fragment)) {
                            param.setResult(null);
                            break;
                        }
                    }
                }
            });
            XposedBridge.hookAllMethods(Initiator.load("org.telegram.ui.ProfileActivity$DiffCallback"), "fillPositions", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object fragment = Reflex.getInstanceObjectOrNull(param.thisObject, "this$0");
                    var args = (Bundle) fBaseFragment_arguments.get(fragment);
                    if (args == null) {
                        return;
                    }
                    var map = (HashMap<Integer, String>) args.getSerializable(PROFILE_ACTIVITY_EXTRA_ROWS);
                    if (map == null) {
                        return;
                    }
                    var i = 1000;
                    for (var pos : map.keySet()) {
                        XposedHelpers.callMethod(param.thisObject, "put", ++i, pos, param.args[0]);
                    }
                }
            });
            // we only need to do this once
            Field fProfileActivity_rowCount = kProfileActivity.getDeclaredField("rowCount");
            fProfileActivity_rowCount.setAccessible(true);
            ArrayList<Field> possibleIds = new ArrayList<>();
            // find all private int fields whose value is -1 or 0
            for (Field f : kProfileActivity.getDeclaredFields()) {
                if (f.getType() == int.class && !Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    if (f.getName().endsWith("Row") || f.getName().endsWith("Row2") || "helpSectionCell".equals(f.getName())) {
                        possibleIds.add(f);
                    }
                }
            }
            mPossibleIds = possibleIds;
        } catch (ReflectiveOperationException e) {
            Utils.loge(e);
            return false;
        }
        mInitialized = true;
        return true;
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
            var rowKey = getRowForKey(fragment, position);
            if (rowKey == null) {
                return;
            }
            for (var c : sCallbacks) {
                if (c.onItemClicked(rowKey, param.thisObject, fragment)) {
                    param.setResult(null);
                    break;
                }
            }
        }
    };

    private static final XC_MethodHook SETTINGS_ROW_ID_ALLOCATOR = new XC_MethodHook(51) {

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (mPossibleIds == null) {
                Utils.loge(new RuntimeException("mPossibleIds is null"));
                return;
            }
            ArrayList<Field> fields = new ArrayList<>();
            for (Field f : mPossibleIds) {
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

            var manipulator = new RowManipulator();
            for (int i = 0; i < fields.size(); i++) {
                var f = fields.get(i);
                var row = new RowInfo();
                row.name = f.getName();
                row.type = RowInfo.TYPE_FIELD;
                row.index = f.getInt(param.thisObject);
                row.fIndex = i;
                manipulator.rows.add(row);
            }

            for (Callback c : sCallbacks) {
                c.onInsertRow(manipulator, param.thisObject);
            }

            for (var row : manipulator.rows) {
                if (row.type == RowInfo.TYPE_FIELD) {
                    fields.get(row.fIndex).setInt(param.thisObject, row.index);
                }
            }

            fProfileActivity_rowCount.setInt(param.thisObject, currentRowCount + manipulator.keyCount);
            // save our row id into arguments bundle
            Bundle args = (Bundle) fBaseFragment_arguments.get(param.thisObject);
            if (args == null) {
                args = new Bundle();
                fBaseFragment_arguments.set(param.thisObject, args);
            }
            var map = new HashMap<Integer, String>();
            for (var row : manipulator.rows) {
                if (row.type == RowInfo.TYPE_KEY) {
                    map.put(row.index, row.name);
                }
            }
            args.putSerializable(PROFILE_ACTIVITY_EXTRA_ROWS, map);
        }
    };

    @SuppressWarnings("unchecked")
    private static @Nullable String getRowForKey(Object profileActivity, int row) {
        try {
            var args = (Bundle) fBaseFragment_arguments.get(profileActivity);
            if (args == null) {
                return null;
            }
            var map = (HashMap<Integer, String>) args.getSerializable(PROFILE_ACTIVITY_EXTRA_ROWS);
            if (map == null) {
                return null;
            }
            return map.get(row);
        } catch (Throwable t) {
            Utils.loge(t);
            return null;
        }
    }

    public static void addCallback(Callback callback) {
        sCallbacks.add(callback);
    }
}
