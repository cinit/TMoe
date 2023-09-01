package cc.ioctl.tmoe.hook.func

import androidx.collection.LongSparseArray
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.td.AccountController
import cc.ioctl.tmoe.util.HookUtils
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.Reflex
import cc.ioctl.tmoe.util.SyncUtils
import com.github.kyuubiran.ezxhelper.utils.Log
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.abs

@FunctionHookEntry
object LocalGroupMemberList : CommonDynamicHook() {


    private lateinit var kMentionsAdapter: Class<*>
    private lateinit var kChatFull: Class<*>
    private lateinit var kChat: Class<*>
    private lateinit var kTL_chatAdminRights: Class<*>
    private lateinit var fChat_admin_rights: Field
    private lateinit var fMentionsAdapter_info: Field
    private lateinit var fMentionsAdapter_chat: Field
    private lateinit var fMentionsAdapter_parentFragment: Field
    private lateinit var fMentionsAdapter_resultStartPosition: Field
    private lateinit var fMentionsAdapter_resultLength: Field
    private lateinit var fChatFull_id: Field
    private lateinit var fChatFull_participants_hidden: Field
    private lateinit var fChatFull_participants: Field
    private lateinit var kChatParticipants: Class<*>
    private lateinit var fChatParticipants_self_participant: Field
    private lateinit var kChatParticipant: Class<*>
    private lateinit var kTL_chatParticipantAdmin: Class<*>
    private lateinit var kTL_chatParticipantCreator: Class<*>
    private lateinit var kChatActivity: Class<*>
    private lateinit var mChatActivity_getCurrentChat: Method

