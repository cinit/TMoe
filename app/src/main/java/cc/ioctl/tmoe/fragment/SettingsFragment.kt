package cc.ioctl.tmoe.fragment

import android.widget.Toast
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.func.*
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.BaseHierarchyFragment
import cc.ioctl.tmoe.ui.dsl.HierarchyDescription

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
                AntiAntiForward, "AntiAntiForward", R.string.AntiAntiForward
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
                AntiAntiCopy, "AntiAntiCopy",  R.string.AntiAntiCopy,
                descProvider ={
                    LocaleController.getString("AntiAntiCopyD", R.string.AntiAntiCopyD)
                }
            )
            functionSwitch(
                ProhibitChannelSwitching, "ProhibitChannelSwitching", R.string.ProhibitChannelSwitching
            )
            functionSwitch(
                ProhibitEnableReactions, "ProhibitEnableReactions",  R.string.ProhibitEnableReactions
            )

        }
        category("About", R.string.About) {
            textValue("AboutTMoe", R.string.AboutTMoe, onClick = {
                presentFragment(AboutFragment())
            })
        }
    }
}
