package cc.ioctl.tmoe.hook.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cc.ioctl.tmoe.util.Utils;
import cc.ioctl.tmoe.util.config.ConfigManager;
import cc.ioctl.tmoe.util.dex.DexKit;

public abstract class CommonDynamicHook extends BaseDynamicHook {
    private final ArrayList<Throwable> mErrors = new ArrayList<>();
    private static final String CFG_HOOK_ENABLE_SUFFIX = ".enabled";
    private final String mKeyName;
    private final int[] mDexDeobfList;
    private final boolean mDefEnabled;
    private boolean mInitialized = false;
    private boolean mInitializationResult = false;

    protected CommonDynamicHook(@NonNull String keyName, @Nullable int[] dexDeobfList, boolean defEnabled) {
        super();
        mKeyName = Objects.requireNonNull(keyName);
        mDexDeobfList = dexDeobfList;
        mDefEnabled = defEnabled;
    }

    protected CommonDynamicHook(@NonNull String keyName, @Nullable int[] dexDeobfList) {
        this(keyName, dexDeobfList, false);
    }

    protected CommonDynamicHook(@NonNull String keyName) {
        this(keyName, null);
    }

    protected CommonDynamicHook() {
        String className = getClass().getName();
        String[] parts = className.split("\\.");
        mKeyName = parts[parts.length - 1];
        mDexDeobfList = null;
        mDefEnabled = false;
    }

    public abstract boolean initOnce() throws Exception;

    @Override
    public boolean isInitialized() {
        return mInitialized && mInitializationResult;
    }

    @Override
    public boolean initialize() {
        if (mInitialized) {
            return mInitializationResult;
        }
        try {
            mInitializationResult = initOnce();
        } catch (Exception | LinkageError e) {
            mInitializationResult = false;
            mErrors.add(e);
        }
        mInitialized = true;
        return mInitializationResult;
    }

    @Override
    public boolean isEnabledByUser() {
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        return cfg.getBooleanOrDefault(mKeyName + CFG_HOOK_ENABLE_SUFFIX, mDefEnabled);
    }

    @Override
    public void setEnabledByUser(boolean enabled) {
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        cfg.putBoolean(mKeyName + CFG_HOOK_ENABLE_SUFFIX, enabled);
        cfg.commit();
    }

    public void logError(Throwable e) {
        // check if there is already an error with the same error message and stack trace
        boolean alreadyLogged = false;
        for (Throwable error : mErrors) {
            if (Objects.equals(error.getMessage(), e.getMessage())
                    && Arrays.equals(error.getStackTrace(), e.getStackTrace())) {
                alreadyLogged = true;
            }
        }
        if (!alreadyLogged) {
            mErrors.add(e);
        }
        Utils.loge(e);
    }

    protected void loge(Throwable e) {
        logError(e);
    }

    protected void loge(String msg) {
        Throwable e = new Throwable(msg);
        logError(e);
    }

    @Nullable
    @Override
    public List<Throwable> getErrors() {
        return mErrors;
    }

    @Override
    public boolean isPreparationRequired() {
        if (mDexDeobfList == null || mDexDeobfList.length == 0) {
            return false;
        }
        for (int dexDeobf : mDexDeobfList) {
            if (DexKit.isDeobfuscationRequiredFor(dexDeobf)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean makePreparations() {
        if (mDexDeobfList == null || mDexDeobfList.length == 0) {
            // nothing to do
            return true;
        }
        boolean failed = false;
        for (int dexDeobf : mDexDeobfList) {
            if (!DexKit.prepareFor(dexDeobf)) {
                failed = true;
            }
        }
        return !failed;
    }
}
