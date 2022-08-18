package cc.ioctl.tmoe.hook.func

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.lifecycle.Parasitics
import cc.ioctl.tmoe.td.AccountController
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
    private lateinit var kChannelParticipant: Class<*>
    private lateinit var kPeer: Class<*>
    private lateinit var fChannelParticipant_peer: Field
    private lateinit var fChannelParticipant_date: Field
    private lateinit var fChannelParticipant_user_id: Field
    private lateinit var fTL_channels_channelParticipants_participants: Field
    private lateinit var fPeer_user_id: Field
    private lateinit var fPeer_chat_id: Field
    private lateinit var fPeer_channel_id: Field

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
        kChannelParticipant = Initiator.loadClass("org.telegram.tgnet.TLRPC\$ChannelParticipant")
        kTL_channels_getParticipants = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_channels_getParticipants")
        kInputChannel = Initiator.loadClass("org.telegram.tgnet.TLRPC\$InputChannel")
        fTL_channels_getParticipants_channel = Reflex.findField(kTL_channels_getParticipants, kInputChannel, "channel")
        fInputChannel_channel_id = Reflex.findField(kInputChannel, java.lang.Long.TYPE, "channel_id")
        kPeer = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Peer")
        fChannelParticipant_peer = Reflex.findField(kChannelParticipant, kPeer, "peer")
        fChannelParticipant_date = Reflex.findField(kChannelParticipant, Integer.TYPE, "date")
        fChannelParticipant_user_id = Reflex.findField(kChannelParticipant, java.lang.Long.TYPE, "user_id")
        fPeer_user_id = Reflex.findField(kPeer, java.lang.Long.TYPE, "user_id")
        fPeer_chat_id = Reflex.findField(kPeer, java.lang.Long.TYPE, "chat_id")
        fPeer_channel_id = Reflex.findField(kPeer, java.lang.Long.TYPE, "channel_id")

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
                        check(channelId > 0 && userId > 0) { "channel = $channelId, user = $userId" }
                        members.add(MemberInfo(channelId, userId, date.toLong()))
                    }
                }
                if (members.isNotEmpty()) {
                    val slot = AccountController.getCurrentActiveSlot()
                    check(slot >= 0) { "invalid slot: $slot" }
                    updateMemberInfoList(slot, members)
                    // Log.d("${members.size} rows affected.")
                }
            }
            else -> {
                Log.w("unhandled response: $klass")
            }
        }
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
                                    AlertDialog.Builder(ctx).apply {
                                        setTitle("Finished")
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
                setTitle("Confirm")
                setMessage("Fetch group members?")
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

    data class MemberInfo(val gid: Long, val uid: Long, val updateTime: Long) {
        init {
            check(gid > 0 && uid > 0) { "gid and uid must be positive" }
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
        val databaseFile = File(if (slot == 0) filesDir else File(filesDir, "account$slot"), "TMoe_group_dump.db")
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
        // group(int64 gid, int64 uid, int64 update_time)
        database.execSQL("CREATE TABLE IF NOT EXISTS group_member(gid INTEGER, uid INTEGER, update_time INTEGER, PRIMARY KEY(gid, uid))")
        mDatabase[slot] = database
        return database
    }

    private fun SQLiteDatabase.exec(stmt: String) {
        rawQuery(stmt, null)?.close()
    }

    private fun updateMemberInfo(slot: Int, info: MemberInfo) {
        updateMemberInfoList(slot, listOf(info))
    }

    private fun updateMemberInfoList(slot: Int, info: List<MemberInfo>) {
        val database = ensureDatabase(slot)
        database.beginTransaction()
        try {
            for (memberInfo in info) {
                database.execSQL(
                    "INSERT OR REPLACE INTO group_member(gid, uid, update_time) VALUES(?, ?, ?)",
                    arrayOf<Any>(memberInfo.gid, memberInfo.uid, memberInfo.updateTime)
                )
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun getMessageForTL_error(err: Any): String {
        return try {
            val code = Reflex.getInstanceObject(err, "code", Integer.TYPE) as Int
            val text = Reflex.getInstanceObject(err, "text", String::class.java) as String
            "code: $code, text: $text"
        } catch (e: ReflectiveOperationException) {
            e.toString()
        }
    }
}
