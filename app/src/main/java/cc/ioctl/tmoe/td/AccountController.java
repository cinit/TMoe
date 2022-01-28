package cc.ioctl.tmoe.td;

import java.lang.reflect.InvocationTargetException;

import cc.ioctl.tmoe.rtti.deobf.ClassLocator;
import cc.ioctl.tmoe.util.Utils;

/**
 * Static helper class for accessing the Telegram account status.
 *
 * @author cinit
 */
public class AccountController {
    private AccountController() {
        throw new AssertionError("no instance for you");
    }

    /**
     * Get the user id for the current active account slot.
     *
     * @param slot the account slot, non-negative
     * @return the user id, or 0 if the slot is not logged in, or exception occurs
     * @throws IllegalArgumentException if the slot is invalid
     */
    public static long getUserIdForSlot(int slot) {
        if (slot < 0 || slot > 32767) {
            throw new IllegalArgumentException("invalid slot: " + slot);
        }
        Class<?> kUserConfig = ClassLocator.getUserConfigClass();
        if (kUserConfig == null) {
            Utils.loge("getUserIdForSlot but UserConfig.class is null");
            return 0;
        }
        try {
            Object userConfig = kUserConfig.getMethod("getInstance", int.class).invoke(null, slot);
            return kUserConfig.getField("clientUserId").getLong(userConfig);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new LinkageError("unable to access UserConfig.getInstance(I).clientUserId", e);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException e) {
            Utils.loge(e);
        }
        return 0;
    }

    /**
     * Get the current active account slot.
     * Notice that Telegram DOES support multiple accounts and the ongoing transaction may NOT always be the active one.
     * AVOID USE THIS METHOD OR USE WITH CAUTION.
     *
     * @return the current active account slot, or -1 if exception occurs
     * @see #getUserIdForSlot(int)
     */
    public static int getCurrentActiveSlot() {
        Class<?> kUserConfig = ClassLocator.getUserConfigClass();
        if (kUserConfig == null) {
            Utils.loge("getCurrentActiveSlot but UserConfig.class is null");
            return -1;
        }
        try {
            return kUserConfig.getDeclaredField("selectedAccount").getInt(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new LinkageError("unable to access UserConfig.selectedAccount", e);
        } catch (NoSuchFieldException e) {
            Utils.loge(e);
        }
        return -1;
    }

    /**
     * Get current active account user id.
     * Notice that Telegram DOES support multiple accounts and the ongoing transaction may NOT always be the active one.
     * AVOID USING THIS METHOD OR USE WITH CAUTION.
     *
     * @return the current active user id, or 0 if not logged in, or exception occurs
     */
    public static long getCurrentActiveUserId() {
        int slot = getCurrentActiveSlot();
        if (slot < 0) {
            return 0;
        }
        return getUserIdForSlot(slot);
    }

    /**
     * Test if the current active account is logged in.
     *
     * @return false if the current active account is not logged in, or exception occurs
     */
    public static boolean isCurrentUserLoggedIn() {
        return getCurrentActiveUserId() != 0;
    }

    public static class NoUserLoginException extends Exception {
        public NoUserLoginException() {
            super("no user login");
        }

        public NoUserLoginException(String msg) {
            super(msg);
        }

        public NoUserLoginException(Throwable cause) {
            super(cause);
        }

        public NoUserLoginException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
