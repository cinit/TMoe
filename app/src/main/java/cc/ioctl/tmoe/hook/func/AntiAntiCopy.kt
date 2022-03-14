package cc.ioctl.tmoe.hook.func

import android.app.AndroidAppHelper
import android.widget.Toast
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*
import java.util.*
import kotlin.collections.ArrayList

object AntiAntiCopy : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        //反反复制
        var isOF=true
        var isNoForw=false
        findAllMethods("org.telegram.messenger.MessagesController") { name == "isChatNoForwards" }.hookAfter {
            if (isEnabled) {
                isNoForw=it.result as Boolean

                if(isOF) it.result = false

            }
        }

        findMethod("org.telegram.ui.ChatActivity"){ name=="processSelectedOption" }.hookBefore {
                if (!isEnabled) return@hookBefore

                if (it.args[0]==2){
                    if (isNoForw){
                        it.result=null
                        Toast.makeText(AndroidAppHelper.currentApplication().applicationContext,"禁止复制和转发此群组的消息。", Toast.LENGTH_SHORT).show()
                    }
                }

        }

        findMethod("org.telegram.ui.ChatActivity"){ name=="openForward" }.hookMethod {
            before { isOF=false }
            after { isOF=true }
        }

    }
}