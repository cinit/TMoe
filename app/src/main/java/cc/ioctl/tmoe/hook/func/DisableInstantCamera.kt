package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse

@FunctionHookEntry
object DisableInstantCamera : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {
        findMethod(loadClass("org.telegram.ui.Components.ChatActivityEnterView")) { name == "isInVideoMode" }.hookBefore {
            if (isEnabled) it.result = false
        }

        findMethod(loadClass("org.telegram.ui.Components.ChatActivityEnterView")) { name == "hasRecordVideo" }.hookBefore {
            if (isEnabled) it.result = false
        }
    }
}