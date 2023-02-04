package cc.ioctl.tmoe.hook.func

import android.app.AndroidAppHelper
import android.widget.Toast
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.ui.LocaleController
import com.github.kyuubiran.ezxhelper.utils.*

object AntiAntiCopy : CommonDynamicHook() {
    private var isOF=true
    var isNoForw=false

    override fun initOnce(): Boolean = tryOrLogFalse {
        findAllMethods("org.telegram.messenger.MessagesController") { name == "isChatNoForwards" }.hookAfter {
            if (isEnabled) {
                isNoForw=it.result as Boolean
                if(isOF) it.result = false
            }
        }

        findMethod("org.telegram.ui.ChatActivity") { name == "processSelectedOption" }.hookBefore {
            if (!isEnabled) return@hookBefore

            if (it.args[0] == 2) {
                if (isNoForw) {
                    it.result = null
                    Toast.makeText(
                        AndroidAppHelper.currentApplication().applicationContext,
                        LocaleController.getString("ForwardsRestrictedInfoGroup", R.string.ForwardsRestrictedInfoGroup),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        findMethod("org.telegram.ui.ChatActivity"){ name=="openForward" }.hookMethod {
            before { isOF=false }
            after { isOF=true }
        }

    }
}