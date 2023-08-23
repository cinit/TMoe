package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*

@FunctionHookEntry
object ForceBlurChatAvailable : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        // 强制显示顶栏模糊选项
        findMethod(loadClass("org.telegram.messenger.SharedConfig")){
            name=="canBlurChat"
        }.hookBefore {
            if (!isEnabled)return@hookBefore

            it.result = true
        }
    }
}