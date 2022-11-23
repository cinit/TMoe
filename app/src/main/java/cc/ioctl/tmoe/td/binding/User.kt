package cc.ioctl.tmoe.td.binding

import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.PrimTypes
import cc.ioctl.tmoe.util.Reflex
import java.lang.reflect.Field

class User(val `this$0`: Any) {

    companion object {

        @Volatile
        private var initialized = false
        private lateinit var klass: Class<*>
        private lateinit var _id: Field
        private lateinit var _first_name: Field
        private lateinit var _last_name: Field
        private lateinit var _username: Field
        private lateinit var _access_hash: Field
        private lateinit var _phone: Field
        private lateinit var _status: Field
        private lateinit var _flags: Field
        private lateinit var _contact: Field
        private lateinit var _mutual_contact: Field
        private lateinit var _deleted: Field
        private lateinit var _bot: Field
        private lateinit var _min: Field
        private lateinit var _flags2: Field


        fun initialize() {
            if (!initialized) {
                synchronized(User::class.java) {
                    klass = Initiator.loadClass("org.telegram.tgnet.TLRPC\$User")
                    _id = Reflex.findField(klass, PrimTypes.LONG, "id")
                    _first_name = Reflex.findField(klass, String::class.java, "first_name")
                    _last_name = Reflex.findField(klass, String::class.java, "last_name")
                    _username = Reflex.findField(klass, String::class.java, "username")
                    _access_hash = Reflex.findField(klass, PrimTypes.LONG, "access_hash")
                    _phone = Reflex.findField(klass, String::class.java, "phone")
                    _status = Reflex.findField(klass, null, "status")
                    _flags = Reflex.findField(klass, PrimTypes.INT, "flags")
                    _contact = Reflex.findField(klass, PrimTypes.BOOLEAN, "contact")
                    _mutual_contact = Reflex.findField(klass, PrimTypes.BOOLEAN, "mutual_contact")
                    _deleted = Reflex.findField(klass, PrimTypes.BOOLEAN, "deleted")
                    _bot = Reflex.findField(klass, PrimTypes.BOOLEAN, "bot")
                    _min = Reflex.findField(klass, PrimTypes.BOOLEAN, "min")
                    _flags2 = Reflex.findField(klass, PrimTypes.INT, "flags")
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
        set(value) {
            _id.setLong(`this$0`, value)
        }

    var first_name: String?
        get() = _first_name.get(`this$0`) as String?
        set(value) {
            _first_name.set(`this$0`, value)
        }

    var last_name: String?
        get() = _last_name.get(`this$0`) as String?
        set(value) {
            _last_name.set(`this$0`, value)
        }

    var username: String?
        get() = _username.get(`this$0`) as String?
        set(value) {
            _username.set(`this$0`, value)
        }

    var access_hash: Long
        get() = _access_hash.getLong(`this$0`)
        set(value) {
            _access_hash.setLong(`this$0`, value)
        }

    var phone: String?
        get() = _phone.get(`this$0`) as String?
        set(value) {
            _phone.set(`this$0`, value)
        }

    var status: Any?
        get() = _status.get(`this$0`) as String?
        set(value) {
            _status.set(`this$0`, value)
        }

    var flags: Int
        get() = _flags.getInt(`this$0`)
        set(value) {
            _flags.setInt(`this$0`, value)
        }

    var contact: Boolean
        get() = _contact.getBoolean(`this$0`)
        set(value) {
            _contact.setBoolean(`this$0`, value)
        }

    var mutual_contact: Boolean
        get() = _mutual_contact.getBoolean(`this$0`)
        set(value) {
            _mutual_contact.setBoolean(`this$0`, value)
        }

    var deleted: Boolean
        get() = _deleted.getBoolean(`this$0`)
        set(value) {
            _deleted.setBoolean(`this$0`, value)
        }

    var bot: Boolean
        get() = _bot.getBoolean(`this$0`)
        set(value) {
            _bot.setBoolean(`this$0`, value)
        }

    var min: Boolean
        get() = _min.getBoolean(`this$0`)
        set(value) {
            _min.setBoolean(`this$0`, value)
        }

    var flags2: Int
        get() = _flags2.getInt(`this$0`)
        set(value) {
            _flags2.setInt(`this$0`, value)
        }

    val name: String
        get() {
            val firstName = first_name ?: ""
            val lastName = last_name ?: ""
            return if (firstName.isEmpty() && lastName.isEmpty()) {
                id.toString()
            } else if (firstName.isEmpty()) {
                lastName
            } else if (lastName.isEmpty()) {
                firstName
            } else {
                "$firstName $lastName"
            }
        }
}
