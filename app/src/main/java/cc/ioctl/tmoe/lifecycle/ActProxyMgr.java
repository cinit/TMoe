package cc.ioctl.tmoe.lifecycle;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import cc.ioctl.tmoe.ui.___WindowIsTranslucent;

/**
 * This class is used to cope with Activity
 */
public class ActProxyMgr {

    public static final String STUB_TRANSLUCENT_ACTIVITY = "org.telegram.ui.VoIPPermissionActivity";

    public static final String ACTIVITY_PROXY_INTENT = "Lcc/ioctl/tmoe/lifecycle/ActProxyMgr;->ACTIVITY_PROXY_INTENT";

    private ActProxyMgr() {
        throw new AssertionError("No instance for you!");
    }

    // NOTICE: ** If you have created your own package, add it to proguard-rules.pro.**

    public static boolean isModuleProxyActivity(@NonNull String className) {
        if (TextUtils.isEmpty(className)) {
            return false;
        }
        return className.startsWith("cc.ioctl.tmoe.activity.");
    }

    public static boolean isModuleBundleClassLoaderRequired(@NonNull String className) {
        if (!isModuleProxyActivity(className)) {
            return false;
        }
        try {
            Class<?> clazz = Class.forName(className);
            return AppCompatActivity.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

    public static boolean isWindowTranslucent(Class<?> clazz) {
        return clazz != null && (___WindowIsTranslucent.class.isAssignableFrom(clazz));
    }
}
