package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption.getField
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption.getMethodAndInvoke
import cc.ioctl.tmoe.ui.LocaleController
import com.github.kyuubiran.ezxhelper.utils.*

object AddSubItemChannel : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {
        //打开频道
        val msg_channel = findField("org.telegram.messenger.R\$drawable") { name == "msg_channel" }.get(null) as Int
        val view_discussion = 22

        findMethod(loadClass("org.telegram.ui.ProfileActivity"), true) {
            name == "createActionBarMenu" && parameterTypes.size == 1
        }.hookAfter {

            if (!isEnabled) return@hookAfter

            val chatId = getField("chatId", it.thisObject) as Long
            if (chatId.toInt() != 0) {
                val getMessagesController =
                    getMethodAndInvoke("getMessagesController", it.thisObject, true)!!
                val chat = getMethodAndInvoke("getChat", getMessagesController, true, 1, chatId)
                val megagroup = getField("megagroup", chat, true) as Boolean


                val isChannel = findMethod("org.telegram.messenger.ChatObject") {
                    name == "isChannel" && parameterTypes.size == 1
                }.invoke(null, chat) as Boolean

                if (isChannel) {

                    if (megagroup) {
                        val chatInfo = getField("chatInfo", it.thisObject)
                        if (chatInfo != null) {
                            val linked_chat_id = getField("linked_chat_id", chatInfo, true) as Long
                            if (linked_chat_id.toInt() != 0) {
                                val otherItem = getField("otherItem", it.thisObject)
                                getMethodAndInvoke(
                                    "addSubItem", otherItem, false, 3, view_discussion, msg_channel
                                    /*R.drawable.ic_setting_hex_outline_24*/,
                                    LocaleController.getString("ViewChannel", R.string.ViewChannel)
                                )
                            }
                        }
                    }

                }


            }

        }

    }
}