package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*

@FunctionHookEntry
object DisablePremiumStickerAnimation : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {
        // 禁用 Premium 贴纸动画
        findMethod(loadClass("org.telegram.messenger.MessageObject")){
            name=="isPremiumSticker"&&parameterTypes.size==1
        }.hookBefore {
            if (!isEnabled)return@hookBefore

            it.result = false
        }
    }
}