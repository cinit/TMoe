package cc.ioctl.tmoe.startup;

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit;

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
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("me.luvletter.nekox");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.nift4.catox");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("ua.itaysonlab.messenger");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("icu.ketal.yunigram");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("icu.ketal.yunigram.lspatch");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("icu.ketal.yunigram.beta");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("icu.ketal.yunigram.lspatch.beta");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("org.forkgram.messenger");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("com.blxueya.GuGugram");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("com.blxueya.GuGugramX");
        TELEGRAM_CLIENT_PACKAGE_NAME_LIST.add("it.owlgram.android");
    }

    private static String sModulePath = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (R.string.res_inject_success >>> 24 == 0x7f) {
            XposedBridge.log("package id must NOT be 0x7f, reject loading...");
            return;
        }
        String packageName = lpparam.packageName;
        // check LSPosed dex-obfuscation
        Class<?> kXposedBridge = XposedBridge.class;
        if (!"de.robv.android.xposed.XposedBridge".equals(kXposedBridge.getName())) {
            String className = kXposedBridge.getName();
            String pkgName = className.substring(0, className.lastIndexOf('.'));
            HybridClassLoader.setObfuscatedXposedApiPackage(pkgName);
        }
        if (TELEGRAM_CLIENT_PACKAGE_NAME_LIST.contains(packageName)) {
            StartupHook.INSTANCE.doInit(lpparam.classLoader);
            EzXHelperInit.INSTANCE.initHandleLoadPackage(lpparam);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        EzXHelperInit.INSTANCE.initZygote(startupParam);
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
