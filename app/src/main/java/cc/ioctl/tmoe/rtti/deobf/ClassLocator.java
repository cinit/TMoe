package cc.ioctl.tmoe.rtti.deobf;

import javax.annotation.Nullable;

import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.dex.DexKit;

public class ClassLocator {

    private static Class<?> kThemeClass = null;
    private static boolean kThemeFastFail = false;

    @Nullable
    public static Class<?> getThemeClass() {
        if (kThemeClass != null) {
            return kThemeClass;
        }
        if (kThemeFastFail) {
            return null;
        }
        kThemeClass = Initiator.load("org.telegram.ui.ActionBar.Theme");
        if (kThemeClass != null) {
            return kThemeClass;
        }
        kThemeClass = DexKit.loadClassFromCache(DexKit.C_THEME);
        if (kThemeClass == null) {
            kThemeFastFail = true;
        }
        return kThemeClass;
    }

    @Nullable
    public static Class<?> findThemeClass() {
        if (getThemeClass() != null) {
            return kThemeClass;
        }
        kThemeClass = DexKit.doFindClass(DexKit.C_THEME);
        return kThemeClass;
    }
}
