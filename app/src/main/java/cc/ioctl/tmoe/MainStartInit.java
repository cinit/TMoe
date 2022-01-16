package cc.ioctl.tmoe;

import android.app.Application;

import cc.ioctl.tmoe.base.DynamicHookInit;
import cc.ioctl.tmoe.lifecycle.Parasitics;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.MultiProcess;

public class MainStartInit {
    public static final MainStartInit INSTANCE = new MainStartInit();

    private MainStartInit() {
    }

    private boolean mInitialized = false;

    public void initForStartup() {
        if (mInitialized) {
            return;
        }
        Application app = HostInfo.getApplication();
        if (MultiProcess.isMainProcess()) {
            // init lifecycle and resource injection
            Parasitics.injectModuleResources(app.getApplicationContext().getResources());
            Parasitics.initForStubActivity(app);
            // init functional hooks
            DynamicHookInit.loadHooks();
        }
        mInitialized = true;
    }

}
