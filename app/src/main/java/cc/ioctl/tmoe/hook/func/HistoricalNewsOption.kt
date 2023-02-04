package cc.ioctl.tmoe.hook.func

import android.app.AndroidAppHelper
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.lifecycle.Parasitics
import cc.ioctl.tmoe.ui.LocaleController
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap


//TODO 分离 复读
object HistoricalNewsOption : CommonDynamicHook() {
    var chatActivity: Any? = null
    override fun initOnce(): Boolean = tryOrLogFalse {

        try {


            var isCreateMenu = false
            findMethod("org.telegram.ui.ChatActivity") { name == "createMenu" }.hookMethod {
                before {
                    chatActivity = it.thisObject
                    isCreateMenu = true
                }
                after { isCreateMenu = false }
            }

            //LocaleController.getString("ReportChat", R.string.ReportChat);
            val reportChat =
                findField("org.telegram.messenger.R\$string") { name == "ReportChat" }.get(null) as Int
            val reportChatText = findMethod("org.telegram.messenger.LocaleController") {
                name == "getString" && parameterTypes.size == 2
            }.invoke(null, "ReportChat", reportChat) as String

            val callC =
                findConstructor("org.telegram.ui.ActionBar.ActionBarMenuSubItem") { parameterTypes.size == 4 }

            findMethod("org.telegram.ui.ActionBar.ActionBarPopupWindow\$ActionBarPopupWindowLayout") {
                name == "addView" && parameterTypes.size == 1
            }.hookBefore {
                if (!isEnabled) return@hookBefore


                if (it.args[0]::class.java.canonicalName == "org.telegram.ui.ActionBar.ActionBarMenuSubItem") {

                    if (!isCreateMenu) return@hookBefore

                    val texts = getField("textView", it.args[0]) as TextView


                    val thisObject = it.thisObject

                    if (texts.text == reportChatText) {
                        it.result = null

                        // ActionBarMenuSubItem cell = new ActionBarMenuSubItem(getParentActivity(), true, true, themeDelegate);
                        val ctx = (thisObject as ViewGroup).context
                        val themeDelegate = getField("resourcesProvider", thisObject)

                        Parasitics.injectModuleResources(ctx.resources)
                        val call = callC.newInstance(ctx, true, true, themeDelegate)
                        getMethodAndInvoke(
                            "setTextAndIcon",
                            call,
                            false,
                            2,
                            LocaleController.getString(
                                "MenuItem_HistoryMessage",
                                R.string.MenuItem_HistoryMessage
                            ),
                            R.drawable.ic_setting_hex_outline_24
                        )
                        XposedBridge.invokeOriginalMethod(it.method, thisObject, arrayOf(call))
                        (call as FrameLayout).setOnClickListener {
                            findUserHistory()
                        }

                        call.setOnLongClickListener { view ->

                            var id = -1L
                            val selectedObject = getField("selectedObject", chatActivity)
                            val messageOwner = getField("messageOwner", selectedObject)
                            val from_id = getField("from_id", messageOwner, true)

                            val user_id = getField("user_id", from_id, true) as Long
                            val channel_id = getField("channel_id", from_id, true) as Long

                            if (user_id.toInt() != 0) {
                                id = user_id
                            } else if (channel_id.toInt() != 0) {
                                id = channel_id
                            }


                            Toast.makeText(ctx, "ID: $id", Toast.LENGTH_SHORT).show()


                            findMethod("org.telegram.messenger.AndroidUtilities") {
                                name == "addToClipboard" && parameterTypes.size == 1
                            }.invoke(null, id.toString())

//                        Toast.makeText(AndroidAppHelper.currentApplication().applicationContext,"查看用户历史消息 $channel_id $post_author  $chat_id $user_id", Toast.LENGTH_SHORT).show()
                            getMethodAndInvoke(
                                "processSelectedOption",
                                chatActivity,
                                args = arrayOf(999)
                            )
                            true
                        }

                        val plus = callC.newInstance(ctx, true, true, themeDelegate)
                        getMethodAndInvoke(
                            "setTextAndIcon",
                            plus,
                            false,
                            2,
                            LocaleController.getString(
                                "MenuItem_RepeatMessage",
                                R.string.MenuItem_RepeatMessage
                            ),
                            R.drawable.ic_setting_hex_outline_24
                        )
                        XposedBridge.invokeOriginalMethod(it.method, it.thisObject, arrayOf(plus))
                        (plus as FrameLayout).setOnClickListener {

                            try {
                                plusOne()
                            } catch (e: Throwable) {
                                XposedBridge.log(e)
                            }

                        }


                    }

                }

            }

        } catch (e: Throwable) {
            XposedBridge.log(e)
        }
    }


