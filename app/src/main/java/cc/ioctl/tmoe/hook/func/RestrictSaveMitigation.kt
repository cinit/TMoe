package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.util.Initiator
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object RestrictSaveMitigation : CommonDynamicHook() {
    @Throws(Exception::class)
    override fun initOnce(): Boolean {
        val kMessagesController = Initiator.loadClass("org.telegram.messenger.MessagesController")
        XposedBridge.hookAllMethods(
            kMessagesController,
            "isChatNoForwards",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (isEnabledByUser) {
                        param.result = false
                    }
                }
            })
        return true
    }
}