    override fun initOnce(): Boolean {
        kMentionsAdapter = Initiator.loadClass("org.telegram.ui.Adapters.MentionsAdapter")
        kChatFull = Initiator.loadClass("org.telegram.tgnet.TLRPC\$ChatFull")
        kChat = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Chat")
        kChatActivity = Initiator.loadClass("org.telegram.ui.ChatActivity")
        fMentionsAdapter_info = Reflex.findField(kMentionsAdapter, kChatFull, "info").also {
            it.isAccessible = true
        }
        fMentionsAdapter_chat = Reflex.findField(kMentionsAdapter, kChat, "chat").also {
            it.isAccessible = true
        }
        fMentionsAdapter_parentFragment = Reflex.findField(kMentionsAdapter, kChatActivity, "parentFragment").also {
            it.isAccessible = true
        }
        fMentionsAdapter_resultStartPosition = Reflex.findField(kMentionsAdapter, Integer.TYPE, "resultStartPosition").also {
            it.isAccessible = true
        }
        fMentionsAdapter_resultLength = Reflex.findField(kMentionsAdapter, Integer.TYPE, "resultLength").also {
            it.isAccessible = true
        }
        mChatActivity_getCurrentChat = kChatActivity.declaredMethods.single {
            it.name == "getCurrentChat" && it.parameterTypes.isEmpty()
        }
        kTL_chatAdminRights = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_chatAdminRights")
        fChat_admin_rights = Reflex.findField(kChat, kTL_chatAdminRights, "admin_rights")
        kChatParticipants = Initiator.loadClass("org.telegram.tgnet.TLRPC\$ChatParticipants")
        kChatParticipant = Initiator.loadClass("org.telegram.tgnet.TLRPC\$ChatParticipant")
        fChatParticipants_self_participant = Reflex.findField(kChatParticipants, kChatParticipant, "self_participant")
        fChatFull_id = Reflex.findField(kChatFull, Long::class.java, "id")
        fChatFull_participants_hidden = Reflex.findField(kChatFull, Boolean::class.java, "participants_hidden")
        fChatFull_participants = Reflex.findField(kChatFull, kChatParticipants, "participants")
        kTL_chatParticipantAdmin = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_chatParticipantAdmin")
        kTL_chatParticipantCreator = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_chatParticipantCreator")

        // org.telegram.ui.Adapters.MentionsAdapter#searchUsernameOrHashtag
        // public void searchUsernameOrHashtag(CharSequence charSequence, int position, ArrayList<MessageObject> messageObjects, boolean usernameOnly, boolean forSearch)
        val searchUsernameOrHashtag = kMentionsAdapter.declaredMethods.single {
            it.name == "searchUsernameOrHashtag" && it.parameterTypes.size == 5
        }

        val showUsersResult = kMentionsAdapter.declaredMethods.single {
            it.name == "showUsersResult" && it.parameterTypes.size == 3 && it.parameterTypes[0] == java.util.ArrayList::class.java
        }.also {
            it.isAccessible = true
        }

        // check linkage
        shadowCastToHostLongSparseArray(LongSparseArray<Any>())

        HookUtils.hookBeforeIfEnabled(this, searchUsernameOrHashtag) { param ->
            val inputEditable = param.args[0] as CharSequence?
            val position = param.args[1] as Int
            val messageObjects = param.args[2] as ArrayList<*>?
            val usernameOnly = param.args[3] as Boolean
            val forSearch = param.args[4] as Boolean
            val inputText = inputEditable?.toString()
            if (inputText.isNullOrEmpty()) {
                // clear result
                return@hookBeforeIfEnabled
            }
            val selfUid = AccountController.getCurrentActiveUserId()
            val chatFull = fMentionsAdapter_info.get(param.thisObject) ?: return@hookBeforeIfEnabled
            val gid = fChatFull_id.getLong(chatFull)
            val participantsHidden = fChatFull_participants_hidden.getBoolean(chatFull)
            val chatActivity: Any? = fMentionsAdapter_parentFragment.get(param.thisObject)
            var chat = if (chatActivity != null) {
                mChatActivity_getCurrentChat.invoke(chatActivity)
            } else {
                fMentionsAdapter_chat.get(param.thisObject)
            }
            if (chat == null) {
                chat = DumpGroupMember.getChatFormCache(gid)
            }
            if (chat == null) {
                Log.w("searchUsernameOrHashtag: chat is null")
                return@hookBeforeIfEnabled
            }
            val isSelfAdmin = isSelfAdminInChat(chat)
            val userSearchKeyword: String
            var resultStartPosition = 0
            var resultLength = 0
            if (usernameOnly) {
                resultStartPosition = 0
                resultLength = inputText.length - 1
            }
            if (forSearch) {
                // remove leading '@'
                userSearchKeyword = if (inputText.startsWith("@")) {
                    inputText.substring(1)
                } else {
                    inputText
                }
            } else {
                // check whether the user is trying to @ something
                if (position < 0 || position > inputText.length) {
                    return@hookBeforeIfEnabled
                }
                // start from current position, find the first '@' backward
                val atPosition = inputText.lastIndexOf('@', position)
                // if not found, return
                if (atPosition < 0) {
                    return@hookBeforeIfEnabled
                }
                val regionStart = atPosition + 1
                val regionEnd = position
                if (regionStart > regionEnd) {
                    return@hookBeforeIfEnabled
                }
                resultStartPosition = atPosition
                resultLength = regionEnd - atPosition
                if (regionStart == regionEnd) {
                    userSearchKeyword = ""
                } else {
                    val region = inputText.substring(regionStart, regionEnd)
                    val isNickName = region.all { isCharLikeLyToBeNickName(it) }
                    if (isNickName) {
                        userSearchKeyword = region
                    } else {
                        return@hookBeforeIfEnabled
                    }
                }
            }
            var isNeedIntercept = participantsHidden && !isSelfAdmin
            val resultArray = ArrayList<DumpGroupMember.ITLIdObjectDescriptor>()
            val resultIds = HashSet<Long>() // for deduplication check
            var isExplicitUidSearch = false
            // check whether the user is attempting to @ someone by it's id
            val maybeUid: Long = userSearchKeyword.toLongOrNull() ?: 0
            if (maybeUid != 0L) {
                // maybe, convert TDLib id to Telegram id
                val uid = abs(maybeUid) % 1000000000000L
                val user = DumpGroupMember.queryUserInfoById(uid)
                if (user != null) {
                    resultArray.add(DumpGroupMember.UserDescriptorWithTime(user, System.currentTimeMillis() / 1000L))
                    resultIds.add(user.uid)
                    isNeedIntercept = true
                    isExplicitUidSearch = true
                }
                // also try search channel, since there are anonymous channels
                val channel = DumpGroupMember.queryChannelInfoById(uid)
                if (channel != null) {
                    resultArray.add(channel)
                    resultIds.add(channel.uid)
                    isNeedIntercept = true
                    isExplicitUidSearch = true
                }
            }
            if (!isNeedIntercept) {
                // let the original method handle it
                return@hookBeforeIfEnabled
            }
            if (!isExplicitUidSearch) {
                if (userSearchKeyword.isEmpty()) {
                    // just add some random users here... to make the result looks good...
                    val rs = DumpGroupMember.selectInaccurateRecentGroupMember(gid, null, 100)
                    for (r in rs) {
                        if (resultIds.contains(r.uid)) {
                            continue
                        }
                        if (r.uid == selfUid) {
                            continue
                        }
                        resultArray.add(r)
                        resultIds.add(r.uid)
                    }
                    if (resultIds.size < 49) {
                        // we want more?
                        val rs2 = DumpGroupMember.queryUserInChannel(gid, null, 100)
                        for (r in rs2) {
                            if (resultIds.contains(r.uid)) {
                                continue
                            }
                            if (r.uid == selfUid) {
                                continue
                            }
                            resultArray.add(DumpGroupMember.UserDescriptorWithTime(r, 0))
                            resultIds.add(r.uid)
                        }
                    }
                } else {
                    val rs = DumpGroupMember.selectInaccurateRecentGroupMember(gid, userSearchKeyword, 100)
                    for (r in rs) {
                        if (resultIds.contains(r.uid)) {
                            continue
                        }
                        resultArray.add(r)
                        resultIds.add(r.uid)
                    }
                    if (resultIds.size < 49) {
                        // we want more?
                        val rs2 = DumpGroupMember.queryUserInChannel(gid, userSearchKeyword, 100)
                        for (r in rs2) {
                            if (resultIds.contains(r.uid)) {
                                continue
                            }
                            resultArray.add(DumpGroupMember.UserDescriptorWithTime(r, 0))
                            resultIds.add(r.uid)
                        }
                    }
                }
            }
            // set result
            val arraylist = ArrayList<Any>(resultArray.size)
            val sparseArray = LongSparseArray<Any>(resultArray.size)
            for (r in resultArray) {
                val obj: Any = when (r) {
                    is DumpGroupMember.ITLUserDescriptor -> {
                        DumpGroupMember.getOrCreateUserObject(r)
                    }

                    is DumpGroupMember.ITLChannelDescriptor -> {
                        DumpGroupMember.getOrCreateChannelObject(r)
                    }

                    else -> {
                        Log.w("unknown result type: $r")
                        continue
                    }
                }

                arraylist.add(obj)
                sparseArray.put(r.uid, obj)
            }
            // call showUsersResult
            SyncUtils.runOnUiThread {
                showUsersResult.invoke(param.thisObject, arraylist, shadowCastToHostLongSparseArray(sparseArray), true)
            }
            // update resultStartPosition and resultLength
            fMentionsAdapter_resultStartPosition.setInt(param.thisObject, resultStartPosition)
            fMentionsAdapter_resultLength.setInt(param.thisObject, resultLength)
            // done, control flow terminated
            param.result = null
        }

        return true
    }

