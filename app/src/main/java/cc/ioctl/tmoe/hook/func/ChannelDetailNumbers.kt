package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*

object ChannelDetailNumbers : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrLogFalse {
        //频道关注者显示详细人数 3k → 3000
        findMethod(loadClass("org.telegram.messenger.LocaleController")) {
            name == "formatShortNumber" && parameterTypes.size == 2
        }.hookBefore {
            if (!isEnabled) return@hookBefore

            val number = it.args[0] as Int
            val rounded = it.args[1] as IntArray?
            if (rounded != null) {
                rounded[0] = number
            }
            it.result = number.toString()
        }


    }
}