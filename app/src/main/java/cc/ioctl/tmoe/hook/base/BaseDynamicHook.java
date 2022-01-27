package cc.ioctl.tmoe.hook.base;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author cinit
 */
public abstract class BaseDynamicHook implements DynamicHook {
    /**
     * Check if the hook should be applied.
     *
     * @return true if the hook should work
     */
    public boolean isEnabled() {
        return isAvailable() && isEnabledByUser();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Nullable
    @Override
    public List<Throwable> getErrors() {
        return null;
    }

    @Override
    public boolean isPreparationRequired() {
        return false;
    }

    @Override
    public boolean makePreparations() {
        return true;
    }

    @Override
    public boolean isApplicationRestartRequired() {
        return false;
    }
}