    @JvmStatic
    private fun shadowCastToHostLongSparseArray(origin: LongSparseArray<Any>): Any {
        val kLongSparseArray = Initiator.loadClass(LongSparseArray::class.java.name)
        val ctor = kLongSparseArray.declaredConstructors.single {
            it.parameterTypes.size == 1 && it.parameterTypes[0] == Int::class.javaPrimitiveType
        }
        val put = kLongSparseArray.getDeclaredMethod("put", Long::class.javaPrimitiveType, Any::class.java)
        val result = ctor.newInstance(origin.size())
        for (i in 0 until origin.size()) {
            val key = origin.keyAt(i)
            val value = origin.valueAt(i)
            put.invoke(result, key, value)
        }
        return result
    }

    private fun isSelfAdminInChat(chat: Any): Boolean {
        fChat_admin_rights.get(chat) ?: return false
        return true
    }

    private fun isCharLikeLyToBeNickName(c: Char): Boolean {
        // typical ASCII
        if (c == ' ' || c == ',' || c == '@' || c == '#') {
            return false
        }
        if (c.code in 1..0x7f) {
            return c.isLetterOrDigit() || c == '_' || c == '\'' || c == '-'
        }
        // and common CJK
        if (c == '，' || c == '。' || c == '？' || c == '！' || c == '；' || c == '：' ||
            c == '、' || c == '‘' || c == '’' || c == '“' || c == '”' || c.code == 0x2000 || c.code == 0x200a ||
            c == '【' || c == '】' || c == '《' || c == '》' || c == '（' || c == '）' || c == '·' || c == '～'
        ) {
            return false
        }
        // /u4e00-/u9fa5 (中文)
        // /x3130-/x318F (韩文)
        // /xAC00-/xD7A3 (韩文)
        // /u0800-/u4e00 (日文)
        if (c.code in 0x4e00..0x9fa5 || c.code in 0x3130..0x318F ||
            c.code in 0xAC00..0xD7A3 || c.code in 0x0800..0x4e00
        ) {
            return true
        }
        return false
    }
}
