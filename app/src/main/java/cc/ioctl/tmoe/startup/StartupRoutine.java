package cc.ioctl.tmoe.startup;


import android.app.Application;
import android.os.Build;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import cc.ioctl.tmoe.base.MainStartInit;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Initiator;

public class StartupRoutine {

    private StartupRoutine() {
        throw new AssertionError("No instance for you!");
    }

    /**
     * Parent ClassLoader is now changed to a new one, we can initialize the rest now.
     * There are the early init procedures.
     *
     * @param application the application
     * @param lpwReserved null, not used
     * @param bReserved   false, not used
     */
    public static void execPreStartupInit(Application application, String lpwReserved, boolean bReserved) {
        // native library was already loaded before this method is called
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
        HostInfo.setHostApplication(application);
        Initiator.initWithHostClassLoader(application.getClassLoader());
        MainStartInit.INSTANCE.initForPreStartup();
    }

    public static void execPostStartupInit() {
        MainStartInit.INSTANCE.initForPostStartup();
    }
}
