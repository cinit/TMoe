package cc.ioctl.tmoe.base;

import java.util.List;

import cc.ioctl.tmoe.hook.base.DynamicHook;
import cc.ioctl.tmoe.hook.core.Initializable;
import cc.ioctl.tmoe.hook.core.InvalidationHook;
import cc.ioctl.tmoe.hook.core.SettingEntryHook;
import cc.ioctl.tmoe.hook.func.AddInfoContainer;
import cc.ioctl.tmoe.hook.func.AddReloadMsgBtn;
import cc.ioctl.tmoe.hook.func.AddSubItemChannel;
import cc.ioctl.tmoe.hook.func.AntiAntiCopy;
import cc.ioctl.tmoe.hook.func.AntiAntiForward;
import cc.ioctl.tmoe.hook.func.ChannelDetailNumbers;
import cc.ioctl.tmoe.hook.func.DatabaseCorruptionWarning;
import cc.ioctl.tmoe.hook.func.DisablePremiumStickerAnimation;
import cc.ioctl.tmoe.hook.func.DumpGroupMember;
import cc.ioctl.tmoe.hook.func.EnableDebugMode;
import cc.ioctl.tmoe.hook.func.ExtendedOfflineSearch;
import cc.ioctl.tmoe.hook.func.HidePremiumStickerSetTab;
import cc.ioctl.tmoe.hook.func.HistoricGroupMemberRecord;
import cc.ioctl.tmoe.hook.func.ForceBlurChatAvailable;
import cc.ioctl.tmoe.hook.func.HidePhoneNumber;
import cc.ioctl.tmoe.hook.func.HideUserAvatar;
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption;
import cc.ioctl.tmoe.hook.func.KeepVideoMuted;
import cc.ioctl.tmoe.hook.func.ProhibitChannelSwitching;
import cc.ioctl.tmoe.hook.func.ProhibitChatGreetings;
import cc.ioctl.tmoe.hook.func.ProhibitEnableReactions;
import cc.ioctl.tmoe.hook.func.ProhibitSpoilers;
import cc.ioctl.tmoe.hook.func.SendCommand;
import cc.ioctl.tmoe.hook.func.ShowMsgId;
import cc.ioctl.tmoe.hook.func.TgnetLogControlStartupApplyHelper;
import cc.ioctl.tmoe.hook.func.ViewTopicAsMsgByDefault;
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
                    AntiAntiCopy.INSTANCE,
                    ProhibitSpoilers.INSTANCE,
                    HistoricalNewsOption.INSTANCE,
                    ProhibitChatGreetings.INSTANCE,
                    HidePhoneNumber.INSTANCE,
                    AddSubItemChannel.INSTANCE,
                    ChannelDetailNumbers.INSTANCE,
                    AddInfoContainer.INSTANCE,
                    SendCommand.INSTANCE,
                    ShowMsgId.INSTANCE,
                    AddReloadMsgBtn.INSTANCE,
                    ForceBlurChatAvailable.INSTANCE,
                    DisablePremiumStickerAnimation.INSTANCE,
                    KeepVideoMuted.INSTANCE,
                    DumpGroupMember.INSTANCE,
                    DatabaseCorruptionWarning.INSTANCE,
                    HistoricGroupMemberRecord.INSTANCE,
                    ExtendedOfflineSearch.INSTANCE,
                    TgnetLogControlStartupApplyHelper.INSTANCE,
                    ViewTopicAsMsgByDefault.INSTANCE,
                    HidePremiumStickerSetTab.INSTANCE,
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

    public static void allowEarlyInit(DynamicHook hook) {
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
