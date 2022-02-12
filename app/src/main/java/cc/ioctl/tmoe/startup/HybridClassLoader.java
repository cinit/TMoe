package cc.ioctl.tmoe.startup;

import android.content.Context;

import java.net.URL;

/**
 * NOTICE: Do NOT use any androidx annotations here.
 */
public class HybridClassLoader extends ClassLoader {

    /**
     * The bootstrap class loader for Android, effectively NonNull.
     */
    private static final ClassLoader BOOT_CLASS_LOADER = Context.class.getClassLoader();
    private static String sObfuscatedPackageName = null;
    private final ClassLoader clPreload;
    private final ClassLoader clBase;

    public HybridClassLoader(ClassLoader x, ClassLoader ctx) {
        clPreload = x;
        clBase = ctx;
    }

    /**
     * Check if a class name is conflicting with the host application.
     *
     * @param name NonNull, class name
     * @return true if conflicting
     */
    public static boolean isConflictingClass(String name) {
        return name.startsWith("androidx.") || name.startsWith("android.support.v4.")
                || name.startsWith("kotlin.") || name.startsWith("kotlinx.")
                || name.startsWith("com.tencent.mmkv.")
                || name.startsWith("com.android.tools.r8.")
                || name.startsWith("com.google.android.material.")
                || name.startsWith("com.google.gson.")
                || name.startsWith("org.intellij.lang.annotations.")
                || name.startsWith("org.jetbrains.annotations.")
                || name.startsWith("org.lsposed.hiddenapibypass.");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return BOOT_CLASS_LOADER.loadClass(name);
        } catch (ClassNotFoundException ignored) {
        }
        if (name != null && isConflictingClass(name)) {
            //Nevertheless, this will not interfere with the host application,
            //classes in host application SHOULD find with their own ClassLoader, eg Class.forName()
            //use shipped androidx and kotlin lib.
            throw new ClassNotFoundException(name);
        }
        // The ClassLoader for some apk-modifying frameworks are terrible, XposedBridge.class.getClassLoader()
        // is the sane as Context.getClassLoader(), which mess up with 3rd lib, can cause the ART to crash.
        if (clPreload != null) {
            try {
                return clPreload.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (clBase != null) {
            try {
                return clBase.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        URL ret = clPreload.getResource(name);
        if (ret != null) {
            return ret;
        }
        return clBase.getResource(name);
    }

    public static void setObfuscatedXposedApiPackage(String packageName) {
        sObfuscatedPackageName = packageName;
    }

    public static String getObfuscatedXposedApiPackage() {
        return sObfuscatedPackageName;
    }

    public static String getXposedBridgeClassName() {
        if (sObfuscatedPackageName == null) {
            return "de.robv.android.xposed.XposedBridge";
        } else {
            return sObfuscatedPackageName + ".XposedBridge";
        }
    }
}
