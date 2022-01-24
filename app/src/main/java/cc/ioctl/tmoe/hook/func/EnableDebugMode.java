package cc.ioctl.tmoe.hook.func;

import cc.ioctl.tmoe.hook.base.CommonDynamicHook;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;

public class EnableDebugMode extends CommonDynamicHook {

    public static final EnableDebugMode INSTANCE = new EnableDebugMode();

    private EnableDebugMode() {
        super("EnableDebugMode");
    }

    @Override
    public boolean initOnce() throws Exception {
        if (isEnabledByUser()) {
            updateValue(true);
        }
        return true;
    }

    @Override
    public void setEnabledByUser(boolean enabled) {
        super.setEnabledByUser(enabled);
        try {
            updateValue(enabled);
        } catch (NoSuchFieldException e) {
            loge(e);
        }
    }

    private void updateValue(boolean value) throws NoSuchFieldException {
        Reflex.setStaticObject(Initiator.load("org.telegram.messenger.BuildVars"), "DEBUG_VERSION", value);
        Reflex.setStaticObjectSilently(Initiator.load("org.telegram.messenger.BuildVars"), "DEBUG_PRIVATE_VERSION", value);
    }
}
