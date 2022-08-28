package cc.ioctl.tmoe.startup;

import android.app.Application;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class StartupHook {
    public static final StartupHook INSTANCE = new StartupHook();

    private StartupHook() {
    }

    private boolean sPre1Initialized = false;
    private boolean sPost2Initialized = false;

    private static void injectClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader == null");
        }
        try {
            Field fParent = ClassLoader.class.getDeclaredField("parent");
            fParent.setAccessible(true);
            ClassLoader mine = StartupHook.class.getClassLoader();
            ClassLoader curr = (ClassLoader) fParent.get(mine);
            if (curr == null) {
                curr = XposedBridge.class.getClassLoader();
            }
            if (!curr.getClass().getName().equals(HybridClassLoader.class.getName())) {
                fParent.set(mine, new HybridClassLoader(curr, classLoader));
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    public void doInit(ClassLoader rtLoader) {
        // our minSdk is 21 so there is no need to wait for MultiDex to initialize
        if (sPre1Initialized) {
            return;
        }
        if (rtLoader == null) {
            throw new AssertionError("StartupHook.doInit: rtLoader == null");
        }
        Class<?> applicationClass = null;
        try {
            applicationClass = rtLoader.loadClass("org.telegram.messenger.ApplicationLoader");
        } catch (ClassNotFoundException ignored) {
        }
        if (applicationClass == null) {
            try {
                applicationClass = rtLoader.loadClass("org.thunderdog.challegram.BaseApplication");
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (applicationClass == null) {
            throw new AssertionError("StartupHook.doInit: unable to find ApplicationLoader");
        }
        XposedHelpers.findAndHookMethod(applicationClass, "onCreate", new XC_MethodHook(51) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Application app = (Application) param.thisObject;
                if (app == null) {
                    throw new AssertionError("app == null");
                }
                NativeLoader.loadAllSharedLibraries(app);
                if (!NativeLoader.isNativeLibraryLoaded()) {
                    throw new AssertionError("NativeLoader.isNativeLibraryLoaded() == false");
                }
                StartupRoutine.execPreStartupInit(app, null, false);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (sPost2Initialized) {
                    return;
                }
                StartupRoutine.execPostStartupInit();
                sPost2Initialized = true;
            }
        });
        sPre1Initialized = true;
    }
}
