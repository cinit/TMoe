package cc.ioctl.tmoe.hook.func

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption.getMethodAndInvoke
import cc.ioctl.tmoe.lifecycle.Parasitics
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse
import de.robv.android.xposed.XposedBridge

object SendCommand : CommonDynamicHook() {

    override fun initOnce(): Boolean = tryOrLogFalse {
        findMethod(loadClass("org.telegram.ui.Components.ChatActivityEnterView")) {
            //public void setCommand(MessageObject messageObject, String command, boolean longPress, boolean username)
            name=="setCommand"&&parameterTypes.size==4
        }.hookBefore {
            if (!isEnabled)return@hookBefore

            try {

//            val messageObject =it.args[0]
            val command=it.args[1] as String
            val longPress=it.args[2] as Boolean
//            val username=it.args[3] as Boolean

            if (longPress)return@hookBefore


            val context = getMethodAndInvoke("getContext", it.thisObject, true, 0) as Context
            if (command.startsWith("/")){
                Parasitics.injectModuleResources(context.resources)
                AlertDialog.Builder(context)
                    .setMessage(R.string.Dialog_Message_ConfirmClickToSendBotCommand)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                      XposedBridge.invokeOriginalMethod(it.method,it.thisObject,it.args)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

                it.result=null
            }
        } catch (e: Throwable) {
            XposedBridge.log(e)
        }

        }

    }
}