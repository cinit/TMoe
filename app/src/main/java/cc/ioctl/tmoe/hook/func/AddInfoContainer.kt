package cc.ioctl.tmoe.hook.func

import android.view.View
import android.widget.LinearLayout
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption.getField
import cc.ioctl.tmoe.hook.func.HistoricalNewsOption.getMethodAndInvoke
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge

object AddInfoContainer : CommonDynamicHook() {

    //非管理员也可查看一些信息。。。
    override fun initOnce(): Boolean = tryOrLogFalse {
        val ProfileActivity = loadClass("org.telegram.ui.ProfileActivity")
        findMethod(ProfileActivity) {
            name == "createActionBarMenu" && parameterTypes.size == 1
        }.hookAfter {
            try {

                val getMessagesController =
                    getMethodAndInvoke("getMessagesController", it.thisObject, true)!!
                val chatId = getField("chatId", it.thisObject) as Long
                val chat = getMethodAndInvoke("getChat", getMessagesController, true, 1, chatId)

                val hasAdminRights = findMethod("org.telegram.messenger.ChatObject") {
                    name == "hasAdminRights" && parameterTypes.size == 1
                }.invoke(null, chat) as Boolean

                var megagroup = getField("megagroup", chat, true) as Boolean?

                if (megagroup == null) {
                    megagroup = false
                }

                if (hasAdminRights || megagroup) {
                    val editItemVisible = findField(it.thisObject::class.java) {
                        name == "editItemVisible"
                    }.get(it.thisObject) as Boolean

                    if (editItemVisible) {
                        return@hookAfter
                    }

                    findField(it.thisObject::class.java) {
                        name == "editItemVisible"
                    }.set(it.thisObject, true)

                    //editItem.setVisibility(View.VISIBLE);

                    val editItem = findField(it.thisObject::class.java) {
                        name == "editItem"
                    }.get(it.thisObject) as View
                    editItem.visibility = View.VISIBLE

                    getMethodAndInvoke("setIcon", editItem, false, 1, R.drawable.ic_setting_hex_outline_24)
                }

            } catch (e: Throwable) {

                XposedBridge.log(e)
            }
        }


        val actions_addadmin = android.R.drawable.ic_lock_idle_alarm

        findAllMethods(loadClass("org.telegram.ui.ChatEditActivity"), true) {
            when (name) {
                "createView" -> {
                    this.hookAfter {
                        val blockCell = getField("blockCell", it.thisObject) as View
                        blockCell.visibility = View.VISIBLE

                        //infoContainer.setVisibility(View.GONE);
                        //            settingsTopSectionCell.setVisibility(View.GONE);

                        val infoContainer = getField("infoContainer", it.thisObject) as LinearLayout
                        infoContainer.visibility = View.VISIBLE

                        val settingsTopSectionCell =
                            getField("settingsTopSectionCell", it.thisObject) as View
                        settingsTopSectionCell.visibility = View.VISIBLE

                        val currentChat = getField("currentChat", it.thisObject)
                        val hasAdminRights = findMethod("org.telegram.messenger.ChatObject") {
                            name == "hasAdminRights" && parameterTypes.size == 1
                        }.invoke(null, currentChat) as Boolean

                        if (!hasAdminRights) {
                            val logCell = getField("logCell", it.thisObject) as View?
                            if (logCell != null) {
//                                    logCell.visibility = View.GONE
                                infoContainer.removeView(logCell)
                                findField(it.thisObject::class.java) {
                                    name == "logCell"
                                }.set(it.thisObject, null)
                            } else {
                                Log.e("logCell==null")
                            }

                        }

                    }

                }
                "updateFields" -> {
                    this.hookAfter {

                        val currentChat = getField("currentChat", it.thisObject)
                        val hasAdminRights = findMethod("org.telegram.messenger.ChatObject") {
                            name == "hasAdminRights" && parameterTypes.size == 1
                        }.invoke(null, currentChat) as Boolean

                        if (!hasAdminRights) {
                            val ChannelAdministrators =
                                findField("org.telegram.messenger.R\$string") { name == "ChannelAdministrators" }.get(
                                    null
                                ) as Int
                            val ChannelAdministratorsText =
                                findMethod("org.telegram.messenger.LocaleController") {
                                    name == "getString" && parameterTypes.size == 2
                                }.invoke(
                                    null,
                                    "ChannelAdministrators",
                                    ChannelAdministrators
                                ) as String

                            val adminCell = getField("adminCell", it.thisObject)
                            getMethodAndInvoke(
                                "setTextAndValueAndIcon", adminCell, false, -1,
                                ChannelAdministratorsText, "**", actions_addadmin, true
                            )


                        }


                    }
                }

            }
            false
        }


        val TYPE_BANNED = 0
        val TYPE_KICKED = 3
        findMethod(loadClass("org.telegram.ui.ChatUsersActivity")) {
            name == "createView" && parameterTypes.size == 1
        }.hookAfter {

            val type = findField(it.thisObject::class.java) {
                name == "type"
            }.get(it.thisObject) as Int


            val currentChat = findField(it.thisObject::class.java) {
                name == "currentChat"
            }.get(it.thisObject)

            val hasAdminRights = findMethod("org.telegram.messenger.ChatObject") {
                name == "hasAdminRights" && parameterTypes.size == 1
            }.invoke(null, currentChat) as Boolean


            if (!hasAdminRights && (type == TYPE_BANNED || type == TYPE_KICKED)) {


                val searchItem = findField(it.thisObject::class.java) {
                    name == "searchItem"
                }.get(it.thisObject) as View?

                val doneItem = findField(it.thisObject::class.java) {
                    name == "doneItem"
                }.get(it.thisObject) as View?

                searchItem?.visibility = View.GONE

                doneItem?.visibility = View.GONE
            }


        }

        findMethod(loadClass("org.telegram.ui.ChatUsersActivity")) {
            name == "updateRows" && parameterTypes.size == 0
        }.hookAfter {

            val currentChat = findField(it.thisObject::class.java) {
                name == "currentChat"
            }.get(it.thisObject)

            val hasAdminRights = findMethod("org.telegram.messenger.ChatObject") {
                name == "hasAdminRights" && parameterTypes.size == 1
            }.invoke(null, currentChat) as Boolean

            val isChannel = findMethod("org.telegram.messenger.ChatObject") {
                name == "isChannel" && parameterTypes.size == 1
            }.invoke(null, currentChat) as Boolean

            if (!(isChannel && hasAdminRights)) {

                findField(it.thisObject::class.java) {
                    name == "recentActionsRow"
                }.set(it.thisObject, -1)

                findField(it.thisObject::class.java) {
                    name == "addNewSectionRow"
                }.set(it.thisObject, -1)



                findField(it.thisObject::class.java) {
                    name == "participantsDivider2Row"
                }.set(it.thisObject, -1)

                findField(it.thisObject::class.java) {
                    name == "removedUsersRow"
                }.set(it.thisObject, -1)
            }


        }


    }
}