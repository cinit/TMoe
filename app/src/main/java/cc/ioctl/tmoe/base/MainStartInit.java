package cc.ioctl.tmoe.base;

import android.app.Application;

import java.lang.reflect.Field;

import cc.ioctl.tmoe.hook.func.DatabaseCorruptionWarning;
import cc.ioctl.tmoe.lifecycle.Parasitics;
import cc.ioctl.tmoe.rtti.ProxyFragmentRttiHandler;
import cc.ioctl.tmoe.rtti.deobf.ClassLocator;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.MultiProcess;
import cc.ioctl.tmoe.util.Utils;

public class MainStartInit {
    public static final MainStartInit INSTANCE = new MainStartInit();

    private MainStartInit() {
    }

    private boolean mPreInitialized = false;
    private boolean mPostInitialized = false;

    public void initForPreStartup() {
        if (mPreInitialized) {
            return;
        }
        // init early hooks
        DynamicHookInit.allowEarlyInit(DatabaseCorruptionWarning.INSTANCE);
        mPreInitialized = true;
    }

    public void initForPostStartup() {
        if (mPostInitialized) {
            return;
        }
        Application app = HostInfo.getApplication();
        if (MultiProcess.isMainProcess()) {
            // init for proxy fragment
            findHostBaseFragmentAndInitForProxy();
            // init lifecycle and resource injection
            Parasitics.injectModuleResources(app.getApplicationContext().getResources());
            Parasitics.initForStubActivity(app);
            // init functional hooks
            DynamicHookInit.loadHooks();
        }
        mPostInitialized = true;
        // auxiliary init
        initForClassLocator();
    }

    private static void findHostBaseFragmentAndInitForProxy() {
        // find host base fragment
        Class<?> kBaseFragment = Initiator.load("org.telegram.ui.ActionBar.BaseFragment");
        if (kBaseFragment == null) {
            // maybe obfuscated
            Class<?> kAppBarLayout = Initiator.load("org.telegram.ui.ActionBar.ActionBarLayout");
            if (kAppBarLayout == null) {
                throw new RuntimeException("can not find class ActionBarLayout");
            }
            try {
                Field newFragment = kAppBarLayout.getDeclaredField("newFragment");
                kBaseFragment = newFragment.getType();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("can not find field ActionBarLayout.newFragment");
            }
        }
        // init for proxy
        try {
            ProxyFragmentRttiHandler.initProxyFragmentClass(kBaseFragment);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initForClassLocator() {
        Class<?> kTheme = ClassLocator.getThemeClass();
        if (kTheme == null) {
            Utils.loge("can not find class Theme");
            // maybe obfuscated
            Utils.async(() -> {
                Class<?> k = ClassLocator.findThemeClass();
                if (k != null) {
                    Utils.logd("find class Theme: " + k.getName());
                } else {
                    Utils.loge("can not find class Theme");
                }
            });
        }
    }
}
