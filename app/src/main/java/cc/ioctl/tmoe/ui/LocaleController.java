package cc.ioctl.tmoe.ui;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Objects;

import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;

public class LocaleController {
    private LocaleController() {
        throw new AssertionError("no instance");
    }

    public static String getString(@NonNull String key) {
        return getString(key, 0);
    }

    public static String getString(@NonNull String key, int res) {
        return getStringImpl(key, null, res);
    }

    public static String getString(@NonNull String key, @Nullable String fallback, int res) {
        return getStringImpl(key, fallback, res);
    }

    private static final HashMap<String, Integer> sNameToResIdCache = new HashMap<>();

    private static String getStringImpl(@NonNull String key, @Nullable String fallback, int res) {
        Objects.requireNonNull(key, "key is null");
        if (res == 0) {
            // guess resource id
            Integer guessedResId = sNameToResIdCache.get(key);
            if (guessedResId == null) {
                res = HostInfo.getApplication().getResources().
                        getIdentifier(key, "string", HostInfo.getPackageName());
                // put back even if 0, faster next time
                sNameToResIdCache.put(key, res);
            } else {
                res = guessedResId;
            }
        }
        Class<?> kLocaleController = Initiator.load("org.telegram.messenger.LocaleController");
        if (kLocaleController != null) {
            try {
                return (String) Reflex.invokeStatic(kLocaleController, "getString", key, fallback, res, String.class, String.class, int.class, String.class);
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

    public static boolean isRTL() {
        Class<?> kLocaleController = Initiator.load("org.telegram.messenger.LocaleController");
        if (kLocaleController != null) {
            try {
                return Boolean.TRUE.equals(Reflex.getStaticObject(kLocaleController, "isRTL", boolean.class));
            } catch (NoSuchFieldException ignored) {
            }
        }
        // unable to load LocaleController
        return false;
    }

    public static void invalidate() {
        // nothing to invalidate yet
    }
}
