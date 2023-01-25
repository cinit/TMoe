package cc.ioctl.tmoe.fragment

import android.widget.Toast
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.func.*
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.BaseHierarchyFragment
import cc.ioctl.tmoe.ui.dsl.HierarchyDescription
import cc.ioctl.tmoe.ui.dsl.item.AbstractSwitch
import kotlin.system.exitProcess

class SettingsFragment : BaseHierarchyFragment() {
    override val hierarchy = HierarchyDescription(
        titleKey = "TMoeSettings",
        titleResId = R.string.TMoeSettings,
    ) {
        category("BasicFunction", R.string.BasicFunction) {
            functionSwitch(
                EnableDebugMode.INSTANCE, "EnableDebugMode", R.string.EnableDebugMode
            )
            functionSwitch(
                AntiAntiForward, "AntiAntiForward", R.string.AntiAntiForward,
                "RestrictContentMitigationDesc", R.string.RestrictContentMitigationDesc
            )
            functionSwitch(
                HideSponsoredMsg, "HideSponsoredMsg", R.string.HideSponsoredMsg
            )
            functionSwitch(
                HideUserAvatar.INSTANCE, "HideUserAvatar", R.string.HideUserAvatar,
                descProvider = {
                    if (HideUserAvatar.INSTANCE.isEnabledByUser) {
                        LocaleController.formatString(
                            "HideUserAvatarEnabledTargetCount",
                            R.string.HideUserAvatarEnabledTargetCount,
                            HideUserAvatar.INSTANCE.selectedUserCount,
                            HideUserAvatar.INSTANCE.selectedChannelCount,
                            HideUserAvatar.INSTANCE.selectedGroupCount
                        )
                    } else {
                        LocaleController.getString("NotEnabled", R.string.NotEnabled)
                    }
                },
                onClick = {
                    Toast.makeText(
                        context,
                        LocaleController.getString("NotImplemented", R.string.NotImplemented),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            functionSwitch(
                AntiAntiCopy, "AntiAntiCopy", R.string.AntiAntiCopy,
                descProvider = {
                    LocaleController.getString("AntiAntiCopyD", R.string.AntiAntiCopyD)
                }
            )
            functionSwitch(
                ProhibitSpoilers, "ProhibitSpoilers", R.string.ProhibitSpoilers
            )
            functionSwitch(
                HistoricalNewsOption, "HistoricalNewsOption", R.string.HistoricalNewsOption
            )
            functionSwitch(
                ProhibitChannelSwitching, "ProhibitChannelSwitching", R.string.ProhibitChannelSwitching
            )
            functionSwitch(
                ProhibitEnableReactions, "ProhibitEnableReactions", R.string.ProhibitEnableReactions
            )
            functionSwitch(
                ProhibitChatGreetings, "ProhibitChatGreetings", R.string.ProhibitChatGreetings
            )
            functionSwitch(
                HidePhoneNumber, "HidePhoneNumber", R.string.HidePhoneNumber
            )
            functionSwitch(
                AddSubItemChannel, "AddSubItemChannel", R.string.AddSubItemChannel,
                descProvider = {
                    LocaleController.getString("AddSubItemChannelD", R.string.AddSubItemChannelD)
                }
            )
            functionSwitch(
                ChannelDetailNumbers, "ChannelDetailNumbers", R.string.ChannelDetailNumbers,
                descProvider = {
                    LocaleController.getString("ChannelDetailNumbersD", R.string.ChannelDetailNumbersD)
                }
            )
            functionSwitch(
                AddInfoContainer, "AddInfoContainer", R.string.AddInfoContainer,
                descProvider = {
                    LocaleController.getString("AddInfoContainerD", R.string.AddInfoContainerD)
                }
            )
            functionSwitch(
                SendCommand, "SendCommand", R.string.SendCommand
            )
            functionSwitch(
                ForceBlurChatAvailable, "ForceBlurChatAvailable", R.string.ForceBlurChatAvailable
            )
            functionSwitch(
                DisablePremiumStickerAnimation, "DisablePremiumStickerAnimation", R.string.DisablePremiumStickerAnimation
            )
            functionSwitch(
                KeepVideoMuted, "KeepVideoMuted", R.string.KeepVideoMuted
            )
            functionSwitch(
                ViewTopicAsMsgByDefault, "ViewTopicAsMsgByDefault", R.string.ViewTopicAsMsgByDefault,
                "ViewTopicAsMsgByDefaultDesc", R.string.ViewTopicAsMsgByDefaultDesc
            )
            functionSwitch(
                HidePremiumStickerSetTab, "HidePremiumStickerSetTab", R.string.HidePremiumStickerSetTab,
                "HidePremiumStickerSetTabDesc", R.string.HidePremiumStickerSetTabDesc
            )
        }
        category("LostMsgMitigation", R.string.LostMsgMitigation) {
            functionSwitch(
                ShowMsgId.INSTANCE, "ShowMsgId", R.string.ShowMsgId
            )
            functionSwitch(
                AddReloadMsgBtn.INSTANCE,
                "AddReloadMsgBtn", R.string.AddReloadMsgBtn,
                "AddReloadMsgBtnDesc", R.string.AddReloadMsgBtnDesc
            )
            functionSwitch(
                DatabaseCorruptionWarning,
                "DatabaseCorruptionWarning", R.string.DatabaseCorruptionWarning,
                "DatabaseCorruptionWarningDesc", R.string.DatabaseCorruptionWarningDesc
            )
        }
        category("AccessHash", R.string.AccessHash) {
            functionSwitch(
                DumpGroupMember,
                "DumpGroupMember", R.string.DumpGroupMember,
                "DumpGroupMemberDesc", R.string.DumpGroupMemberDesc
            )
            functionSwitch(
                ExtendedOfflineSearch,
                "ExtendedOfflineSearch", R.string.ExtendedOfflineSearch,
                "ExtendedOfflineSearchDesc", R.string.ExtendedOfflineSearchDesc
            )
            functionSwitch(
                HistoricGroupMemberRecord,
                "HistoricGroupMemberRecord", R.string.HistoricGroupMemberRecord,
                "HistoricGroupMemberRecordDesc", R.string.HistoricGroupMemberRecordDesc
            )
        }
        category("DebugAndLogsForClient", R.string.DebugAndLogsForClient) {
            add(mBuildVarsLogSwitch)
            add(mTgnetNativeLogSwitch)
            functionSwitch(
                TgnetLogControlStartupApplyHelper,
                "DisableLogConfigOnStartup", R.string.DisableLogConfigOnStartup,
                "DisableLogConfigOnStartupDesc", R.string.DisableLogConfigOnStartupDesc
            )
        }
        category("Misc", R.string.Misc) {
            textValue("RestartClient", R.string.RestartClient) {
                // calling System.exit(0) will work, because AM will automatically restart the app
                // so we don't need to restart the app manually
                exitProcess(0)
            }
        }
        category("About", R.string.About) {
            textValue("AboutTMoe", R.string.AboutTMoe, onClick = {
                presentFragment(AboutFragment())
            })
        }
    }

    private val mTgnetNativeLogSwitch = AbstractSwitch(
        "TgnetLogsEnabled", R.string.TgnetLogsEnabled,
        isCheckedLambda = {
            TgnetLogController.getCurrentTgnetLogStatus() > 0
        },
        onCheckedChanged = {
            TgnetLogController.setCurrentTgnetLogStatus(if (it) 1 else 0)
        },
        enabledLambda = {
            TgnetLogController.getCurrentTgnetLogStatus() >= 0
        },
        descProvider = {
            val symNotFound = TgnetLogController.getCurrentTgnetLogStatus() < 0
            if (symNotFound) {
                LocaleController.getString(
                    "TgnetLogsEnabledDescSymbolNotFound",
                    R.string.TgnetLogsEnabledDescSymbolNotFound
                )
            } else {
                LocaleController.getString(
                    "TgnetLogsEnabledDesc",
                    R.string.TgnetLogsEnabledDesc
                )
            }
        }
    )

    private val mBuildVarsLogSwitch = AbstractSwitch(
        "BuildVarsLogsEnabled", R.string.BuildVarsLogsEnabled,
        isCheckedLambda = {
            TgnetLogController.getCurrentBuildVarsLogStatus() > 0
        },
        onCheckedChanged = {
            TgnetLogController.setCurrentBuildVarsLogStatus(if (it) 1 else 0)
        },
        enabledLambda = {
            TgnetLogController.getCurrentBuildVarsLogStatus() >= 0
        },
        descProvider = {
            val notFound = TgnetLogController.getCurrentBuildVarsLogStatus() < 0
            if (notFound) {
                LocaleController.getString(
                    "BuildVarsLogsEnabledDescSymbolNotFound",
                    R.string.BuildVarsLogsEnabledDescSymbolNotFound
                )
            } else {
                LocaleController.getString(
                    "BuildVarsLogsEnabledDesc",
                    R.string.BuildVarsLogsEnabledDesc
                )
            }
        }
    )

}
