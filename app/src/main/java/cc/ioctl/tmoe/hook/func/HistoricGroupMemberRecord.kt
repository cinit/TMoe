package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.td.AccountController
import cc.ioctl.tmoe.td.RequestInterceptor
import cc.ioctl.tmoe.util.HookUtils
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.Log
import cc.ioctl.tmoe.util.PrimTypes
import cc.ioctl.tmoe.util.Reflex
import java.lang.reflect.Field
import kotlin.math.abs

@FunctionHookEntry
object HistoricGroupMemberRecord : CommonDynamicHook() {

    private lateinit var kRequestDelegate: Class<*>
    private lateinit var kTLObject: Class<*>
    private lateinit var kTL_error: Class<*>
    private lateinit var kChat: Class<*>
    private lateinit var fChat_id: Field
    private lateinit var fChat_title: Field
    private lateinit var fChat_flags: Field
    private lateinit var fChat_access_hash: Field
    private lateinit var fChat_username: Field
    private lateinit var kInputUser: Class<*>
    private lateinit var fInputUser_user_id: Field
    private lateinit var fInputUser_access_hash: Field
    private lateinit var kmessages_Chats: Class<*>
    private lateinit var fmessages_Chats_chats: Field
    private lateinit var fmessages_Chats_count: Field
    private lateinit var kTL_messages_getCommonChats: Class<*>
    private lateinit var fTL_messages_getCommonChats_user_id: Field

    override fun initOnce(): Boolean {
        kRequestDelegate = Initiator.loadClass("org.telegram.tgnet.RequestDelegate")
        kTLObject = Initiator.loadClass("org.telegram.tgnet.TLObject")
        kTL_error = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_error")
        kChat = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Chat")
        fChat_id = Reflex.findField(kChat, java.lang.Long.TYPE, "id")
        fChat_title = Reflex.findField(kChat, String::class.java, "title")
        fChat_flags = Reflex.findField(kChat, Integer.TYPE, "flags")
        fChat_access_hash = Reflex.findField(kChat, java.lang.Long.TYPE, "access_hash")
        fChat_username = Reflex.findField(kChat, String::class.java, "username")
        kInputUser = Initiator.loadClass("org.telegram.tgnet.TLRPC\$InputUser")
        fInputUser_user_id = Reflex.findField(kInputUser, java.lang.Long.TYPE, "user_id")
        fInputUser_access_hash = Reflex.findField(kInputUser, java.lang.Long.TYPE, "access_hash")
        kmessages_Chats = Initiator.loadClass("org.telegram.tgnet.TLRPC\$messages_Chats")
        fmessages_Chats_chats = Reflex.findField(kmessages_Chats, ArrayList::class.java, "chats")
        fmessages_Chats_count = Reflex.findField(kmessages_Chats, Integer.TYPE, "count")
        kTL_messages_getCommonChats = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_messages_getCommonChats")
        fTL_messages_getCommonChats_user_id = Reflex.findField(kTL_messages_getCommonChats, kInputUser, "user_id")

        // test linkage
        val currentSlot = AccountController.getCurrentActiveSlot()
        check(currentSlot >= 0) { "AccountController.getCurrentActiveSlot fail" }

        RequestInterceptor.registerTlrpcSuccessfulResultInterceptor(
            Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_messages_getCommonChats")
        ) { req, resp ->
            if (!isEnabled) {
                return@registerTlrpcSuccessfulResultInterceptor null
            }
            handleRequestComplete(req, resp)
            null
        }

        // handle cases that common_chats_count is 0 but local database is not empty
        val kSharedMediaLayout = Initiator.loadClass("org.telegram.ui.Components.SharedMediaLayout");
        val fSharedMediaLayout_dialog_id = Reflex.findField(kSharedMediaLayout, PrimTypes.LONG, "dialog_id").apply {
            isAccessible = true
        }
        val updateTabs = kSharedMediaLayout.getDeclaredMethod("updateTabs", PrimTypes.BOOLEAN)
        val hasMediaField = kSharedMediaLayout.getDeclaredField("hasMedia").apply {
            isAccessible = true
        }
        HookUtils.hookBeforeIfEnabled(this, updateTabs) { param ->
            val dialogId = fSharedMediaLayout_dialog_id.getLong(param.thisObject)
            if (dialogId <= 0) {
                return@hookBeforeIfEnabled
            }
            if (!Reflex.isCallingFromMethod("setCommonGroupsCount", 10)) {
                return@hookBeforeIfEnabled
            }
            val hasMedia = hasMediaField.get(param.thisObject) as IntArray
            val currentCount = hasMedia[6]
            if (currentCount == 0) {
                // query local database
                val groupInfoList = DumpGroupMember.queryUserGroupDescriptors(dialogId)
                if (groupInfoList.isNotEmpty()) {
                    // 1 is enough for a TLRPC.TL_messages_getCommonChats request
                    hasMedia[6] = 1
                }
            }
        }
        return true
    }

    private fun handleRequestComplete(request: Any, response: Any) {
        val reqType = request.javaClass
        when (reqType) {
            kTL_messages_getCommonChats -> {
                if (kmessages_Chats.isInstance(response)) {
                    val uid = fInputUser_user_id.getLong(fTL_messages_getCommonChats_user_id.get(request))
                    if (uid > 0) {
                        injectExtraGroup(uid, response)
                    }
                } else if (!kTL_error.isInstance(response)) {
                    Log.e("unknown response for TL_messages_getCommonChats: " + response.javaClass.name)
                }
            }
        }

    }

    private fun injectExtraGroup(uid: Long, response: Any) {
        val extraGroupDescriptors = DumpGroupMember.queryUserGroupDescriptors(uid).toMutableSet()
        val originalChats = fmessages_Chats_chats.get(response) as ArrayList<Any>
        val originCount = originalChats.size
        // remove duplicate groups
        originalChats.forEach { chat ->
            val chatId = fChat_id.getLong(chat)
            val gid = abs(chatId)
            extraGroupDescriptors.removeIf { it.uid == gid }
        }
        // inject extra groups
        if (extraGroupDescriptors.isEmpty()) {
            return
        }
        extraGroupDescriptors.forEach { desc ->
            val cachedChat = DumpGroupMember.getChatFormCache(desc.uid)
            // inject
            // Log.d("inject group: $desc, cachedChat = $cachedChat")
            originalChats.add(cachedChat ?: DumpGroupMember.createMinimalChannelChat(desc))
        }
        // update count
        fmessages_Chats_count.set(response, originalChats.size)
        // Log.d("inject extra groups: originCount = $originCount, newCount = ${originalChats.size}")
    }

}
