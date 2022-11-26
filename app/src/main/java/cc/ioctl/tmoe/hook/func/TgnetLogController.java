package cc.ioctl.tmoe.hook.func;


import java.lang.reflect.Field;

import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Log;

public class TgnetLogController {

    private TgnetLogController() {
    }

    private static Field sLogsEnabledField;

    private static Field getLogsEnabledField() {
        if (sLogsEnabledField == null) {
            try {
                sLogsEnabledField = Initiator.loadClass("org.telegram.messenger.BuildVars").getDeclaredField("LOGS_ENABLED");
                sLogsEnabledField.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                Log.e(e);
            }
        }
        return sLogsEnabledField;
    }

    public static int getCurrentBuildVarsLogStatus() {
        Field field = getLogsEnabledField();
        if (field == null) {
            return -1;
        }
        try {
            return field.getBoolean(null) ? 1 : 0;
        } catch (Exception e) {
            Log.e(e);
            return -1;
        }
    }

    public static int setCurrentBuildVarsLogStatus(int status) {
        Field field = getLogsEnabledField();
        if (field == null) {
            return -1;
        }
        try {
            field.setBoolean(null, status != 0);
            return 0;
        } catch (Exception e) {
            Log.e(e);
            return -1;
        }
    }

    public static native int getCurrentTgnetLogStatus();

    public static native int setCurrentTgnetLogStatus(int status);

    public static void setupClientLogPreferenceForStartup() {
        setCurrentBuildVarsLogStatus(0);
        setCurrentTgnetLogStatus(0);
    }
}
