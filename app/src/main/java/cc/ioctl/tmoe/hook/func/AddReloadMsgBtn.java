package cc.ioctl.tmoe.hook.func;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry;
import cc.ioctl.tmoe.hook.base.CommonDynamicHook;
import cc.ioctl.tmoe.lifecycle.Parasitics;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.util.HookUtils;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@FunctionHookEntry
public class AddReloadMsgBtn extends CommonDynamicHook {

    public static final AddReloadMsgBtn INSTANCE = new AddReloadMsgBtn();

    private AddReloadMsgBtn() {
    }

    private static final int ID_RELOAD_BTN = 1001001;

    private Class<?> mActionBarMenuOnItemClickClass = null;
    private boolean mActionBarMenuOnItemClickHooked = false;

    private final XC_MethodHook mOnMenuItemClickHook = HookUtils.afterAlways(this, 49, param -> {
        int id = (int) param.args[0];
        if (id == ID_RELOAD_BTN) {
            Object chatActivity = Reflex.getFirstByType(param.thisObject, Initiator.loadClass("org.telegram.ui.ChatActivity"));
            requestReloadChatMessage(chatActivity);
        }
    });

    @Override
    public boolean initOnce() throws Exception {
        Class<?> kChatActivity = Initiator.loadClass("org.telegram.ui.ChatActivity");
        Field fHeaderItem = kChatActivity.getDeclaredField("headerItem");
        fHeaderItem.setAccessible(true);
        Class<?> kActionBarMenuItem = Initiator.loadClass("org.telegram.ui.ActionBar.ActionBarMenuItem");
        Method addSubItem = kActionBarMenuItem.getDeclaredMethod("addSubItem", int.class, int.class, CharSequence.class);
        addSubItem.setAccessible(true);
        Field fActionBar = Initiator.loadClass("org.telegram.ui.ActionBar.BaseFragment").getDeclaredField("actionBar");
        fActionBar.setAccessible(true);
        Method getActionBarMenuOnItemClick = Initiator.loadClass("org.telegram.ui.ActionBar.ActionBar").getDeclaredMethod("getActionBarMenuOnItemClick");
        Method createView = kChatActivity.getDeclaredMethod("createView", Context.class);
        HookUtils.hookAfterIfEnabled(this, createView, param -> {
            Context ctx = (Context) param.args[0];
            Parasitics.injectModuleResources(ctx.getResources());
            if (!mActionBarMenuOnItemClickHooked) {
                Object actionBar = fActionBar.get(param.thisObject);
                Object listener = getActionBarMenuOnItemClick.invoke(actionBar);
                Objects.requireNonNull(listener, "getActionBarMenuOnItemClick listener is null");
                mActionBarMenuOnItemClickClass = listener.getClass();
                Method onItemClick = mActionBarMenuOnItemClickClass.getDeclaredMethod("onItemClick", int.class);
                XposedBridge.hookMethod(onItemClick, mOnMenuItemClickHook);
                mActionBarMenuOnItemClickHooked = true;
            }
            Object headerItem = fHeaderItem.get(param.thisObject);
            if (headerItem != null) {
                FrameLayout cell = (FrameLayout) addSubItem.invoke(headerItem, ID_RELOAD_BTN, R.drawable.ic_refresh_24,
                        LocaleController.getString("ReloadMessage", R.string.ReloadMessage));
                assert cell != null;
                // move call to the second position
                ViewGroup parent = (ViewGroup) cell.getParent();
                Objects.requireNonNull(parent, "parent is null");
                if (parent.getChildCount() > 1) {
                    parent.removeView(cell);
                    parent.addView(cell, 1);
                }
            }
        });
        return true;
    }

    private void requestReloadChatMessage(Object chatActivity) throws Exception {
        Context ctx = (Context) Reflex.invokeVirtual(chatActivity, "getParentActivity");
        try {
            Method getMessagesController = Initiator.loadClass("org.telegram.ui.ActionBar.BaseFragment")
                    .getDeclaredMethod("getMessagesController");
            getMessagesController.setAccessible(true);
            Object messagesController = getMessagesController.invoke(chatActivity);
            // public void loadMessages(long dialogId, long mergeDialogId, boolean loadInfo, int count, int max_id,
            // int offset_date, boolean fromCache, int midDate, int classGuid, int load_type,
            // int last_message_id, int mode, int threadMessageId, int replyFirstUnread, int loadIndex)
            Method loadMessages = messagesController.getClass().getDeclaredMethod("loadMessages",
                    long.class, long.class, boolean.class, int.class, int.class,
                    int.class, boolean.class, int.class, int.class, int.class,
                    int.class, int.class, int.class, int.class, int.class);
            // loadMessages(dialog_id, mergeDialogId, false, 30, 0, date, true, 0, classGuid, 4, 0, chatMode, threadMessageId, replyMaxReadId, lastLoadIndex++);
            long dialogId = Reflex.getInstanceObject(chatActivity, "dialog_id", long.class);
            long mergeDialogId = Reflex.getInstanceObject(chatActivity, "mergeDialogId", long.class);
            int date = Math.toIntExact(System.currentTimeMillis() / 1000L);
            int classGuid = Reflex.getInstanceObject(chatActivity, "classGuid", int.class);
            int chatMode = Reflex.getInstanceObject(chatActivity, "chatMode", int.class);
            int threadMessageId = Reflex.getInstanceObject(chatActivity, "threadMessageId", int.class);
            int replyMaxReadId = Reflex.getInstanceObject(chatActivity, "replyMaxReadId", int.class);
            int lastLoadIndex = Reflex.getInstanceObject(chatActivity, "lastLoadIndex", int.class);
            loadMessages.invoke(messagesController, dialogId, mergeDialogId, false, 30, 0, date, true, 0, classGuid, 4, 0, chatMode, threadMessageId, replyMaxReadId, lastLoadIndex);
        } catch (Exception e) {
            new AlertDialog.Builder(ctx).setTitle(Reflex.getShortClassName(e)).setMessage(e.getMessage()).show();
        }
    }

}
