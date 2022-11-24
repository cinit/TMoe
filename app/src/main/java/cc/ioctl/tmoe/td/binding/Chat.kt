package cc.ioctl.tmoe.td.binding

import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.PrimTypes
import cc.ioctl.tmoe.util.Reflex
import java.lang.reflect.Field

class Chat(val `this$0`: Any) {

    companion object {

        @Volatile
        private var initialized = false
        private lateinit var klass: Class<*>

        private lateinit var _id: Field
        private lateinit var _title: Field
        private lateinit var _date: Field
        private lateinit var _flags: Field
        private lateinit var _creator: Field
        private lateinit var _kicked: Field
        private lateinit var _deactivated: Field
        private lateinit var _left: Field
        private lateinit var _photo: Field
        private lateinit var _version: Field
        private lateinit var _broadcast: Field
        private lateinit var _megagroup: Field
        private lateinit var _access_hash: Field
        private lateinit var _until_date: Field
        private lateinit var _moderator: Field
        private lateinit var _username: Field
        private lateinit var _min: Field
        private lateinit var _has_link: Field
        private lateinit var _gigagroup: Field
        private lateinit var _noforwards: Field

        // since 8.8.2
        private lateinit var _join_to_send: Field
        private lateinit var _join_request: Field

        // since 9.1.0
        private var _forum: Field? = null

        fun initialize() {
            if (!initialized) {
                synchronized(User::class.java) {
                    klass = Initiator.loadClass("org.telegram.tgnet.TLRPC\$Chat")
                    _id = Reflex.findField(klass, PrimTypes.LONG, "id")
                    _title = Reflex.findField(klass, String::class.java, "title")
                    _date = Reflex.findField(klass, PrimTypes.INT, "date")
                    _flags = Reflex.findField(klass, PrimTypes.INT, "flags")
                    _creator = Reflex.findField(klass, PrimTypes.BOOLEAN, "creator")
                    _kicked = Reflex.findField(klass, PrimTypes.BOOLEAN, "kicked")
                    _deactivated = Reflex.findField(klass, PrimTypes.BOOLEAN, "deactivated")
                    _left = Reflex.findField(klass, PrimTypes.BOOLEAN, "left")
                    _photo = Reflex.findField(klass, null, "photo")
                    _version = Reflex.findField(klass, PrimTypes.INT, "version")
                    _broadcast = Reflex.findField(klass, PrimTypes.BOOLEAN, "broadcast")
                    _megagroup = Reflex.findField(klass, PrimTypes.BOOLEAN, "megagroup")
                    _access_hash = Reflex.findField(klass, PrimTypes.LONG, "access_hash")
                    _until_date = Reflex.findField(klass, PrimTypes.INT, "until_date")
                    _moderator = Reflex.findField(klass, PrimTypes.BOOLEAN, "moderator")
                    _username = Reflex.findField(klass, String::class.java, "username")
                    _min = Reflex.findField(klass, PrimTypes.BOOLEAN, "min")
                    _has_link = Reflex.findField(klass, PrimTypes.BOOLEAN, "has_link")
                    _gigagroup = Reflex.findField(klass, PrimTypes.BOOLEAN, "gigagroup")
                    _noforwards = Reflex.findField(klass, PrimTypes.BOOLEAN, "noforwards")
                    _join_to_send = Reflex.findField(klass, PrimTypes.BOOLEAN, "join_to_send")
                    _join_request = Reflex.findField(klass, PrimTypes.BOOLEAN, "join_request")

                    _forum = try {
                        Reflex.findField(klass, PrimTypes.BOOLEAN, "forum")
                    } catch (ignored: NoSuchFieldException) {
                        null
                    }

                    initialized = true
                }
            }
        }
    }

    init {
        initialize()
        klass.cast(`this$0`)
    }

    var id: Long
        get() = _id.getLong(`this$0`)
        set(value) = _id.setLong(`this$0`, value)

    var title: String?
        get() = _title.get(`this$0`) as String?
        set(value) = _title.set(`this$0`, value)

    var date: Int
        get() = _date.getInt(`this$0`)
        set(value) = _date.setInt(`this$0`, value)

    var flags: Int
        get() = _flags.getInt(`this$0`)
        set(value) = _flags.setInt(`this$0`, value)

    var creator: Boolean
        get() = _creator.getBoolean(`this$0`)
        set(value) = _creator.setBoolean(`this$0`, value)

    var kicked: Boolean
        get() = _kicked.getBoolean(`this$0`)
        set(value) = _kicked.setBoolean(`this$0`, value)

    var deactivated: Boolean
        get() = _deactivated.getBoolean(`this$0`)
        set(value) = _deactivated.setBoolean(`this$0`, value)

    var left: Boolean
        get() = _left.getBoolean(`this$0`)
        set(value) = _left.setBoolean(`this$0`, value)

    var photo: Any?
        get() = _photo.get(`this$0`)
        set(value) = _photo.set(`this$0`, value)

    var version: Int
        get() = _version.getInt(`this$0`)
        set(value) = _version.setInt(`this$0`, value)

    var broadcast: Boolean
        get() = _broadcast.getBoolean(`this$0`)
        set(value) = _broadcast.setBoolean(`this$0`, value)

    var megagroup: Boolean
        get() = _megagroup.getBoolean(`this$0`)
        set(value) = _megagroup.setBoolean(`this$0`, value)

    var access_hash: Long
        get() = _access_hash.getLong(`this$0`)
        set(value) = _access_hash.setLong(`this$0`, value)

    var until_date: Int
        get() = _until_date.getInt(`this$0`)
        set(value) = _until_date.setInt(`this$0`, value)

    var moderator: Boolean
        get() = _moderator.getBoolean(`this$0`)
        set(value) = _moderator.setBoolean(`this$0`, value)

    var username: String?
        get() = _username.get(`this$0`) as String?
        set(value) = _username.set(`this$0`, value)

    var min: Boolean
        get() = _min.getBoolean(`this$0`)
        set(value) = _min.setBoolean(`this$0`, value)

    var has_link: Boolean
        get() = _has_link.getBoolean(`this$0`)
        set(value) = _has_link.setBoolean(`this$0`, value)

    var gigagroup: Boolean
        get() = _gigagroup.getBoolean(`this$0`)
        set(value) = _gigagroup.setBoolean(`this$0`, value)

    var noforwards: Boolean
        get() = _noforwards.getBoolean(`this$0`)
        set(value) = _noforwards.setBoolean(`this$0`, value)

    var join_to_send: Boolean
        get() = _join_to_send.getBoolean(`this$0`)
        set(value) = _join_to_send.setBoolean(`this$0`, value)

    var join_request: Boolean
        get() = _join_request.getBoolean(`this$0`)
        set(value) = _join_request.setBoolean(`this$0`, value)

    var forum: Boolean
        get() = _forum?.getBoolean(`this$0`) ?: false
        set(value) = _forum?.setBoolean(`this$0`, value) ?: Unit
}
