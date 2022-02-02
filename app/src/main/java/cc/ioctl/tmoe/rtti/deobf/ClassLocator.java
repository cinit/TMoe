package cc.ioctl.tmoe.rtti.deobf;

import javax.annotation.Nullable;

import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Utils;
import cc.ioctl.tmoe.util.dex.DexKit;

public class ClassLocator {

    private static Class<?> kThemeClass = null;
    private static boolean kThemeFastFail = false;

    @Nullable
    public static Class<?> getThemeClass() {
        if (kThemeClass != null) {
            return kThemeClass;
        }
        if (kThemeFastFail) {
            return null;
        }
        kThemeClass = Initiator.load("org.telegram.ui.ActionBar.Theme");
        if (kThemeClass != null) {
            return kThemeClass;
        }
        kThemeClass = DexKit.loadClassFromCache(DexKit.C_THEME);
        if (kThemeClass == null) {
            kThemeFastFail = true;
        }
        return kThemeClass;
    }

    @Nullable
    public static Class<?> findThemeClass() {
        if (getThemeClass() != null) {
            return kThemeClass;
        }
        kThemeClass = DexKit.doFindClass(DexKit.C_THEME);
        return kThemeClass;
    }

    // -----------------------------

    private static Class<?> kUserConfigClass = null;

    @Nullable
    public static Class<?> getUserConfigClass() {
        if (kUserConfigClass != null) {
            return kUserConfigClass;
        }
        kUserConfigClass = Initiator.load("org.telegram.messenger.UserConfig");
        // TODO: 2022-01-30 this class is obfuscated
        if (kUserConfigClass != null) {
            return kUserConfigClass;
        }
        Utils.loge("UserConfig class not found");
        return null;
    }

    // -----------------------------

    private static Class<?> kTlrpcUserClass = null;

    @Nullable
    public static Class<?> getTlrpcUserClass() {
        if (kTlrpcUserClass != null) {
            return kTlrpcUserClass;
        }
        kTlrpcUserClass = Initiator.load("org.telegram.tgnet.TLRPC$User");
        if (kTlrpcUserClass != null) {
            return kTlrpcUserClass;
        }
        Class<?> userConfig = getUserConfigClass();
        if (userConfig != null) {
            try {
                kTlrpcUserClass = userConfig.getDeclaredMethod("getCurrentUser").getReturnType();
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (kTlrpcUserClass != null) {
            return kTlrpcUserClass;
        }
        Utils.loge("TLRPC$User class not found");
        return null;
    }

    // -----------------------------

    private static Class<?> kTlrpcChatClass = null;

    @Nullable
    public static Class<?> getTlrpcChatClass() {
        if (kTlrpcChatClass != null) {
            return kTlrpcChatClass;
        }
        kTlrpcChatClass = Initiator.load("org.telegram.tgnet.TLRPC$Chat");
        if (kTlrpcChatClass != null) {
            return kTlrpcChatClass;
        }
        Class<?> profileActivity = Initiator.load("org.telegram.ui.ProfileActivity");
        if (profileActivity != null) {
            try {
                kTlrpcChatClass = profileActivity.getDeclaredField("currentChat").getType();
            } catch (NoSuchFieldException ignored) {
            }
        }
        if (kTlrpcChatClass != null) {
            return kTlrpcChatClass;
        }
        Utils.loge("TLRPC$Chat class not found");
        return null;
    }
}
