package cc.ioctl.tmoe.hook.func

import android.content.Context
import android.widget.Toast
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.findField
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse
import java.util.*

object ProhibitEnableReactions : CommonDynamicHook() {

    private val enabledReactionsList: List<Objects> = ArrayList()
    private var applicationContext: Context? = null
    override fun initOnce(): Boolean = tryOrLogFalse {


        findMethod("org.telegram.messenger.MediaDataController") { name == "getEnabledReactionsList" }.hookBefore {
            if (isEnabled) it.result = enabledReactionsList
        }


// old:   private void selectReaction(MessageObject primaryMessage, ReactionsContainerLayout reactionsLayout, float x, float y, TLRPC.TL_availableReaction reaction, boolean fromDoubleTap, boolean bigEmoji) {
//        private void selectReaction(MessageObject primaryMessage, ReactionsContainerLayout reactionsLayout, View fromView, float x, float y, ReactionsLayoutInBubble.VisibleReaction visibleReaction, boolean fromDoubleTap, boolean bigEmoji, boolean addToRecent) {
        findMethod("org.telegram.ui.ChatActivity") { name == "selectReaction" }.hookBefore {
            if (!isEnabled) return@hookBefore

            it.result = null
//            Log.d(Arrays.toString(it.args))

            val fromDoubleTap = it.args[6] as Boolean

            if (fromDoubleTap) {

//                val BulletinFactory = findMethod("org.telegram.ui.Components.BulletinFactory") {
//                    name == "of" && parameterTypes.size == 1
//                }.invoke(null, it.thisObject)
//
//                val createCopyBulletin = findMethod(BulletinFactory::class.java) {
//                    name == "createCopyBulletin" && parameterTypes.size == 1
//                }.invoke(BulletinFactory, "双击 666")
//
//                findMethod(createCopyBulletin::class.java) {
//                    name == "show" && parameterTypes.size == 0
//                }.invoke(createCopyBulletin)

                //org.telegram.messenger.ApplicationLoader applicationContext
                if (applicationContext == null) {
                    applicationContext =
                        findField("org.telegram.messenger.ApplicationLoader") { name == "applicationContext" }.get(
                            null
                        ) as Context?
                }

                Toast.makeText(applicationContext, "双击 666", Toast.LENGTH_SHORT).show()

            }
        }


    }
}