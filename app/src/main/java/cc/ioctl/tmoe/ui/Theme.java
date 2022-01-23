package cc.ioctl.tmoe.ui;

import java.util.Random;

import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;

public class Theme {
    private Theme() {
        throw new IllegalStateException("no instance");
    }

    public static final String key_windowBackgroundWhiteBlueHeader = "windowBackgroundWhiteBlueHeader";
    public static final String key_windowBackgroundGray = "windowBackgroundGray";

    public static int getColor(String key) {
        return getColor(key, null, false);
    }

    public static int getColor(String key, boolean[] isDefault) {
        return getColor(key, isDefault, false);
    }

    static Random random = new Random();

    public static int getColor(String key, boolean[] isDefault, boolean ignoreAnimation) {
        Class<?> kTheme = Initiator.load("org.telegram.ui.ActionBar.Theme");
        if (kTheme != null) {
            try {
                return (int) Reflex.invokeStatic(kTheme, "getColor", key, isDefault, ignoreAnimation,
                        String.class, boolean[].class, boolean.class, int.class);
            } catch (ReflectiveOperationException e) {
                Utils.loge(e);
            }
        }
        // error occurs
        return (key.hashCode() ^ random.nextInt()) | 0x80000000;
    }
}
