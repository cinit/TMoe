package cc.ioctl.tmoe.td.binding

import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.PrimTypes
import cc.ioctl.tmoe.util.Reflex
import java.lang.reflect.Field

class Peer(val `this$0`: Any) {

    companion object {

        @Volatile
        private var initialized = false
        private lateinit var klass: Class<*>
        private lateinit var kTL_peerChannel: Class<*>
        private lateinit var kTL_peerChat: Class<*>
        private lateinit var kTL_peerUser: Class<*>

        private lateinit var _user_id: Field
        private lateinit var _chat_id: Field
        private lateinit var _channel_id: Field

        fun initialize() {
            if (!initialized) {
                synchronized(User::class.java) {
                    klass = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Peer")
                    kTL_peerChannel = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_peerChannel")
                    kTL_peerChat = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_peerChat")
                    kTL_peerUser = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_peerUser")
                    _user_id = Reflex.findField(klass, PrimTypes.LONG, "user_id")
                    _chat_id = Reflex.findField(klass, PrimTypes.LONG, "chat_id")
                    _channel_id = Reflex.findField(klass, PrimTypes.LONG, "channel_id")
                    initialized = true
                }
            }
        }

        @JvmStatic
        fun forUser(uid: Long): Any {
            check(uid > 0)
            initialize()
            val peer = Reflex.newInstance(kTL_peerUser)
            _user_id.setLong(peer, uid)
            return peer
        }

        @JvmStatic
        fun forChat(cid: Long): Any {
            check(cid > 0)
            initialize()
            val peer = Reflex.newInstance(kTL_peerChat)
            _chat_id.setLong(peer, cid)
            return peer
        }

        @JvmStatic
        fun forChannel(cid: Long): Any {
            check(cid > 0)
            initialize()
            val peer = Reflex.newInstance(kTL_peerChannel)
            _channel_id.setLong(peer, cid)
            return peer
        }

    }

    init {
        initialize()
        klass.cast(`this$0`)
    }

    var user_id: Long
        get() = _user_id.getLong(`this$0`)
        set(value) = _user_id.setLong(`this$0`, value)

    var chat_id: Long
        get() = _chat_id.getLong(`this$0`)
        set(value) = _chat_id.setLong(`this$0`, value)

    var channel_id: Long
        get() = _channel_id.getLong(`this$0`)
        set(value) = _channel_id.setLong(`this$0`, value)

    val isChannel: Boolean
        get() = kTL_peerChannel.isInstance(`this$0`)

    val isChat: Boolean
        get() = kTL_peerChat.isInstance(`this$0`)

    val isUser: Boolean
        get() = kTL_peerUser.isInstance(`this$0`)

}
