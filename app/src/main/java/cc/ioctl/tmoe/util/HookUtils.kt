package cc.ioctl.tmoe.util

import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import java.lang.reflect.Method

fun CommonDynamicHook.hookBeforeIfEnabled(method: Method, prior: Int = 50, callback: HookUtils.BeforeHookedMethod) {
    HookUtils.hookBeforeIfEnabled(this, method, prior, callback)
}

fun CommonDynamicHook.hookAfterIfEnabled(method: Method, prior: Int = 50, callback: HookUtils.AfterHookedMethod) {
    HookUtils.hookAfterIfEnabled(this, method, prior, callback)
}
