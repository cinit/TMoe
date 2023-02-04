package cc.ioctl.tmoe.hook.func

import android.widget.Toast
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.lifecycle.Parasitics
import cc.ioctl.tmoe.util.HostInfo
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse
import java.util.Objects

object ProhibitEnableReactions : CommonDynamicHook() {

    private val enabledReactionsList: List<Objects> = ArrayList()
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

                val ctx = HostInfo.getApplication()
                Parasitics.injectModuleResources(ctx.resources)
                Toast.makeText(ctx, R.string.DebugVerbose_DoubleTapDetected, Toast.LENGTH_SHORT).show()

            }
        }


    }
}