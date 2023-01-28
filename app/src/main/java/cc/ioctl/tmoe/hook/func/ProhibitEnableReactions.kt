package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*
import java.util.*
import kotlin.collections.ArrayList

object ProhibitEnableReactions : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        val enabledReactionsList: List<Objects> = ArrayList()
        findMethod("org.telegram.messenger.MediaDataController") { name == "getEnabledReactionsList" }.hookBefore {
            if (isEnabled) it.result = enabledReactionsList
        }

        //private void selectReaction(MessageObject primaryMessage, ReactionsContainerLayout reactionsLayout, float x, float y, TLRPC.TL_availableReaction reaction, boolean fromDoubleTap, boolean bigEmoji) {
        findMethod("org.telegram.ui.ChatActivity") { name == "selectReaction" }.hookBefore {
            if (!isEnabled) return@hookBefore

            it.result = null
            val fromDoubleTap = it.args[5] as Boolean

            if (fromDoubleTap) {

                val BulletinFactory = findMethod("org.telegram.ui.Components.BulletinFactory") {
                    name == "of" && parameterTypes.size == 1
                }.invoke(null, it.thisObject)

                val createCopyBulletin = findMethod(BulletinFactory::class.java) {
                    name == "createCopyBulletin" && parameterTypes.size == 1
                }.invoke(BulletinFactory, "双击 666")

                findMethod(createCopyBulletin::class.java) {
                    name == "show" && parameterTypes.size == 0
                }.invoke(createCopyBulletin)


            }
        }


    }
}