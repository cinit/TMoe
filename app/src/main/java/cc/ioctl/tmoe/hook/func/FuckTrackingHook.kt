package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReturnConstant
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse

@FunctionHookEntry
object FuckTrackingHook : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {
        if (!isEnabled) return@tryOrLogFalse

        findMethod(loadClass("org.telegram.ui.ChatActivity")) {
            name == "logSponsoredClicked" &&
                    parameterTypes.size == 1
        }.hookReturnConstant(null)
    }
}