package cc.ioctl.tmoe.hook.func

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.ui.LocaleController
import com.github.kyuubiran.ezxhelper.utils.*


@FunctionHookEntry
object MutualContactsBtnOption : CommonDynamicHook() {
    object MutualViewTag
    var needAddMutual: Boolean = false;
    override fun initOnce(): Boolean = tryOrLogFalse {
        val themeClzName = "org.telegram.ui.ActionBar.Theme"
        val themeGetColor = findMethod(themeClzName) { name == "getColor" && parameterCount == 2 }
        val themePlayerActionBarSelector =
            findField(themeClzName) { name == "key_player_actionBarSelector" }.get(null) as Int
        val themeWindowBackgroundWhiteGrayIcon =
            findField(themeClzName) { name == "key_windowBackgroundWhiteGrayIcon" }.get(null) as Int
        val themeCreateSelectorDrawable =
            findMethod(themeClzName) { name == "createSelectorDrawable" }

        Log.dx("${MutualContactsBtnOption.javaClass.name}: before onCreateViewHolder")
        findMethod("org.telegram.ui.Adapters.ContactsAdapter") { name == "onCreateViewHolder" }.hookBefore {
            needAddMutual = it.args[1] as Int == 0
        }

        Log.dx("${MutualContactsBtnOption.javaClass.name}: before findUserCell")
        findConstructor("org.telegram.ui.Cells.UserCell") { parameterTypes.size == 6 }.hookAfter {
            if (!isEnabled || !needAddMutual) return@hookAfter
            if (it.args[5] != null && it.args[5].javaClass.canonicalName != "$themeClzName.ResourcesProvider") return@hookAfter  // neko workaround
            val context = it.args[0] as Context
            val resourcesProvider = it.args[5]
            Log.dx("${MutualContactsBtnOption.javaClass.name}: findUserCell: init mutual view")
            val mutualView = ImageView(context)
            mutualView.tag = MutualViewTag
            mutualView.setImageResource(R.drawable.ic_round_swap_horiz_24)
            mutualView.scaleType = ImageView.ScaleType.CENTER
            mutualView.visibility = View.GONE
            mutualView.contentDescription =
                LocaleController.getString("MutualContact", R.string.MutualContact)
            mutualView.background = themeCreateSelectorDrawable.invoke(
                null,
                themeGetColor.invoke(
                    null,
                    themePlayerActionBarSelector, resourcesProvider
                )
            ) as Drawable
            mutualView.colorFilter = PorterDuffColorFilter(
                themeGetColor.invoke(
                    null,
                    themeWindowBackgroundWhiteGrayIcon, resourcesProvider
                ) as Int, PorterDuff.Mode.MULTIPLY
            )
            mutualView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            val layoutHelperCreateFrame =
                findMethod("org.telegram.ui.Components.LayoutHelper") {
                    name == "createFrame" && parameterCount == 7
                }
            val tgIsRtl =
                findField("org.telegram.messenger.LocaleController") { name == "isRTL" }
                    .get(null) as Boolean
            (it.thisObject as ViewGroup).addView(
                mutualView,
                layoutHelperCreateFrame.invoke(
                    null,
                    40, 40,
                    (if (tgIsRtl) Gravity.START else Gravity.END) or Gravity.CENTER_VERTICAL,
                    if (tgIsRtl) 8 else 0, 0,
                    if (tgIsRtl) 0 else 8, 0
                ) as ViewGroup.LayoutParams
            )
        }

        Log.dx("${MutualContactsBtnOption.javaClass.name}: before findUpdate")
        findMethod("org.telegram.ui.Cells.UserCell") { name == "update" }.hookAfter {
            val mutualView = (it.thisObject as ViewGroup).findViewWithTag<ImageView>(MutualViewTag)
                ?: return@hookAfter
            val currObj = it.thisObject.findFieldObject { name == "currentObject" }
            try {
                if (currObj.getObject("mutual_contact") as Boolean) {
                    mutualView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.d("${MutualContactsBtnOption.javaClass.name}: findUpdate: seems that not a user object")
            }
        }
    }
}