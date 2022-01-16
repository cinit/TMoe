package cc.ioctl.tmoe.startup;

import cc.ioctl.tmoe.R;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String PACKAGE_NAME_TELEGRAM = "org.telegram.messenger";
    public static final String PACKAGE_NAME_NEKO_X = "nekox.messenger";
    public static final String PACKAGE_NAME_PIGEON_GRAM = "com.jasonkhew96.pigeongram";
    public static final String PACKAGE_NAME_NA_GRAM = "xyz.nextalone.nagram";

    private static String sModulePath = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (R.string.res_inject_success >>> 24 == 0x7f) {
            XposedBridge.log("package id must NOT be 0x7f, reject loading...");
            return;
        }
        switch (lpparam.packageName) {
            case PACKAGE_NAME_TELEGRAM:
            case PACKAGE_NAME_NEKO_X:
            case PACKAGE_NAME_PIGEON_GRAM:
            case PACKAGE_NAME_NA_GRAM:
                StartupHook.INSTANCE.doInit(lpparam.classLoader);
                break;
            default:
                // do nothing
                break;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        sModulePath = startupParam.modulePath;
    }

    public static String getModulePath() {
        String path = sModulePath;
        if (path == null) {
            throw new IllegalStateException("sModulePath is null");
        }
        return path;
    }
}