    private fun plusOne() {
        val currentChat = getField("currentChat", chatActivity)
        val selectedObject = getField("selectedObject", chatActivity)
        val selectedObjectGroup = getField("selectedObjectGroup", chatActivity)
        val messageOwner = getField("messageOwner", selectedObject)
        val getMessagesController =
            getMethodAndInvoke("getMessagesController", chatActivity, true)!!

//        val isChatNoForwards= getMethodAndInvoke("isChatNoForwards",getMessagesController,false,1,currentChat) as Boolean
        val isChatNoForwards = findMethod(getMessagesController::class.java) {
            name == "isChatNoForwards" && parameterTypes.size == 1 && parameterTypes[0] != Long::class.javaPrimitiveType
        }.invoke(getMessagesController, currentChat) as Boolean


        val isThreadChat = getMethodAndInvoke("isThreadChat", chatActivity, false)!! as Boolean

        if (isThreadChat || isChatNoForwards || AntiAntiCopy.isNoForw) {
            Toast.makeText(
                AndroidAppHelper.currentApplication().applicationContext,
                LocaleController.getString("UnsupportedOperation", R.string.UnsupportedOperation),
                Toast.LENGTH_SHORT
            ).show()

            if (selectedObject != null) {
                val isAnyKindOfSticker =
                    getMethodAndInvoke("isAnyKindOfSticker", selectedObject, true) as Boolean
                val isAnimatedEmoji =
                    getMethodAndInvoke("isAnimatedEmoji", selectedObject, true) as Boolean
                val isDice = getMethodAndInvoke("isDice", selectedObject, true) as Boolean

                val dialog_id = getField("dialog_id", chatActivity)!!
                val threadMessageObject = getField("threadMessageObject", chatActivity)
                val getDocument = getMethodAndInvoke("getDocument", selectedObject, false, 0)
                val getSendMessagesHelper =
                    getMethodAndInvoke("getSendMessagesHelper", chatActivity, true)!!

                if (isAnyKindOfSticker && !isAnimatedEmoji && !isDice) {

                    getMethodAndInvoke(
                        "sendSticker", getSendMessagesHelper, true, 9,
                        getDocument,
                        null,
                        dialog_id,
                        threadMessageObject,
                        threadMessageObject,
                        null,
                        null,
                        true,
                        0
                    )

                    getMethodAndInvoke("processSelectedOption", chatActivity, args = arrayOf(999))
                    chatActivity = null
                } else {
//                    messageOwner = getField("messageOwner", selectedObject)
                    val message = getField("message", messageOwner, true) as String?
//                    var message: String = messageObject.messageOwner.message

                    if (!TextUtils.isEmpty(message)) {
//                        val entities: ArrayList<TLRPC.MessageEntity>?
                        val entities: ArrayList<Any?>?
                        val entities1 =
                            getField("entities", messageOwner, true) as Collection<Any?>?
                        if (entities1 != null && !entities1.isEmpty()) {
                            entities = ArrayList()
                            for (entity in entities1) {


                                val TLEM =
                                    loadClass("org.telegram.tgnet.TLRPC\$TL_messageEntityMentionName")//TL_inputMessageEntityMentionName
                                if (TLEM.isInstance(entity)) {

                                    val TMEM =
                                        loadClass("org.telegram.tgnet.TLRPC\$TL_inputMessageEntityMentionName")//TL_inputMessageEntityMentionName
                                    val mention = TMEM.newInstance()


                                    val oldLength = getField("length", entity, true)
                                    val oldOffset = getField("offset", entity, true)

                                    findField(mention::class.java, true) {
                                        name == "length"
                                    }.set(mention, oldLength)

                                    findField(mention::class.java, true) {
                                        name == "offset"
                                    }.set(mention, oldOffset)

                                    val olduser_id = getField("user_id", entity, true)
                                    val getInputUser =
                                        findMethod(getMessagesController::class.java) {
                                            name == "getInputUser" && parameterTypes.size == 1 && parameterTypes[0] == Long::class.javaPrimitiveType
                                        }.invoke(getMessagesController, olduser_id)

                                    findField(mention::class.java, true) {
                                        name == "user_id"
                                    }.set(mention, getInputUser)
                                    entities.add(mention)
                                } else {
                                    entities.add(entity)
                                }
                            }
                        } else {
                            entities = null
                        }

                        getMethodAndInvoke(
                            "sendMessage", getSendMessagesHelper, false, 12,
                            message,
                            dialog_id,
                            threadMessageObject,
                            threadMessageObject,
                            null,
                            false,
                            entities,
                            null,
                            null,
                            true,
                            0,
                            null
                        )


                        if (chatActivity != null) {
                            getMethodAndInvoke(
                                "processSelectedOption",
                                chatActivity,
                                args = arrayOf(999)
                            )
                            chatActivity = null
                        }

                    }
                }
            }

        } else {
            val messages1 = getField("messages", selectedObjectGroup)
//            val messages: ArrayList<MessageObject> = ArrayList()
            val messages: ArrayList<Any?> = ArrayList()
            if (selectedObjectGroup != null) {
                messages.addAll(messages1 as Collection<Any?>)
            } else {
                messages.add(selectedObject!!)
            }

            //            forwardMessages(messages, false, false, true, 0)
            getMethodAndInvoke(
                "forwardMessages",
                chatActivity,
                false,
                5,
                messages,
                false,
                false,
                true,
                0
            )

        }

        if (chatActivity != null) {
            getMethodAndInvoke("processSelectedOption", chatActivity, args = arrayOf(999))
            chatActivity = null
        }

    }


