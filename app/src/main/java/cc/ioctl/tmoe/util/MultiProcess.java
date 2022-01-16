package cc.ioctl.tmoe.util;

import static cc.ioctl.tmoe.util.Utils.loge;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class MultiProcess {

    public static final int PROC_MAIN = 1;
    public static final int PROC_PHOENIX = 2;
    public static final int PROC_OTHERS = 1 << 31;

    private static String sProcName = null;
    private static int sProcType = 0;

    public static boolean isMainProcess() {
        return getProcessType() == PROC_MAIN;
    }

    public static int getProcessType() {
        if (sProcType != 0) {
            return sProcType;
        }
        String[] parts = getProcessName().split(":");
        if (parts.length == 1) {
            if (parts[0].equals("unknown")) {
                return PROC_MAIN;
            } else {
                sProcType = PROC_MAIN;
            }
        } else {
            String tail = parts[parts.length - 1];
            switch (tail) {
                case "phoenix":
                    sProcType = PROC_PHOENIX;
                    break;
                default:
                    sProcType = PROC_OTHERS;
                    break;
            }
        }
        return sProcType;
    }

    public static String getProcessName() {
        if (sProcName != null) {
            return sProcName;
        }
        String name = "unknown";
        int retry = 0;
        do {
            try {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses =
                        ((ActivityManager) HostInfo.getApplication().getSystemService(Context.ACTIVITY_SERVICE))
                                .getRunningAppProcesses();
                if (runningAppProcesses != null) {
                    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                        if (runningAppProcessInfo != null
                                && runningAppProcessInfo.pid == android.os.Process.myPid()) {
                            sProcName = runningAppProcessInfo.processName;
                            return runningAppProcessInfo.processName;
                        }
                    }
                }
            } catch (Throwable e) {
                loge("getProcessName error " + e);
            }
            retry++;
            if (retry >= 3) {
                break;
            }
        } while ("unknown".equals(name));
        return name;
    }

}
