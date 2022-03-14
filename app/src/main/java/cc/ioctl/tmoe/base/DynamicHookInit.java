package cc.ioctl.tmoe.base;

import java.util.List;

import cc.ioctl.tmoe.hook.base.DynamicHook;
import cc.ioctl.tmoe.hook.core.Initializable;
import cc.ioctl.tmoe.hook.core.InvalidationHook;
import cc.ioctl.tmoe.hook.core.SettingEntryHook;
import cc.ioctl.tmoe.hook.func.AntiAntiCopy;
import cc.ioctl.tmoe.hook.func.AntiAntiForward;
import cc.ioctl.tmoe.hook.func.EnableDebugMode;
import cc.ioctl.tmoe.hook.func.HideUserAvatar;
import cc.ioctl.tmoe.hook.func.ProhibitChannelSwitching;
import cc.ioctl.tmoe.hook.func.ProhibitEnableReactions;
import cc.ioctl.tmoe.util.Utils;

/**
 * @author cinit
 */
public class DynamicHookInit {

    public static void loadHooks() {
        initializeCoreHooks();
        initializeFunctionHooks();
    }

    private static void initializeCoreHooks() {
        Initializable[] coreHooks = new Initializable[]{
                InvalidationHook.INSTANCE,
                SettingEntryHook.INSTANCE,
                HideUserAvatar.INSTANCE
        };
        for (Initializable hook : coreHooks) {
            try {
                if (!hook.initialize()) {
                    Utils.loge("initialize failed: " + hook.getClass().getName());
                }
            } catch (Exception | LinkageError e) {
                Utils.loge(e);
            }
        }
    }

    private static DynamicHook[] sAllFunctionHooks = null;

    public static DynamicHook[] queryAllFunctionHooks() {
        if (sAllFunctionHooks == null) {
            sAllFunctionHooks = new DynamicHook[]{
                    EnableDebugMode.INSTANCE,
                    AntiAntiForward.INSTANCE,
                    ProhibitChannelSwitching.INSTANCE,
                    ProhibitEnableReactions.INSTANCE,
                    AntiAntiCopy.INSTANCE
            };
        }
        return sAllFunctionHooks;
    }

    private static void initializeFunctionHooks() {
        DynamicHook[] hooks = queryAllFunctionHooks();
        for (DynamicHook hook : hooks) {
            try {
                if (hook.isAvailable() && hook.isEnabledByUser()
                        && !hook.isPreparationRequired() && !hook.isInitialized()) {
                    // initialize hook
                    if (!hook.initialize()) {
                        Utils.logw("initialize failed: " + hook.getClass().getName());
                        List<Throwable> errors = hook.getErrors();
                        if (errors != null && !errors.isEmpty()) {
                            for (Throwable error : errors) {
                                Utils.loge(error);
                            }
                        }
                    }
                }
            } catch (Exception | LinkageError e) {
                Utils.loge(e);
            }
        }
    }
}