    private fun findUserHistory() {
        try {
            // TLRPC.Peer peer = selectedObject.messageOwner.from_id;
            val selectedObject = getField("selectedObject", chatActivity)
            val messageOwner = getField("messageOwner", selectedObject)
            val from_id = getField("from_id", messageOwner, true)


            getMethodAndInvoke("openSearchWithText", chatActivity, args = arrayOf(""))

            val getMessagesController =
                getMethodAndInvoke("getMessagesController", chatActivity, true)!!
            val user_id = getField("user_id", from_id, true) as Long
            val chat_id = getField("chat_id", from_id, true) as Long
            val channel_id = getField("channel_id", from_id, true) as Long
            when {
                user_id.toInt() != 0 -> {
                    //                    TLRPC.User user = getMessagesController().getUser(peer.user_id);
                    //                    searchUserMessages(user, null);
                    val user =
                        getMethodAndInvoke("getUser", getMessagesController, true, 1, user_id)!!
                    getMethodAndInvoke(
                        "searchUserMessages",
                        chatActivity,
                        args = arrayOf(user, null)
                    )

                }
                chat_id.toInt() != 0 -> {
                    val chat =
                        getMethodAndInvoke("getChat", getMessagesController, true, 1, chat_id)!!
                    getMethodAndInvoke(
                        "searchUserMessages",
                        chatActivity,
                        args = arrayOf(null, chat)
                    )
                }
                channel_id.toInt() != 0 -> {
                    val chat =
                        getMethodAndInvoke("getChat", getMessagesController, true, 1, channel_id)!!
                    getMethodAndInvoke(
                        "searchUserMessages",
                        chatActivity,
                        args = arrayOf(null, chat)
                    )
                }
            }

            getMethodAndInvoke("showMessagesSearchListView", chatActivity, args = arrayOf(true))
            getMethodAndInvoke("processSelectedOption", chatActivity, args = arrayOf(999))

            chatActivity = null
        } catch (e: Throwable) {
            XposedBridge.log(e)
        }
    }

    private val mField: MutableMap<String, Field> = ConcurrentHashMap()//HashMap()
    fun getField(
        n: String,
        o: Any?,
        findSuper: Boolean = false,
        clzName: String = ""
    ): Any? {
        var name1 = ""
        if (o != null) {
            name1 = o::class.java.canonicalName!!
        }
        if (clzName != "") {
            name1 = clzName
        }

        if (mField.containsKey(n + name1)) {
            return mField[n + name1]?.get(o)
        }

        if (o != null) {
            val mF = findField(o::class.java, findSuper) { name == n }
            mField[n + name1] = mF
            return mF.get(o)
        }

        if (clzName != "") {
            val mF = findField(clzName, InitFields.ezXClassLoader, findSuper) { name == n }
            mField[n + name1] = mF
            return mF.get(o)
        }

        return null
    }

    private val mMethod: MutableMap<Any, Method> = ConcurrentHashMap()
    fun getMethodAndInvoke(
        n: String,
        obj: Any?,
        findSuper: Boolean = false,
        parameterSize: Int = -1,
        vararg args: Any?
    ): Any? {

        var name1 = ""
        if (obj != null) {
            val na = obj::class.java.canonicalName
            if (na != null) {
                name1 = na
            }
        }


        if (mMethod.containsKey(n + name1)) {
            return mMethod[n + name1]!!.invoke(obj, *args)
        }


        if (obj != null) {
            val mM = findMethod(obj::class.java, findSuper) {
                if (name == n) {
                    if (parameterSize != -1) return@findMethod parameterTypes.size == parameterSize

                    return@findMethod true
                }
                false
            }
            mMethod[n + name1] = mM
            return mM.invoke(obj, *args)
        }


        return null
    }


}