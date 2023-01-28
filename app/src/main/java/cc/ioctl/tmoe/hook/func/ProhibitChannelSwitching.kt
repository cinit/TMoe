package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*

object ProhibitChannelSwitching : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {
        val cpd= loadClass("org.telegram.ui.ChatPullingDownDrawable")
        for (method in cpd.declaredMethods) {
            when (method.name) {
                "getNextUnreadDialog",
                "drawBottomPanel",
                "draw"->{
                    method.hookBefore {
                        if (isEnabled) it.result=null
                    }
                }
                "showBottomPanel"->{
                    method.hookBefore {
                        if (isEnabled) it.args[0]=false
                    }
                }
                "needDrawBottomPanel"->{
                    method.hookBefore {
                        if (isEnabled) it.result=false
                    }
                }
            }
        }



    }
}