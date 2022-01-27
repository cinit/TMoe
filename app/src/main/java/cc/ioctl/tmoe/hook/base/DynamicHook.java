package cc.ioctl.tmoe.hook.base;

import java.util.List;

import javax.annotation.Nullable;

import cc.ioctl.tmoe.hook.core.Initializable;

/**
 * @author cinit
 */
public interface DynamicHook extends Initializable {

    /**
     * Check if the hook is initialized and ready to be used.
     * If initialization is not successful, the hook should not be used, and this method should return false.
     *
     * @return true if the hook is initialized and usable.
     */
    boolean isInitialized();

    /**
     * Initialize the hook.
     * Note that you MUST NOT take too much time to initialize the hook.
     * Because the initialization may be called in main thread.
     * Avoid time-consuming operations in this method.
     *
     * @return true if initialization is successful.
     */
    @Override
    boolean initialize();

    /**
     * Get the errors if anything goes wrong.
     * Note that this method has NOTHING to do with the initialization.
     *
     * @return the errors, null if no errors.
     */
    @Nullable
    List<Throwable> getErrors();

    /**
     * Whether the hook is enabled by user.
     *
     * @return true if the hook is enabled by user.
     */
    boolean isEnabledByUser();

    /**
     * Set the hook enabled by user.
     *
     * @param enabled true if the hook is enabled by user.
     */
    void setEnabledByUser(boolean enabled);

    /**
     * Check if the hook is compatible with the current application.
     * If the hook is not compatible, the hook should not be used, and initialize() shall NOT be called.
     * This method is called before initialize() on main thread.
     * Avoid time-consuming operations in this method.
     *
     * @return true if the hook is compatible
     */
    boolean isAvailable();

    /**
     * Some hooks may need to do some time-consuming operations before initialization.
     * Such as dex-deobfuscation, or some other operations.
     *
     * @return true if the hook wants to do some time-consuming operations before initialization.
     */
    boolean isPreparationRequired();

    /**
     * Make parameters for the hook.
     * This method is called before initialize() on background thread.
     * You may perform time-consuming operations in this method.
     *
     * @return true if the hook is prepared successfully and ready to initialize.
     */
    boolean makePreparations();

    /**
     * Is an application restart required to use this hook?
     *
     * @return true if a restart is required.
     */
    boolean isApplicationRestartRequired();
}
