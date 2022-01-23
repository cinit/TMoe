package cc.ioctl.tmoe.startup;

import java.util.ArrayList;

import cc.ioctl.tmoe.R;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final ArrayList<String> TELEGRAM_CLIENT_PACKAGE_NAME_LIST = new ArrayList<>(16);

    static {
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.telegram.messenger");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.telegram.messenger.beta");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.telegram.plus");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("nekox.messenger");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("com.jasonkhew96.pigeongram");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("app.nicegram");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("ir.ilmili.telegraph");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("xyz.nextalone.nagram");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.telegram.messenger.web");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("com.cool2645.nekolite");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("com.iMe.android");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.telegram.BifToGram");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("ua.itaysonlab.messenger");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.forkclient.messenger.beta");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.aka.messenger");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("ellipi.messenger");
    }

    private static String sModulePath = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (R.string.res_inject_success >>> 24 == 0x7f) {
            XposedBridge.log("package id must NOT be 0x7f, reject loading...");
            return;
        }
        String packageName = lpparam.packageName;
        if (TELEGRAM_CLIENT_PACKAGE_NAME_LIST.contains(packageName)) {
            StartupHook.INSTANCE.doInit(lpparam.classLoader);
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
