package cc.ioctl.tmoe.hook.func

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.Shader
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*
import java.util.Arrays


object ProhibitSpoilers : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {

        findAllMethods("org.telegram.ui.Components.spoilers.SpoilerEffect"){ name=="addSpoilers" }.hookBefore {
            if (isEnabled) it.result=null
        }

        //  org.telegram.ui.Cells.ChatMessageCell.drawBlurredPhoto(ChatMessageCell.java)
        findMethod("org.telegram.messenger.MessageObject") { name == "hasMediaSpoilers" }.hookBefore {
            if (isEnabled) it.result = false
        }


    }
}