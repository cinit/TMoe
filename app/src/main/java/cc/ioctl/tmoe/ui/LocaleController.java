package cc.ioctl.tmoe.ui;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;

public class LocaleController {

    public static String getString(@NonNull String key, int res) {
        return getStringImpl(key, null, res);
    }

    public static String getString(@NonNull String key, @Nullable String fallback, int res) {
        return getStringImpl(key, fallback, res);
    }

    private static String getStringImpl(@NonNull String key, @Nullable String fallback, int res) {
        Class<?> kLocaleController = Initiator.load("org.telegram.messenger.LocaleController");
        if (kLocaleController != null) {
            try {
                return (String) Reflex.invoke_static(kLocaleController, "getString", key, fallback, res, String.class, String.class, int.class, String.class);
            } catch (Exception ignored) {
            }
        }
        // unable to load LocaleController
        if (fallback != null) {
            return fallback;
        }
        // fallback to resource
        if (res != 0) {
            try {
                return HostInfo.getApplication().getString(res);
            } catch (Resources.NotFoundException ignored) {
            }
        }
        // fallback to key
        return "LC_ERR:" + key;
    }

}
