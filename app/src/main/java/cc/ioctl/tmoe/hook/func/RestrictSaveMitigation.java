package cc.ioctl.tmoe.hook.func;

import cc.ioctl.tmoe.hook.base.CommonDynamicHook;
import cc.ioctl.tmoe.util.Initiator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class RestrictSaveMitigation extends CommonDynamicHook {
    public static final RestrictSaveMitigation INSTANCE = new RestrictSaveMitigation();

    private RestrictSaveMitigation() {
    }

    @Override
    public boolean initOnce() throws Exception {
        Class<?> kMessagesController = Initiator.loadClass("org.telegram.messenger.MessagesController");
        XposedBridge.hookAllMethods(kMessagesController, "isChatNoForwards", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (isEnabledByUser()) {
                    param.setResult(false);
                }
            }
        });
        return true;
    }
}
