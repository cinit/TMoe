package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.tryOrFalse

@FunctionHookEntry
object KeepVideoMuted : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        findMethod(loadClass("org.telegram.ui.ChatActivity")) {
            name == "maybePlayVisibleVideo"
        }.hookBefore {
            if (!isEnabled) return@hookBefore
            it.result = false
        }
    }
}
