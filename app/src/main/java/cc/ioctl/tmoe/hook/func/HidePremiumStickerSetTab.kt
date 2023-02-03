package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.util.HookUtils
import cc.ioctl.tmoe.util.Initiator
import java.util.ArrayList

object HidePremiumStickerSetTab : CommonDynamicHook() {

    private const val TYPE_PREMIUM_STICKERS: Int = 7

    override fun initOnce(): Boolean {
        val kMediaDataController = Initiator.loadClass("org.telegram.messenger.MediaDataController")
        val getRecentStickers = try {
            kMediaDataController.getDeclaredMethod("getRecentStickers",Int::class.javaPrimitiveType,Int::class.javaPrimitiveType)
        } catch (ignored: NoSuchMethodException) {
            kMediaDataController.getDeclaredMethod("getRecentStickers",Int::class.javaPrimitiveType)
        }

        HookUtils.hookBeforeIfEnabled(this, getRecentStickers) {
            val args = it.args
            if (args[0] == TYPE_PREMIUM_STICKERS) {
                it.result = ArrayList<Any>()
            }
        }
        return true
    }

}
