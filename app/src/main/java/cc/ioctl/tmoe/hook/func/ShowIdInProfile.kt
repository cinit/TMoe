package cc.ioctl.tmoe.hook.func

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.FrameLayout
import android.widget.Toast
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.hook.core.ProfileActivityRowHook
import cc.ioctl.tmoe.lifecycle.Parasitics
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.util.HostInfo
import cc.ioctl.tmoe.util.Reflex
import de.robv.android.xposed.XposedHelpers

@FunctionHookEntry
object ShowIdInProfile : CommonDynamicHook(), ProfileActivityRowHook.Callback {
    private val rowName = "SHOW_ID_IN_PROFILE"

    override fun initOnce(): Boolean {
        ProfileActivityRowHook.addCallback(this)
        return true
    }

    override fun onBindViewHolder(
        key: String,
        holder: Any,
        adpater: Any,
        profileActivity: Any
    ): Boolean {
        if (rowName != key) return false
        (Reflex.getInstanceObjectOrNull(holder, "itemView") as? FrameLayout)?.let { textCell ->
            Parasitics.injectModuleResources(HostInfo.getApplication().resources)
            val userId = getUserId(profileActivity)
            val realId = if (userId == 0L) getChatId(profileActivity) else userId
            val isUser = userId != 0L
            val title = if (isUser) LocaleController.getString("UserId", R.string.UserId)
            else LocaleController.getString("GroupOrChannelId", R.string.GroupOrChannelId)
            XposedHelpers.callMethod(textCell, "setTextAndValue", realId.toString(), title, false)
        }
        return true
    }

    override fun getItemViewType(key: String, adapter: Any, profileActivity: Any): Int {
        if (rowName == key) return 2 // VIEW_TYPE_TEXT_DETAIL
        return -1
    }

    override fun onItemClicked(key: String, adapter: Any, profileActivity: Any): Boolean {
        if (rowName != key) return false
        val userId = getUserId(profileActivity)
        val realId = if (userId == 0L) getChatId(profileActivity) else userId
        val context = HostInfo.getApplication()
        context.getSystemService(ClipboardManager::class.java)
            .setPrimaryClip(ClipData.newPlainText("", realId.toString()))
        Toast.makeText(context, LocaleController.getString("IdCopied", R.string.IdCopied), Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onInsertRow(
        manipulator: ProfileActivityRowHook.RowManipulator,
        profileActivity: Any
    ) {
        var row = manipulator.getRowIdForField("infoHeaderRow")
        row = if (row == -1) 1 else row + 1
        manipulator.insertRowAtPosition(rowName, row)
    }

    private fun getUserId(profileActivity: Any): Long {
        return XposedHelpers.getObjectField(profileActivity, "userId") as Long
    }

    private fun getChatId(profileActivity: Any): Long {
        val chat = XposedHelpers.getObjectField(profileActivity, "currentChat")
        if (chat != null) {
            return XposedHelpers.getObjectField(chat, "id") as Long
        }
        return 0L
    }
}