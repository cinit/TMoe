package cc.ioctl.tmoe.hook.func

import android.app.AndroidAppHelper
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption.getField
import com.github.kyuubiran.ezxhelper.utils.*
import java.lang.reflect.Method

//https://github.com/DrKLO/Telegram/blob/master/TMessagesProj/src/main/java/org/telegram/ui/Components/ChatGreetingsView.java
object ProhibitChatGreetings : CommonDynamicHook() {

    override fun initOnce(): Boolean = tryOrFalse {

        val cgv= loadClass("org.telegram.ui.Components.ChatGreetingsView")
        for (method in cgv.declaredMethods) {
            when (method.name) {
                "onMeasure"-> onMeasure(method)
                "setListener",
                "setSticker",
                "fetchSticker"->{
                    method.hookBefore {
                        if (!isEnabled) return@hookBefore
                        it.result=null
                    }
                }


            }
        }
    }

    private fun onMeasure(method: Method) {
        method.hookAfter {
            if (!isEnabled) return@hookAfter

            val stickerToSendView = getField("stickerToSendView", it.thisObject) as View
            val descriptionView = getField("descriptionView", it.thisObject) as View
            stickerToSendView.visibility = View.GONE
            descriptionView.visibility = View.GONE

            val chatGreetingsView=it.thisObject as LinearLayout
            chatGreetingsView.background=null

//            findMethod(it.thisObject::class.java,true){
//                name=="setBackground"&&parameterTypes.size==1
//            }.invoke(it.thisObject,null)

        }
    }
}