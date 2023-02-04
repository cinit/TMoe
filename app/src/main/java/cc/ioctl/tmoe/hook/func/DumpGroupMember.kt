package cc.ioctl.tmoe.hook.func

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.lifecycle.Parasitics
import cc.ioctl.tmoe.td.AccountController
import cc.ioctl.tmoe.td.binding.Chat
import cc.ioctl.tmoe.td.binding.User
import cc.ioctl.tmoe.ui.util.FaultyDialog
import cc.ioctl.tmoe.util.*
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object DumpGroupMember : CommonDynamicHook() {

    private val mDatabase = HashMap<Int, SQLiteDatabase>(1)

    private lateinit var kTL_channels_channelParticipants: Class<*>
    private lateinit var kRequestDelegate: Class<*>
    private lateinit var kTLObject: Class<*>
    private lateinit var kTL_error: Class<*>
    private lateinit var kTL_channels_getParticipants: Class<*>
    private lateinit var fTL_channels_getParticipants_channel: Field
    private lateinit var kInputChannel: Class<*>
    private lateinit var fInputChannel_channel_id: Field
    private lateinit var kUser: Class<*>
    private lateinit var fUser_id: Field
    private lateinit var fUser_first_name: Field
    private lateinit var fUser_last_name: Field
    private lateinit var fUser_access_hash: Field
    private lateinit var fUser_username: Field
    private lateinit var fUser_flags: Field
    private lateinit var fUser_deleted: Field
    private lateinit var fUser_bot: Field
    private lateinit var fUser_lang_code: Field
    private lateinit var fUser_inactive: Field
    private lateinit var kChannelParticipant: Class<*>
    private lateinit var fChannelParticipant_peer: Field
    private lateinit var fChannelParticipant_date: Field
    private lateinit var fChannelParticipant_user_id: Field
    private lateinit var fChannelParticipant_inviter_id: Field
    private lateinit var fChannelParticipant_flags: Field
    private lateinit var fTL_channels_channelParticipants_participants: Field
    private lateinit var fTL_channels_channelParticipants_users: Field
    private lateinit var fTL_channels_channelParticipants_chats: Field
    private lateinit var kPeer: Class<*>
    private lateinit var fPeer_user_id: Field
    private lateinit var fPeer_chat_id: Field
    private lateinit var fPeer_channel_id: Field
    private lateinit var kChat: Class<*>
    private lateinit var fChat_id: Field
    private lateinit var fChat_title: Field
    private lateinit var fChat_flags: Field
    private lateinit var fChat_access_hash: Field
    private lateinit var fChat_username: Field

    private val mRuntimeHookLock = Any()
    private val mHookedClasses = HashSet<Class<*>>()
    private val mCallbackSetLock = Any()
    private val mRequestCallbacks = HashSet<Pair<WeakReference<*>, WeakReference<*>>>(4)

    override fun initOnce(): Boolean {
        kRequestDelegate = Initiator.loadClass("org.telegram.tgnet.RequestDelegate")
        kTLObject = Initiator.loadClass("org.telegram.tgnet.TLObject")
        kTL_error = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_error")
        kTL_channels_channelParticipants = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_channels_channelParticipants")
        fTL_channels_channelParticipants_participants = Reflex.findField(
            kTL_channels_channelParticipants, java.util.ArrayList::class.java, "participants"
        )
        fTL_channels_channelParticipants_users = Reflex.findField(
            kTL_channels_channelParticipants, java.util.ArrayList::class.java, "users"
        )
        fTL_channels_channelParticipants_chats = Reflex.findField(
            kTL_channels_channelParticipants, java.util.ArrayList::class.java, "chats"
        )
        kChannelParticipant = Initiator.loadClass("org.telegram.tgnet.TLRPC\$ChannelParticipant")
        kTL_channels_getParticipants = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_channels_getParticipants")
        kInputChannel = Initiator.loadClass("org.telegram.tgnet.TLRPC\$InputChannel")
        fTL_channels_getParticipants_channel = Reflex.findField(kTL_channels_getParticipants, kInputChannel, "channel")
        fInputChannel_channel_id = Reflex.findField(kInputChannel, java.lang.Long.TYPE, "channel_id")
        kPeer = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Peer")
        fChannelParticipant_peer = Reflex.findField(kChannelParticipant, kPeer, "peer")
        fChannelParticipant_date = Reflex.findField(kChannelParticipant, Integer.TYPE, "date")
        fChannelParticipant_user_id = Reflex.findField(kChannelParticipant, java.lang.Long.TYPE, "user_id")
        fChannelParticipant_inviter_id = Reflex.findField(kChannelParticipant, java.lang.Long.TYPE, "inviter_id")
        fChannelParticipant_flags = Reflex.findField(kChannelParticipant, Integer.TYPE, "flags")
        fPeer_user_id = Reflex.findField(kPeer, java.lang.Long.TYPE, "user_id")
        fPeer_chat_id = Reflex.findField(kPeer, java.lang.Long.TYPE, "chat_id")
        fPeer_channel_id = Reflex.findField(kPeer, java.lang.Long.TYPE, "channel_id")
        kUser = Initiator.loadClass("org.telegram.tgnet.TLRPC\$User")
        fUser_id = Reflex.findField(kUser, java.lang.Long.TYPE, "id")
        fUser_first_name = Reflex.findField(kUser, String::class.java, "first_name")
        fUser_last_name = Reflex.findField(kUser, String::class.java, "last_name")
        fUser_username = Reflex.findField(kUser, String::class.java, "username")
        fUser_access_hash = Reflex.findField(kUser, java.lang.Long.TYPE, "access_hash")
        fUser_flags = Reflex.findField(kUser, Integer.TYPE, "flags")
        fUser_deleted = Reflex.findField(kUser, java.lang.Boolean.TYPE, "deleted")
        fUser_bot = Reflex.findField(kUser, java.lang.Boolean.TYPE, "bot")
        fUser_lang_code = Reflex.findField(kUser, String::class.java, "lang_code")
        fUser_inactive = Reflex.findField(kUser, java.lang.Boolean.TYPE, "inactive")
        kChat = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Chat")
        fChat_id = Reflex.findField(kChat, java.lang.Long.TYPE, "id")
        fChat_title = Reflex.findField(kChat, String::class.java, "title")
        fChat_flags = Reflex.findField(kChat, Integer.TYPE, "flags")
        fChat_access_hash = Reflex.findField(kChat, java.lang.Long.TYPE, "access_hash")
        fChat_username = Reflex.findField(kChat, String::class.java, "username")
        User.initialize()
        Chat.initialize()

        // test linkage
        val currentSlot = AccountController.getCurrentActiveSlot()
        if (currentSlot < 0) {
            throw RuntimeException("AccountController.getCurrentActiveSlot fail")
        }
        val sendRequest9 = Initiator.loadClass("org.telegram.tgnet.ConnectionsManager").declaredMethods.single {
            it.name == "sendRequest" && it.parameterTypes.size == 9
        }
        HookUtils.hookBeforeIfEnabled(this, sendRequest9) { params ->
            val request = kTLObject.cast(params.args[0] ?: return@hookBeforeIfEnabled)
            val onComplete = params.args[1] ?: return@hookBeforeIfEnabled
            kRequestDelegate.cast(onComplete)
            val klass = onComplete.javaClass
            if (klass.name.startsWith("\$Proxy")) {
                // don't hook proxy
                return@hookBeforeIfEnabled
            }
            if (isInterestedRequest(request.javaClass)) {
                if (!mHookedClasses.contains(klass)) {
                    val run = klass.getDeclaredMethod("run", kTLObject, kTL_error)
                    synchronized(mRuntimeHookLock) {
                        if (!mHookedClasses.contains(klass)) {
                            XposedBridge.hookMethod(run, mHookedOnCompleteHandler)
                            mHookedClasses.add(klass)
                        }
                    }
                }
                val pair = Pair(WeakReference(request), WeakReference(onComplete))
                synchronized(mCallbackSetLock) {
                    mRequestCallbacks.add(pair)
                }
            }
        }
        val putChatsInternal = Initiator.loadClass("org.telegram.messenger.MessagesStorage")
            .getDeclaredMethod("putChatsInternal", java.util.ArrayList::class.java)
        XposedBridge.hookMethod(putChatsInternal, mPutChatsInternalHook)
        // add btn long click listener
        val kProfileActivity = Initiator.loadClass("org.telegram.ui.ProfileActivity")
        val fProfileActivity_onlineTextView = kProfileActivity.getDeclaredField("onlineTextView").apply {
            isAccessible = true
        }
        kProfileActivity.getDeclaredMethod("createView", Context::class.java).let {
            HookUtils.hookAfterIfEnabled(this, it) { params ->
                val that = params.thisObject
                val onlineTextView = fProfileActivity_onlineTextView.get(that) as Array<out View?>
                val v0 = onlineTextView[0]
                val v1 = onlineTextView[1]
                val listener = OnOnlineCountTextViewLongListener(that)
                v0?.setOnLongClickListener(listener)
                v1?.setOnLongClickListener(listener)
            }
        }
        return true
    }

    private class RequestDelegateInvocationHandler(private val fp: (Any?, Any?) -> Unit) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
            return if (method.name == "run") {
                fp(args!![0], args[1])
                null
            } else {
                // Object.class
                method.invoke(this, args)
            }
        }
    }

    @Throws(ReflectiveOperationException::class)
    private fun createRequestDelegate(fp: (Any?, Any?) -> Unit): Any {
        // ensure method name and signature is correct
        kRequestDelegate.getDeclaredMethod("run", kTLObject, kTL_error)
        return Proxy.newProxyInstance(
            Initiator.getHostClassLoader(),
            arrayOf(kRequestDelegate),
            RequestDelegateInvocationHandler(fp)
        )
    }

    private fun handleRequestComplete(originalRequest: Any, response: Any) {
        val klass = response.javaClass
        when (klass) {
            kTL_channels_channelParticipants -> {
                val inputChannel = fTL_channels_getParticipants_channel.get(originalRequest)
                val channelId = fInputChannel_channel_id.getLong(inputChannel)
                check(channelId > 0) { "invalid channel_id: $channelId" }
                val slot = AccountController.getCurrentActiveSlot()
                check(slot >= 0) { "invalid slot: $slot" }
                val channelParticipants = fTL_channels_channelParticipants_participants.get(response) as ArrayList<*>
                val channelParticipantCount = channelParticipants.size
                // Log.d("channelParticipants.size = $channelParticipantCount")
                val members = ArrayList<MemberInfo>(channelParticipantCount)
                channelParticipants.forEach {
                    var userId: Long = 0
                    val date = fChannelParticipant_date.getInt(it)
                    val peer = fChannelParticipant_peer.get(it)
                    if (peer != null) {
                        userId = fPeer_user_id.getLong(peer)
                    }
                    if (userId != 0L) {
                        val inviterUserId = fChannelParticipant_inviter_id.getLong(it)
                        val flags = fChannelParticipant_flags.getInt(it)
                        check(channelId > 0 && userId > 0) { "channel = $channelId, user = $userId" }
                        members.add(MemberInfo(channelId, userId, flags, date.toLong(), inviterUserId))
                    }
                }
                if (members.isNotEmpty()) {
                    updateChannelMemberInfoList(slot, members)
                    // Log.d("${members.size} rows affected.")
                }
                val users = fTL_channels_channelParticipants_users.get(response) as ArrayList<*>
                if (users.isNotEmpty()) {
                    val userCount = users.size
                    // Log.d("users.size = $userCount")
                    val userSet = ArrayList<UserInfo9>(userCount)
                    users.forEach {
                        val uid = fUser_id.getLong(it)
                        check(uid > 0) { "invalid user_id: $uid" }
                        val firstName = (fUser_first_name.get(it) as String?) ?: ""
                        val lastName = (fUser_last_name.get(it) as String?)?.ifEmpty { null }
                        val username = (fUser_username.get(it) as String?)?.ifEmpty { null }
                        val accessHash = fUser_access_hash.getLong(it)
                        val flags = fUser_flags.getInt(it)
                        val languageCode = (fUser_lang_code.get(it) as String?)?.ifEmpty { null }
                        val isBot = fUser_bot.getBoolean(it)
                        val isDeleted = fUser_deleted.getBoolean(it)
                        val isInactive = fUser_inactive.getBoolean(it)
                        check(uid > 0) { "invalid user_id: $uid" }
                        val name = if (lastName.isNullOrEmpty()) firstName else "$firstName $lastName"
                        val user = UserInfo9(
                            uid, accessHash, name, flags, username, isBot, isDeleted, isInactive, languageCode,
                        )
                        userSet.add(user)
                    }
                    updateUserInfoList(slot, userSet)
                }
                val chats = fTL_channels_channelParticipants_chats.get(response) as ArrayList<*>
                if (chats.isNotEmpty()) {
                    val chatCount = chats.size
                    // Log.d("chats.size = $chatCount")
                    val chatSet = ArrayList<ChannelInfo>(chatCount)
                    chats.forEach {
                        chatSet.add(channelInfoFromChat(it))
                    }
                    updateChannelInfoList(slot, chatSet)
                }
            }

            else -> {
                Log.w("unhandled response: $klass")
            }
        }
    }

    private val mPutChatsInternalHook = HookUtils.beforeIfEnabled(this) { params ->
        val chats = params.args[0] as ArrayList<*>?
        val slot = AccountController.getCurrentActiveSlot()
        if (!chats.isNullOrEmpty()) {
            val chatInfoList = ArrayList<ChannelInfo>(chats.size)
            chats.forEach { chatObj ->
                chatInfoList.add(channelInfoFromChat(chatObj))
            }
            updateChannelInfoList(slot, chatInfoList)
        }
    }

    private fun channelInfoFromChat(chatObj: Any): ChannelInfo {
        val chatId = fChat_id.getLong(chatObj)
        check(chatId > 0) { "invalid chat_id: $chatId" }
        val title = (fChat_title.get(chatObj) as String?) ?: ""
        val flags = fChat_flags.getInt(chatObj)
        val accessHash = fChat_access_hash.getLong(chatObj)
        val username = (fChat_username.get(chatObj) as String?)?.ifEmpty { null }
        return ChannelInfo(chatId, accessHash, title, flags, username)
    }

    private val mHookedOnCompleteHandler = HookUtils.afterIfEnabled(this) { params ->
        val resp = params.args[0] ?: return@afterIfEnabled // error
        val klass = resp.javaClass
        var originalRequest: Any? = null
        synchronized(mCallbackSetLock) {
            val it = mRequestCallbacks.iterator()
            while (it.hasNext()) {
                val pair = it.next()
                val r = pair.first.get()
                val c = pair.second.get()
                if (r == null || c == null) {
                    it.remove()
                } else {
                    if (c == params.thisObject) {
                        originalRequest = r
                        it.remove()
                        break
                    }
                }
            }
        }
        if (originalRequest == null) {
            Log.w("original request lost, resp = " + klass.name + ", this = " + params.thisObject.javaClass.name)
            return@afterIfEnabled
        }
        handleRequestComplete(originalRequest!!, resp)
    }

    @Throws(ReflectiveOperationException::class)
    private fun createTL_channels_getParticipants(acctSlot: Int, chatId: Long, offset: Int, filter: Any? = null): Any {
        check(acctSlot >= 0)
        val accountInstance = Reflex.invokeStatic(
            Initiator.loadClass("org.telegram.messenger.AccountInstance"),
            "getInstance", acctSlot, Integer.TYPE
        )!!
        val messagesController = Reflex.invokeVirtual(accountInstance, "getMessagesController")!!
        val inputChannel = Reflex.invokeVirtual(messagesController, "getInputChannel", chatId, java.lang.Long.TYPE)!!
        val obj = kTL_channels_getParticipants.getConstructor().newInstance()
        Reflex.setInstanceObject(obj, "channel", null, inputChannel)
        Reflex.setInstanceObject(obj, "offset", null, offset)
        Reflex.setInstanceObject(
            obj, "filter", null, filter
                ?: Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_channelParticipantsRecent").newInstance()
        )
        Reflex.setInstanceObject(obj, "limit", null, 200)
        return obj
    }

    @UiThread
    private fun fetchGroupMembersForeground(fragment: Any, ctx: Context) {
        var progressDialog: AlertDialog? = null
        val isCancelled = AtomicBoolean(false)
        try {
            val slot = AccountController.getCurrentActiveSlot()
            check(slot >= 0) { "invalid slot: $slot" }
            val chatId = Reflex.getInstanceObject(fragment, "chatId", java.lang.Long.TYPE) as Long
            check(chatId != 0L) { "invalid chatId: $chatId" }
            val dialog = AlertDialog.Builder(ctx).apply {
                setTitle("TLRPC")
                setMessage("TL_channelParticipantsRecent\nuser = $slot, chat = $chatId")
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    isCancelled.set(true)
                }
            }.show()
            progressDialog = dialog
            val accountInstance = Reflex.invokeStatic(
                Initiator.loadClass("org.telegram.messenger.AccountInstance"),
                "getInstance", slot, Integer.TYPE
            )!!
            val connMgr = Reflex.invokeVirtual(accountInstance, "getConnectionsManager")!!
            val sendRequest2 = Initiator.loadClass("org.telegram.tgnet.ConnectionsManager")
                .getDeclaredMethod("sendRequest", kTLObject, kRequestDelegate)
            val currentOffset = AtomicInteger(0)
            val startTime = System.currentTimeMillis()
            val task = object : Runnable {
                override fun run() {
                    try {
                        if (isCancelled.get()) {
                            return
                        }
                        val startOffset = currentOffset.get()
                        val request = createTL_channels_getParticipants(slot, chatId, startOffset, null)
                        val resp = createRequestDelegate { result, error ->
                            if (error != null) {
                                isCancelled.set(true)
                                dialog.dismiss()
                                FaultyDialog.show(ctx, "TL_error", getMessageForTL_error(error))
                                return@createRequestDelegate
                            }
                            kTL_channels_channelParticipants.cast(result!!)
                            val participants = fTL_channels_channelParticipants_participants.get(result) as ArrayList<*>
                            currentOffset.set(startOffset + participants.size)
                            handleRequestComplete(request, result)
                            if (participants.size < 100) {
                                // reached the end
                                isCancelled.set(true)
                                dialog.dismiss()
                                SyncUtils.runOnUiThread {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    val totalCount = currentOffset.get()
                                    Parasitics.injectModuleResources(ctx.resources)
                                    AlertDialog.Builder(ctx).apply {
                                        setTitle(R.string.DialogTitle_OperationCompleted)
                                        setMessage(
                                            "TL_channelParticipantsRecent\n" +
                                                    "user = $slot, chat = $chatId, " +
                                                    "total = $totalCount, elapsed = $elapsed ms"
                                        )
                                        setPositiveButton(android.R.string.ok) { _, _ -> }
                                    }.show()
                                }
                            } else {
                                // update progress
                                SyncUtils.runOnUiThread {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    val offset = currentOffset.get()
                                    val last = participants.size
                                    dialog.setMessage(
                                        "TL_channelParticipantsRecent\n" +
                                                "user = $slot, chat = $chatId, offset = $offset, " +
                                                "batch_size = $last, elapsed = ${elapsed}ms"
                                    )
                                }
                                // wait 1s and continue
                                SyncUtils.postDelayed(1000L, this)
                            }
                        }
                        sendRequest2.invoke(connMgr, request, resp)
                    } catch (e: Exception) {
                        isCancelled.set(true)
                        dialog.dismiss()
                        FaultyDialog.show(ctx, e)
                    }
                }
            }
            task.run()
        } catch (e: Exception) {
            progressDialog?.dismiss()
            FaultyDialog.show(ctx, e)
        }
    }

    private class OnOnlineCountTextViewLongListener(profileFragment: Any) : View.OnLongClickListener {

        private val mFragmentRef = WeakReference(profileFragment)
        override fun onLongClick(v: View): Boolean {
            val fragment = mFragmentRef.get() ?: return false
            val ctx = CommonContextWrapper.createAppCompatContext(v.context)
            Parasitics.injectModuleResources(ctx.resources)
            // confirm action
            AlertDialog.Builder(ctx).apply {
                setTitle(R.string.DialogTitle_OperationConfirmation)
                setMessage(R.string.DialogMsg_ConfirmDumpChannelMembers)
                setCancelable(true)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    fetchGroupMembersForeground(fragment, ctx)
                }
                show()
            }
            return true
        }
    }

    private fun isInterestedRequest(requestClass: Class<*>): Boolean {
        val name = requestClass.name
        // Log.d("requestClass: $name")
        return when (name) {
            "org.telegram.tgnet.TLRPC\$TL_channels_getParticipants",
            "org.telegram.tgnet.TLRPC\$TL_channels_getParticipant" -> true

            else -> false
        }
    }

    @Synchronized
    private fun ensureDatabase(slot: Int): SQLiteDatabase {
        check(slot >= 0 && slot < Short.MAX_VALUE) { "invalid slot: $slot" }
        if (mDatabase.containsKey(slot)) {
            return mDatabase[slot]!!
        }
        val context = HostInfo.getApplication()
        val filesDir = context.filesDir
        val databaseFile = File(if (slot == 0) filesDir else File(filesDir, "account$slot"), "TMoe_channel_dump.db")
        val createTable = !databaseFile.exists()
        val database = SQLiteDatabase.openDatabase(
            databaseFile.absolutePath, null,
            SQLiteDatabase.OPEN_READWRITE or if (createTable) SQLiteDatabase.CREATE_IF_NECESSARY else 0
        ).apply {
            beginTransaction()
            try {
                exec("PRAGMA secure_delete = ON")
                exec("PRAGMA temp_store = MEMORY")
                exec("PRAGMA journal_mode = WAL")
                exec("PRAGMA journal_size_limit = 10485760")
                exec("PRAGMA busy_timeout = 5000")
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS t_channel_member
            (
                gid        INTEGER NOT NULL,
                uid        INTEGER NOT NULL,
                flags      INTEGER NOT NULL,
                join_date  INTEGER,
                inviter_id INTEGER,
                ext_status INTEGER NOT NULL,
                ext_flags  INTEGER NOT NULL,
                update_ts  INTEGER NOT NULL,
                PRIMARY KEY (gid, uid)
            )
        """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_t_channel_member_uid ON t_channel_member (uid)")
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS t_user
            (
                uid         INTEGER PRIMARY KEY,
                access_hash INTEGER NOT NULL,
                name        TEXT    NOT NULL,
                flags       INTEGER NOT NULL,
                username    TEXT,
                bot         INTEGER,
                deleted     INTEGER,
                inactive    INTEGER,
                lang_code   TEXT,
                update_ts   INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS t_channel
            (
                uid          INTEGER PRIMARY KEY,
                access_hash  INTEGER NOT NULL,
                name         TEXT    NOT NULL,
                flags        INTEGER NOT NULL,
                username     TEXT,
                broadcast    INTEGER NOT NULL,
                megagroup    INTEGER NOT NULL,
                gigagroup    INTEGER NOT NULL,
                has_link     INTEGER NOT NULL,
                noforwards   INTEGER NOT NULL,
                join_to_send INTEGER NOT NULL,
                join_request INTEGER NOT NULL,
                update_ts    INTEGER NOT NULL
            )
            """.trimIndent()
        )
        mDatabase[slot] = database
        return database
    }

    data class UserInfo9(
        val uid: Long,
        val accessHash: Long,
        val name: String,
        val flags: Int,
        val username: String?,
        val bot: Boolean,
        val deleted: Boolean,
        val inactive: Boolean,
        val langCode: String?
    ) {
        init {
            check(uid > 0) { "uid must be positive" }
        }
    }

    data class MemberInfo(
        val gid: Long,
        val uid: Long,
        val flags: Int,
        val joinTime: Long,
        val inviterId: Long
    ) {
        init {
            check(gid > 0 && uid > 0) { "gid and uid must be positive" }
        }
    }

    data class ChannelInfo(
        val uid: Long,
        val accessHash: Long,
        val name: String,
        val flags: Int,
        val username: String?,
        val broadcast: Boolean = (flags and 32 != 0),
        val megagroup: Boolean = (flags and 256 != 0),
        val gigagroup: Boolean = (flags and 67108864 != 0),
        val hasLink: Boolean = (flags and 1048576 != 0),
        val noForwards: Boolean = (flags and 134217728 != 0),
        val joinToSend: Boolean = (flags and 268435456 != 0),
        val joinRequest: Boolean = (flags and 536870912 != 0)
    ) {
        init {
            check(uid > 0) { "uid must be positive" }
        }
    }

    private fun SQLiteDatabase.exec(stmt: String) {
        rawQuery(stmt, null)?.close()
    }

    private fun updateChannelMemberInfo(slot: Int, info: MemberInfo) {
        updateChannelMemberInfoList(slot, listOf(info))
    }

    private fun updateChannelMemberInfoList(slot: Int, info: List<MemberInfo>) {
        val now = System.currentTimeMillis() / 1000L
        val database = ensureDatabase(slot)
        database.beginTransaction()
        try {
            for (memberInfo in info) {
                database.execSQL(
                    "INSERT OR REPLACE INTO t_channel_member (gid, uid, flags, join_date, inviter_id," +
                            " ext_status, ext_flags, update_ts) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        memberInfo.gid.toString(),
                        memberInfo.uid.toString(),
                        memberInfo.flags.toString(),
                        memberInfo.joinTime.toString(),
                        memberInfo.inviterId.toString(),
                        "0",
                        "0",
                        now.toString()
                    )
                )
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun updateUserInfoList(slot: Int, info: List<UserInfo9>) {
        val now = System.currentTimeMillis() / 1000L
        val database = ensureDatabase(slot)
        database.beginTransaction()
        try {
            for (userInfo in info) {
                if (userInfo.deleted) {
                    // we don't want to overwrite the user info
                    val affected = database.update(
                        "t_user",
                        ContentValues().apply {
                            put("access_hash", userInfo.accessHash)
                            put("deleted", 1)
                            put("update_ts", now)
                        },
                        "uid = ?",
                        arrayOf(userInfo.uid.toString())
                    )
                    if (affected != 0) {
                        // done, no need to insert again.
                        continue
                    }
                }
                database.execSQL(
                    "INSERT OR REPLACE INTO t_user (uid, access_hash, name, flags, username, " +
                            "bot, deleted, inactive, lang_code, update_ts) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        userInfo.uid.toString(),
                        userInfo.accessHash.toString(),
                        userInfo.name,
                        userInfo.flags.toString(),
                        userInfo.username,
                        if (userInfo.bot) "1" else "0",
                        if (userInfo.deleted) "1" else "0",
                        if (userInfo.inactive) "1" else "0",
                        userInfo.langCode,
                        now.toString()
                    )
                )
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun updateChannelInfoList(slot: Int, info: List<ChannelInfo>) {
        val now = System.currentTimeMillis() / 1000L
        val database = ensureDatabase(slot)
        database.beginTransaction()
        try {
            for (channelInfo in info) {
                database.execSQL(
                    "INSERT OR REPLACE INTO t_channel " +
                            "(uid, access_hash, name, flags, username, " +
                            "broadcast, megagroup, gigagroup, has_link, noforwards, join_to_send, join_request, " +
                            "update_ts) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        channelInfo.uid.toString(),
                        channelInfo.accessHash.toString(),
                        channelInfo.name,
                        channelInfo.flags.toString(),
                        channelInfo.username,
                        if (channelInfo.broadcast) "1" else "0",
                        if (channelInfo.megagroup) "1" else "0",
                        if (channelInfo.gigagroup) "1" else "0",
                        if (channelInfo.hasLink) "1" else "0",
                        if (channelInfo.noForwards) "1" else "0",
                        if (channelInfo.joinToSend) "1" else "0",
                        if (channelInfo.joinRequest) "1" else "0",
                        now.toString()
                    )
                )
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    @JvmStatic
    fun getMessageForTL_error(err: Any): String {
        return try {
            val code = Reflex.getInstanceObject(err, "code", Integer.TYPE) as Int
            val text = Reflex.getInstanceObject(err, "text", String::class.java) as String
            "code: $code, text: $text"
        } catch (e: ReflectiveOperationException) {
            e.toString()
        }
    }

    data class GroupDescriptor(
        val uid: Long,
        val accessHash: Long,
        val name: String,
        val username: String?,
        val flags: Int,
    ) {
        override fun toString(): String {
            return "GroupDescriptor(uid=$uid, accessHash=$accessHash, name='$name', username=$username, flags=$flags)"
        }
    }

    fun queryUserGroupDescriptors(uid: Long): List<GroupDescriptor> {
        val currentSlot = AccountController.getCurrentActiveSlot()
        if (currentSlot < 0) {
            Log.w("queryUserGroupDescriptors: no active account")
            return emptyList()
        }
        if (uid <= 0) {
            return emptyList()
        }
        val gids = mutableSetOf<Long>()
        val database = ensureDatabase(currentSlot)
        val start = System.nanoTime()
        database.rawQuery(
            "SELECT gid FROM t_channel_member WHERE uid = ?",
            arrayOf(uid.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                gids.add(cursor.getLong(0))
            }
        }
        val results = gids.map { gid ->
            var group: GroupDescriptor? = null
            database.rawQuery(
                "SELECT access_hash, name, username, flags FROM t_channel WHERE uid = ?",
                arrayOf(gid.toString())
            ).use { cursor ->
                if (cursor.moveToNext()) {
                    group = GroupDescriptor(
                        gid,
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3)
                    )
                }
            }
            group ?: GroupDescriptor(
                gid,
                0,
                gid.toString(),
                null,
                4096
            )
        }.filter {
            // exclude broadcast channels
            (it.flags and 32) == 0
        }
        val cost = (System.nanoTime() - start) / 1000L
        // Log.d("queryUserGroupDescriptors: ${results.size} groups, $cost us")
        return results
    }

    @JvmStatic
    fun getChatFormCache(uid: Long): Any? {
        val currentSlot = AccountController.getCurrentActiveSlot()
        if (currentSlot < 0) {
            error("queryUserGroupDescriptors: no active account")
        }
        val accountInstance = Reflex.invokeStatic(
            Initiator.loadClass("org.telegram.messenger.AccountInstance"),
            "getInstance", currentSlot, Integer.TYPE
        )!!
        val messagesController = Reflex.invokeVirtual(accountInstance, "getMessagesController")!!
        // MessagesController->getChat(Ljava/lang/Long;)Lorg/telegram/tgnet/TLRPC$Chat;
        return Reflex.invokeVirtual(messagesController, "getChat", uid, java.lang.Long::class.java)
    }

    @JvmStatic
    fun createMinimalChannelChat(groupInfo: GroupDescriptor): Any {
        // create a minimal channel chat object which only contains id, access_hash, title, username and some flags
        val kTL_channel = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_channel")
        val kTL_chat = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_chat")
        val isBasicGroup = groupInfo.flags.let {
            (it and 32 == 0) and (it and 256 == 0) and (it and 67108864 == 0)
        }
        return (if (isBasicGroup) kTL_chat else kTL_channel).newInstance().also { obj ->
            val chat = Chat(obj)
            chat.id = groupInfo.uid
            chat.title = groupInfo.name
            chat.access_hash = groupInfo.accessHash
            chat.username = groupInfo.username
            val allowedFlags = bitwiseOr(
                1, 2, 4, 32, 128, 256, 512, 2048,
                524288, 1048576, 33554432, 67108864,
                134217728, 268435456, 536870912, 1073741824
            )
            val flags = (groupInfo.flags and allowedFlags) or 4096
            chat.flags = flags
            // update flag attributes
            chat.creator = flags and 1 != 0
            chat.left = flags and 4 != 0
            chat.broadcast = flags and 32 != 0
            chat.megagroup = flags and 256 != 0
            chat.has_link = flags and 1048576 != 0
            chat.gigagroup = flags and 67108864 != 0
            chat.noforwards = flags and 134217728 != 0
            chat.join_to_send = flags and 268435456 != 0
            chat.join_request = flags and 536870912 != 0
            chat.forum = flags and 1073741824 != 0
        }
    }

    @JvmStatic
    private fun bitwiseOr(vararg flags: Int): Int {
        var result = 0
        flags.forEach { result = result or it }
        return result
    }

    data class UserDescriptor(
        val uid: Long,
        val accessHash: Long,
        val name: String,
        val username: String?,
        val flags: Int,
    ) {
        override fun toString(): String {
            return "UserDescriptor(uid=$uid, accessHash=$accessHash, name='$name', username=$username, flags=$flags)"
        }
    }

    @JvmStatic
    fun getUserFormCache(uid: Long): Any? {
        check(uid > 0) { "uid must be positive" }
        val currentSlot = AccountController.getCurrentActiveSlot()
        check(currentSlot >= 0) { "getUserFormCache: no active account" }
        val accountInstance = Reflex.invokeStatic(
            Initiator.loadClass("org.telegram.messenger.AccountInstance"),
            "getInstance", currentSlot, Integer.TYPE
        )!!
        val messagesController = Reflex.invokeVirtual(accountInstance, "getMessagesController")!!
        // MessagesController->getUser(Ljava/lang/Long;)Lorg/telegram/tgnet/TLRPC$User;
        return Reflex.invokeVirtual(messagesController, "getUser", uid, java.lang.Long::class.java)
    }

    @JvmStatic
    fun queryUserDescriptor(uid: Long): UserDescriptor? {
        val currentSlot = AccountController.getCurrentActiveSlot()
        if (currentSlot < 0) {
            Log.w("queryUserDescriptor: no active account")
            return null
        }
        check(uid > 0) { "uid must be positive" }
        val database = ensureDatabase(currentSlot)
        var user: UserDescriptor? = null
        database.rawQuery(
            "SELECT access_hash, name, username, flags FROM t_user WHERE uid = ?",
            arrayOf(uid.toString())
        ).use { cursor ->
            if (cursor.moveToNext()) {
                user = UserDescriptor(
                    uid,
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3)
                )
            }
        }
        return user
    }

    @JvmStatic
    fun createMinimalUser(userInfo: UserDescriptor): Any {
        check(userInfo.uid > 0) { "uid must be positive" }
        // create a minimal user object which only contains id, access_hash, name, username and some flags
        val kTL_user = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_user")
        return kTL_user.newInstance().also { obj ->
            val user = User(obj)
            user.id = userInfo.uid
            user.first_name = userInfo.name
            user.last_name = ""
            user.access_hash = userInfo.accessHash
            user.username = userInfo.username
            // flag 4: last name, which has been dropped
            val allowedFlags = bitwiseOr(
                1, 2, 8,
                2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144,
                2097152, 8388608, 67108864
            )
            val flags = (userInfo.flags and allowedFlags) or 1048576
            user.flags = flags
            // update flag attributes
            user.contact = flags and 2048 != 0
            user.mutual_contact = flags and 4096 != 0
            user.deleted = flags and 8192 != 0
            user.bot = flags and 16384 != 0
        }
    }

    @JvmStatic
    fun queryUserInfoById(uid: Long): UserDescriptor? {
        val currentSlot = AccountController.getCurrentActiveSlot()
        if (currentSlot < 0) {
            Log.w("queryUserInfoById: no active account")
            return null
        }
        check(uid > 0) { "uid must be positive" }
        val database = ensureDatabase(currentSlot)
        var user: UserDescriptor? = null
        database.rawQuery(
            "SELECT access_hash, name, username, flags FROM t_user WHERE uid = ?",
            arrayOf(uid.toString())
        ).use { cursor ->
            if (cursor.moveToNext()) {
                user = UserDescriptor(
                    uid,
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3)
                )
            }
        }
        return user
    }

    @JvmStatic
    fun queryChannelInfoById(uid: Long): GroupDescriptor? {
        val currentSlot = AccountController.getCurrentActiveSlot()
        if (currentSlot < 0) {
            Log.w("queryChannelInfoById: no active account")
            return null
        }
        check(uid > 0) { "uid must be positive" }
        val database = ensureDatabase(currentSlot)
        var group: GroupDescriptor? = null
        database.rawQuery(
            "SELECT access_hash, name, username, flags FROM t_channel WHERE uid = ?",
            arrayOf(uid.toString())
        ).use { cursor ->
            if (cursor.moveToNext()) {
                group = GroupDescriptor(
                    uid,
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3)
                )
            }
        }
        return group
    }

}
