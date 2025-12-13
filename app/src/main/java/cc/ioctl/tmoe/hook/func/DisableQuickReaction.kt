package cc.ioctl.tmoe.hook.func

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.util.HostInfo
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.config.ConfigManager
import cc.ioctl.tmoe.util.dex.DexMethodDescriptor
import com.github.kyuubiran.ezxhelper.init.InitFields.ezXClassLoader
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getAllClassesList
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.utils.tryOrLogFalse
import java.lang.reflect.Method

@FunctionHookEntry
object DisableQuickReaction : CommonDynamicHook() {
    fun getMethod(key: String, block: () -> Method): Method {
        val cache = ConfigManager.getCache()
        val currentHostVersionCode32 = HostInfo.getVersionCode()
        val lastVersion = cache.getIntOrDefault("cache_" + key + "_code", 0)

        if (currentHostVersionCode32 == lastVersion) {
            return DexMethodDescriptor(cache.getString("cache_" + key + "_method")).getMethodInstance(
                Initiator.getHostClassLoader()
            )
        }

        val method = block()
        val desc = DexMethodDescriptor(method)

        cache.putString("cache_" + key + "_method", desc.toString())
        cache.putInt("cache_" + key + "_code", currentHostVersionCode32)
        return method
    }

    override fun initOnce(): Boolean = tryOrLogFalse {
        getMethod("onItemClickListener_hasDoubleTap", {
            ezXClassLoader.getAllClassesList().asSequence()
                .filter { it.startsWith("org.telegram.ui.ChatActivity$") }
                .mapNotNull { loadClassOrNull(it) }
                .filter { it.methods.any { m -> m.name == "hasDoubleTap" } }.first()
                .findMethod { name == "hasDoubleTap" }
        }).hookBefore {
            if (isEnabled) it.result = false
        }

        return true
    }
}
