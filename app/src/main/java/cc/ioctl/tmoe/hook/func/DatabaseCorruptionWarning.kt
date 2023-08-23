package cc.ioctl.tmoe.hook.func

import android.content.Context
import androidx.appcompat.app.AlertDialog
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.util.CommonContextWrapper
import cc.ioctl.tmoe.util.HookUtils
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.Log
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.util.*

@FunctionHookEntry
object DatabaseCorruptionWarning : CommonDynamicHook() {

    private lateinit var kSQLiteException: Class<out Exception>
    private lateinit var kSQLiteDatabase: Class<*>
    private lateinit var kSQLiteCursor: Class<*>
    private lateinit var kSQLitePreparedStatement: Class<*>

    private lateinit var fSQLiteDatabase_sqliteHandle: Field
    private lateinit var fSQLiteCursor_preparedStatement: Field
    private lateinit var fSQLitePreparedStatement_sqliteStatementHandle: Field

    private val mDatabaseRefLock = Any()
    private val mOpenedDatabaseRef = HashMap<Long, String>(4)
    private val mmStatementDatabaseMapLock = Any()
    private val mStatementDatabaseMap = object : LinkedHashMap<Long, Long>(100) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Long>?): Boolean {
            return size > 100
        }
    }

    @Volatile
    private var mIsIgnored = false

    @Volatile
    private var mIsShowingDialog = false

    @Volatile
    private var mErrorMsg: String? = null

    override fun initOnce(): Boolean {
        kSQLiteDatabase = Initiator.loadClass("org.telegram.SQLite.SQLiteDatabase")
        kSQLiteException = Initiator.loadClass("org.telegram.SQLite.SQLiteException") as Class<out Exception>
        kSQLiteCursor = Initiator.loadClass("org.telegram.SQLite.SQLiteCursor")
        kSQLitePreparedStatement = Initiator.loadClass("org.telegram.SQLite.SQLitePreparedStatement")
        fSQLiteDatabase_sqliteHandle = kSQLiteDatabase.getDeclaredField("sqliteHandle").apply {
            isAccessible = true
        }
        fSQLiteCursor_preparedStatement = kSQLiteCursor.getDeclaredField("preparedStatement").apply {
            isAccessible = true
        }
        fSQLitePreparedStatement_sqliteStatementHandle = kSQLitePreparedStatement.getDeclaredField("sqliteStatementHandle").apply {
            isAccessible = true
        }
        val queryFinalized = kSQLiteDatabase.getDeclaredMethod(
            "queryFinalized",
            String::class.java, Array<Any>::class.java
        )
        val next = kSQLiteCursor.getDeclaredMethod("next")
        val ctorSQLiteDatabase = kSQLiteDatabase.getDeclaredConstructor(String::class.java)
        val ctorSQLitePreparedStatement = kSQLitePreparedStatement.getDeclaredConstructor(
            kSQLiteDatabase, String::class.java
        )
        val kChatActivity = Initiator.loadClass("org.telegram.ui.ChatActivity")
        val createView = kChatActivity.getDeclaredMethod("createView", Context::class.java)
        HookUtils.hookAfterAlways(this, ctorSQLiteDatabase) { param ->
            val db = kSQLiteDatabase.cast(param.thisObject)!!
            val path = param.args[0] as String
            onOpenDatabase(db, path)
        }
        HookUtils.hookAfterAlways(this, ctorSQLitePreparedStatement) { param ->
            val statement = kSQLitePreparedStatement.cast(param.thisObject)!!
            val database = kSQLiteDatabase.cast(param.args[0])!!
            val hDatabase = fSQLiteDatabase_sqliteHandle.getLong(database)
            val hStmt = fSQLitePreparedStatement_sqliteStatementHandle.getLong(statement)
            onPrepareStatement(hStmt, hDatabase)
        }
        XposedBridge.hookMethod(queryFinalized, mAfterHookHandler)
        XposedBridge.hookMethod(next, mAfterHookHandler)
        HookUtils.hookAfterIfEnabled(this, createView) { param ->
            if (!mIsIgnored && !mIsShowingDialog && mErrorMsg != null) {
                val ctx = param.args[0] as Context
                showErrorDialog(ctx, mErrorMsg!!)
            }
        }
        return true
    }

    private val mAfterHookHandler = HookUtils.afterIfEnabled(this, 51) { param ->
        val ex = param.throwable
        if (ex != null && ex.javaClass == kSQLiteException) {
            val thiz = param.thisObject!!
            onSQLitException(thiz, ex as Exception)
        }
    }

    private fun onSQLitException(that: Any, oex: Exception) {
        val msg = oex.message
        if (msg != null) {
            if (msg.contains("database disk image is malformed")) {
                val hDatabase: Long = when (that.javaClass) {
                    kSQLiteDatabase -> {
                        fSQLiteDatabase_sqliteHandle.getLong(that)
                    }
                    kSQLitePreparedStatement -> {
                        findDatabaseWithStatement(that)
                    }
                    kSQLiteCursor -> {
                        findDatabaseWithCursor(that)
                    }
                    else -> {
                        0
                    }
                }
                val path = findPathWithDatabase(hDatabase)
                mErrorMsg = (path ?: if (hDatabase != 0L) {
                    String.format(Locale.ROOT, "<pDatabase = 0x%x>", hDatabase)
                } else "<handle unavailable>") + "\n" + Log.getStackTraceString(oex)
                // Log.w("DatabaseCorruptionWarning: $mErrorMsg")
            }
        }
    }

    private fun findPathWithDatabase(db: Any): String? {
        val h = fSQLiteDatabase_sqliteHandle.get(db)
        synchronized(mDatabaseRefLock) {
            return mOpenedDatabaseRef[h]
        }
    }

    private fun findPathWithDatabase(hDatabase: Long): String? {
        if (hDatabase == 0L) {
            return null
        }
        synchronized(mDatabaseRefLock) {
            return mOpenedDatabaseRef[hDatabase]
        }
    }

    private fun findDatabaseWithCursor(cursor: Any): Long {
        val stmt = fSQLiteCursor_preparedStatement.get(cursor) ?: return 0L
        return findDatabaseWithStatement(stmt)
    }

    private fun findDatabaseWithStatement(stmt: Any): Long {
        val h = fSQLitePreparedStatement_sqliteStatementHandle.getLong(stmt)
        synchronized(mDatabaseRefLock) {
            return mStatementDatabaseMap[h] ?: 0L
        }
    }

    private fun onOpenDatabase(db: Any, path: String) {
        val h = fSQLiteDatabase_sqliteHandle.getLong(db)
        Log.d("onOpenDatabase: db=$db, path=$path, h=" + String.format(Locale.ROOT, "0x%x", h))
        if (h == 0L) {
            return
        }
        synchronized(mDatabaseRefLock) {
            mOpenedDatabaseRef[h] = path
        }
    }

    private fun onPrepareStatement(hStmt: Long, hDatabase: Long) {
        synchronized(mmStatementDatabaseMapLock) {
            mStatementDatabaseMap[hStmt] = hDatabase
        }
    }

    private fun findDatabaseOfStatement(hStmt: Long): Long {
        synchronized(mmStatementDatabaseMapLock) {
            return mStatementDatabaseMap[hStmt] ?: 0L
        }
    }

    private fun showErrorDialog(context: Context, msg: String) {
        val ctx = CommonContextWrapper.createAppCompatContext(context)
        AlertDialog.Builder(ctx).apply {
            setTitle(LocaleController.getString("DatabaseCorruptionWarning", R.string.DatabaseCorruptionWarning))
            setMessage(msg)
            setPositiveButton(LocaleController.getString("Ignore", R.string.Ignore)) { _, _ ->
                mIsIgnored = true
                mIsShowingDialog = false
            }
            setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel)) { _, _ ->
                mIsShowingDialog = false
            }
            setOnDismissListener {
                mIsShowingDialog = false
            }
        }.show()
    }
}
