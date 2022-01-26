package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore

object AntiAntiForward : CommonDynamicHook() {
    override fun initOnce(): Boolean {
        findMethod("org.telegram.messenger.MessageObject") { name == "canForwardMessage" }.hookBefore {
            if (!isEnabled) return@hookBefore
            it.result = true
        }
        findAllMethods("org.telegram.messenger.MessageObject") { name == "isSecretMedia" }.hookBefore {
            if (!isEnabled) return@hookBefore
            it.result = false
        }
        findAllMethods("org.telegram.messenger.MessagesController") { name == "isChatNoForwards" }.hookBefore {
            if (!isEnabled) return@hookBefore
            it.result = false
        }
        return true
    }
}