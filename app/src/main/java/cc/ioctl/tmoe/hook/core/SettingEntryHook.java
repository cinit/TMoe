package cc.ioctl.tmoe.hook.core;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.fragment.SettingsFragment;
import cc.ioctl.tmoe.lifecycle.Parasitics;
import cc.ioctl.tmoe.rtti.ProxyFragmentRttiHandler;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;

public class SettingEntryHook implements Initializable, ProfileActivityRowHook.Callback {
    public static final SettingEntryHook INSTANCE = new SettingEntryHook();

    private SettingEntryHook() {
    }

    private static final String TMOE_SETTINGS_ROW = "TMOE_SETTINGS_ROW";

    private boolean mInitialized = false;

    @Override
    public boolean initialize() {
        if (mInitialized) {
            return true;
        }
        ProfileActivityRowHook.addCallback(this);
        mInitialized = true;
        return true;
    }

    private static void presentTMoeSettingsFragment(@NonNull Object parentFragment) {
        ViewGroup parentLayout = ProxyFragmentRttiHandler.staticGetParentLayout(parentFragment);
        if (parentLayout != null) {
            ProxyFragmentRttiHandler.staticPresentFragment(parentLayout, new SettingsFragment(), false);
        }
    }

    @Override
    public boolean onBindViewHolder(@NonNull String key, @NonNull Object holder, @NonNull Object adpater, @NonNull Object profileActivity) {
        if (!TMOE_SETTINGS_ROW.equals(key)) {
            return false;
        }
        FrameLayout textCell = (FrameLayout) Reflex.getInstanceObjectOrNull(holder, "itemView");
        if (textCell != null) {
            // color and theme is already set by Telegram, we only need to set the text and icon
            // textCell.setTextAndIcon(text, iconResId, true)
            // inject resources
            Parasitics.injectModuleResources(textCell.getContext().getResources());
            Parasitics.injectModuleResources(HostInfo.getApplication().getResources());
            String text = LocaleController.getString("TMoeSettings", R.string.TMoeSettings);
            int iconResId = R.drawable.ic_setting_hex_outline_24;
            try {
                try {
                    Reflex.invokeVirtual(textCell, "setTextAndIcon", text, iconResId, true,
                            CharSequence.class, int.class, boolean.class, void.class);
                } catch (NoSuchMethodException e) {
                    Reflex.invokeVirtual(textCell, "setTextAndIcon", text, iconResId, true,
                            String.class, int.class, boolean.class, void.class);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            Utils.loge(new IllegalStateException("textCell is null"));
        }
        return true;
    }

    @Override
    public int getItemViewType(@NonNull String key, @NonNull Object adapter, @NonNull Object profileActivity) {
        if (TMOE_SETTINGS_ROW.equals(key)) {
            return 4;
        }
        return -1;
    }

    @Override
    public boolean onItemClicked(@NonNull String key, @NonNull Object adapter, @NonNull Object profileActivity) {
        if (TMOE_SETTINGS_ROW.equals(key)) {
            presentTMoeSettingsFragment(profileActivity);
            return false;
        }
        return false;
    }

    @Override
    public void onInsertRow(@NonNull ProfileActivityRowHook.RowManipulator manipulator, @NonNull Object profileActivity) {
        // put our fields into the list just before the language row
        int targetRow = manipulator.getRowIdForField("languageRow");
        if (targetRow <= 0) {
            // languageRow is not, not user setting, so we can't do anything
            return;
        }
        manipulator.insertRowAtPosition(TMOE_SETTINGS_ROW, targetRow);
    }
}
