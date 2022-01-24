package cc.ioctl.tmoe.hook.core;

import cc.ioctl.tmoe.ui.LayoutHelper;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.ui.Theme;

/**
 * Hooks for theme changes, locale changes, configuration changes, etc.
 */
public class InvalidationHook implements Initializable {
    public static final InvalidationHook INSTANCE = new InvalidationHook();

    private boolean mInitialized = false;

    private InvalidationHook() {
    }

    @Override
    public boolean initialize() {
        if (mInitialized) {
            return true;
        }
        // TODO: 2022-01-24 add hooks to Theme changes, configuration changes, etc.
        mInitialized = true;
        return true;
    }

    private static void dispatchConfigurationChanged() {
        LayoutHelper.invalidate();
    }

    private static void dispatchThemeChanged() {
        Theme.invalidate();
    }

    private static void dispatchLocaleChanged() {
        LocaleController.invalidate();
    }
}
