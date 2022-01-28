package cc.ioctl.tmoe.hook.func;

import cc.ioctl.tmoe.hook.base.CommonDynamicHook;

public class HideUserAvatar extends CommonDynamicHook {
    public static final HideUserAvatar INSTANCE = new HideUserAvatar();

    private HideUserAvatar() {
    }

    @Override
    public boolean initOnce() throws Exception {
        // TODO: 2022-01-28 implement this function
        return false;
    }

    /**
     * Include: natural users, bots
     */
    public int getSelectedUserCount() {
        return 0;
    }

    /**
     * Include: channels(channels and anonymous channel users are treated in the same way)
     */
    public int getSelectedChannelCount() {
        return 0;
    }

    /**
     * Include: group
     */
    public int getSelectedGroupCount() {
        return 0;
    }
}
