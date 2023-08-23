package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.tryOrFalse

@FunctionHookEntry
object AntiAntiForward : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        findMethod("org.telegram.messenger.MessageObject") { name == "canForwardMessage" }.hookBefore {
            if (isEnabled) it.result = true
        }
        findAllMethods("org.telegram.messenger.MessageObject") { name == "isSecretMedia" }.hookBefore {
            if (isEnabled) it.result = false
        }
        findAllMethods("org.telegram.messenger.MessagesController") { name == "isChatNoForwards" }.hookBefore {
            if (isEnabled) it.result = false
        }
    }
}