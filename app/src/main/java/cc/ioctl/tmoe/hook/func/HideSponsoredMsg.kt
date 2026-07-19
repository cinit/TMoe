package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadAndFindMethods
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.loadClass

/**
 * From `https://github.com/shatyuka/Killergram`
 */
@FunctionHookEntry
object HideSponsoredMsg : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {
        arrayOf(
            "org.telegram.messenger.MessagesController",
            "org.telegram.ui.ChatActivity"
        ).loadAndFindMethods {
            name.contains("SponsoredMessages")
        }.hookBefore { if (isEnabled) it.result = null }
        findMethod(loadClass("org.telegram.tgnet.TLRPC\$messages_SponsoredMessages")) {
            name == "TLdeserialize"
        }.hookBefore { if (isEnabled) it.result = null }
    }
}
