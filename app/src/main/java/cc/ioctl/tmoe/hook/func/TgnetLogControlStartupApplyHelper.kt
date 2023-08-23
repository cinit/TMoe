package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.util.HookUtils
import cc.ioctl.tmoe.util.Initiator

@FunctionHookEntry
object TgnetLogControlStartupApplyHelper : CommonDynamicHook() {

    override fun initOnce(): Boolean {
        TgnetLogController.setupClientLogPreferenceForStartup()
        val method = Initiator.loadClass("org.telegram.messenger.FileLog").getDeclaredMethod("getNetworkLogPath")
        HookUtils.hookBeforeIfEnabled(this, method) { param ->
            param.result = ""
        }
        return true
    }

}
