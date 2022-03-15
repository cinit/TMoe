package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*
import java.util.*
import kotlin.collections.ArrayList

object ProhibitSpoilers : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        findAllMethods("org.telegram.ui.Components.spoilers.SpoilerEffect"){ name=="addSpoilers" }.hookBefore {
            if (isEnabled) it.result=null
        }
    }
}