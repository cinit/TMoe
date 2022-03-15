package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*
import java.util.*
import kotlin.collections.ArrayList

object ProhibitEnableReactions : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        val enabledReactionsList: List<Objects> = ArrayList()
        findMethod("org.telegram.messenger.MediaDataController"){ name=="getEnabledReactionsList" }.hookBefore {
            if (isEnabled) it.result=enabledReactionsList
        }

        findMethod("org.telegram.ui.ChatActivity"){ name=="selectReaction" }.hookBefore {
            if (isEnabled) it.result = null
        }

    }
}