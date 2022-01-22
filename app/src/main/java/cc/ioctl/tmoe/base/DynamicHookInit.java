package cc.ioctl.tmoe.base;

import cc.ioctl.tmoe.hook.SettingEntryHook;
import cc.ioctl.tmoe.util.Utils;

/**
 * @author cinit
 */
public class DynamicHookInit {

    public static void loadHooks() {
        try {
            test();
        } catch (Throwable e) {
            Utils.loge(e);
        }
    }

    private static void test() throws Throwable {
        SettingEntryHook.INSTANCE.init();
    }
}
