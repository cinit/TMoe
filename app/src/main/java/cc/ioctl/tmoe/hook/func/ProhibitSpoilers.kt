package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*


object ProhibitSpoilers : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {

        findAllMethods("org.telegram.ui.Components.spoilers.SpoilerEffect"){ name=="addSpoilers" }.hookBefore {
            if (isEnabled) it.result=null
        }

        //  org.telegram.ui.Cells.ChatMessageCell.drawBlurredPhoto(ChatMessageCell.java)
        findMethod("org.telegram.messenger.MessageObject") { name == "hasMediaSpoilers" }.hookBefore {
            if (isEnabled) it.result = false
        }


    }
}