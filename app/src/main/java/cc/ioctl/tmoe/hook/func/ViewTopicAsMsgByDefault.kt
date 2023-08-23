package cc.ioctl.tmoe.hook.func

import android.os.Bundle
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.util.HookUtils
import cc.ioctl.tmoe.util.Initiator

@FunctionHookEntry
object ViewTopicAsMsgByDefault : CommonDynamicHook() {

    override fun initOnce(): Boolean {
        val kDialogsActivity = Initiator.loadClass("org.telegram.ui.DialogsActivity")
        val kBaseFragment = Initiator.loadClass("org.telegram.ui.ActionBar.BaseFragment")
        val kTopicsFragment = Initiator.loadClass("org.telegram.ui.TopicsFragment")
        val kChatActivity = Initiator.loadClass("org.telegram.ui.ChatActivity")
        val getArguments_BaseFragment = kBaseFragment.getDeclaredMethod("getArguments")
        val presentFragment_DialogsActivity = kDialogsActivity.getDeclaredMethod("presentFragment", kBaseFragment)
        val isTopic_ChatActivity = kChatActivity.getDeclaredField("isTopic").apply {
            isAccessible = true
        }
        HookUtils.hookBeforeIfEnabled(this, presentFragment_DialogsActivity) { param ->
            val fragment = param.args[0]
            val args = getArguments_BaseFragment.invoke(fragment) as Bundle?
            if (args == null || args.getBoolean("forward_to", false) || args.getBoolean("for_select", false)) {
                // do not intercept forward and select
                return@hookBeforeIfEnabled
            }
            if (fragment.javaClass == kTopicsFragment) {
                val chatId = args.getLong("chat_id", 0)
                if (chatId == 0L) {
                    // do not intercept chat_id == 0
                    return@hookBeforeIfEnabled
                }
                val newBundle = Bundle().apply {
                    putLong("chat_id", chatId)
                }
                val newFragment = kChatActivity.getConstructor(Bundle::class.java).newInstance(newBundle)
                param.args[0] = newFragment
                // control flow continues.
            } else if (fragment.javaClass == kChatActivity) {
                if (isTopic_ChatActivity.getBoolean(fragment)) {
                    // unset forum by replacing with a new fragment
                    val newFragment = kChatActivity.getConstructor(Bundle::class.java).newInstance(args)
                    param.args[0] = newFragment
                }
            }
        }
        return true
    }

}
