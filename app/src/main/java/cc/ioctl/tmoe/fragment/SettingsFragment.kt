package cc.ioctl.tmoe.fragment

import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.func.AntiAntiForward
import cc.ioctl.tmoe.hook.func.EnableDebugMode
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
        }
    }
}
