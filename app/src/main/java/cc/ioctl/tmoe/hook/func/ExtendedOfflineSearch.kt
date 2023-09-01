package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.td.AccountController
import cc.ioctl.tmoe.td.RequestInterceptor
import cc.ioctl.tmoe.td.binding.Chat
import cc.ioctl.tmoe.td.binding.Peer
import cc.ioctl.tmoe.td.binding.User
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.Log
import cc.ioctl.tmoe.util.Reflex
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import kotlin.math.abs

@FunctionHookEntry
object ExtendedOfflineSearch : CommonDynamicHook() {

    private lateinit var kRequestDelegate: Class<*>
    private lateinit var kTLObject: Class<*>
    private lateinit var kTL_error: Class<*>
    private lateinit var kChat: Class<*>
    private lateinit var kInputUser: Class<*>
    private lateinit var fInputUser_user_id: Field
    private lateinit var fInputUser_access_hash: Field
    private lateinit var kTL_contacts_search: Class<*>
    private lateinit var fTL_contacts_search_q: Field
    private lateinit var kTL_contacts_found: Class<*>
    private lateinit var fTL_contacts_found_my_results: Field
    private lateinit var fTL_contacts_found_results: Field
    private lateinit var fTL_contacts_found_chats: Field
    private lateinit var fTL_contacts_found_users: Field

    private val mRuntimeHookLock = Any()
    private val mHookedClasses = HashSet<Class<*>>()
    private val mCallbackSetLock = Any()
    private val mRequestCallbacks = HashSet<Pair<WeakReference<*>, WeakReference<*>>>(4)

    override fun initOnce(): Boolean {
        kRequestDelegate = Initiator.loadClass("org.telegram.tgnet.RequestDelegate")
        kTLObject = Initiator.loadClass("org.telegram.tgnet.TLObject")
        kTL_error = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_error")
        kChat = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Chat")
        kInputUser = Initiator.loadClass("org.telegram.tgnet.TLRPC\$InputUser")
        fInputUser_user_id = Reflex.findField(kInputUser, java.lang.Long.TYPE, "user_id")
        fInputUser_access_hash = Reflex.findField(kInputUser, java.lang.Long.TYPE, "access_hash")
        kTL_contacts_search = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_contacts_search")
        fTL_contacts_search_q = Reflex.findField(kTL_contacts_search, String::class.java, "q")
        kTL_contacts_found = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_contacts_found")
        fTL_contacts_found_my_results = Reflex.findField(kTL_contacts_found, java.util.ArrayList::class.java, "my_results")
        fTL_contacts_found_results = Reflex.findField(kTL_contacts_found, java.util.ArrayList::class.java, "results")
        fTL_contacts_found_chats = Reflex.findField(kTL_contacts_found, java.util.ArrayList::class.java, "chats")
        fTL_contacts_found_users = Reflex.findField(kTL_contacts_found, java.util.ArrayList::class.java, "users")

        // test linkage
        val currentSlot = AccountController.getCurrentActiveSlot()
        check(currentSlot >= 0) { "AccountController.getCurrentActiveSlot fail" }

        RequestInterceptor.registerTlrpcSuccessfulResultInterceptor(
            Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_contacts_search")
        ) { req, resp ->
            if (!isEnabled) {
                return@registerTlrpcSuccessfulResultInterceptor null
            }
            handleRequestComplete(req, resp)
            null
        }

        return true
    }

    private fun handleRequestComplete(request: Any, response: Any) {
        val reqType = request.javaClass
        when (reqType) {
            kTL_contacts_search -> {
                if (kTL_contacts_found.isInstance(response)) {
                    val query = fTL_contacts_search_q.get(request) as String? ?: ""
                    if (query.matches(Regex("-?[0-9]{4,10}")) || query.matches(Regex("-?100[0-9]{10}"))) {
                        val id = abs(query.toLong()) % 1000000000000L
                        handleQueryWithId(id, response)
                    }
                } else if (!kTL_error.isInstance(response)) {
                    Log.e("unknown response for kTL_contacts_search: " + response.javaClass.name)
                }
            }
        }

    }

    private fun handleQueryWithId(id: Long, response: Any) {
        if (id <= 0) {
            return
        }
        // Log.d("handleQueryWithId: $id")
        val myResults = fTL_contacts_found_my_results.get(response) as ArrayList<Any>
        val globalResults = fTL_contacts_found_my_results.get(response) as ArrayList<Any>
        val chats = fTL_contacts_found_chats.get(response) as ArrayList<Any>
        val users = fTL_contacts_found_users.get(response) as ArrayList<Any>
        // cached chat
        val cachedChat = DumpGroupMember.getChatFormCache(id)
        if (cachedChat != null) {
            // Log.d("found cached chat: $cachedChat")
            // check if already in results
            val alreadyExists = myResults.any {
                val p = Peer(it)
                p.isChannel && p.channel_id == id || p.isChat && p.chat_id == id
            } || globalResults.any {
                val p = Peer(it)
                p.isChannel && p.channel_id == id || p.isChat && p.chat_id == id
            }
            if (!alreadyExists) {
                // Log.d("add cached chat to results")
                chats.add(cachedChat)
                myResults.add(Peer.forChat(Chat(cachedChat).id))
            } else {
                // Log.d("cached chat already exists")
            }
            return
        }
        // cached user
        val cachedUser = DumpGroupMember.getUserFormCache(id)
        if (cachedUser != null) {
            // Log.d("found cached user: $cachedUser")
            // check if already in results
            val alreadyExists = myResults.any {
                val p = Peer(it)
                p.isUser && p.user_id == id
            } || globalResults.any {
                val p = Peer(it)
                p.isUser && p.user_id == id
            }
            if (!alreadyExists) {
                // Log.d("add cached user to results")
                users.add(cachedUser)
                myResults.add(Peer.forUser(User(cachedUser).id))
            } else {
                // Log.d("cached user already exists")
            }
            return
        }
        // channels in database
        val channelInfo = DumpGroupMember.queryChannelInfoById(id)
        if (channelInfo != null) {
            // Log.d("found channel info: $channelInfo")
            val chat = DumpGroupMember.createMinimalChannelChat(channelInfo)
            chats.add(chat)
            myResults.add(Peer.forChat(channelInfo.uid))
            return
        }
        // users in database
        val userInfo = DumpGroupMember.queryUserInfoById(id)
        if (userInfo != null) {
            val user = DumpGroupMember.getOrCreateUserObject(userInfo)
            // Log.d("found user info: $userInfo")
            users.add(user)
            myResults.add(Peer.forUser(userInfo.uid))
            return
        }
        // nothing found
        // Log.d("nothing found for id: $id")
    }

}
