package cc.ioctl.tmoe.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import cc.ioctl.tmoe.lifecycle.Parasitics;
import cc.ioctl.tmoe.ui.Theme;

/**
 * If you just want to create a MaterialDialog or AppCompatDialog, see
 * {@link #createAppCompatContext(Context)}
 **/
public class CommonContextWrapper extends ContextThemeWrapper {

    /**
     * Creates a new context wrapper with the specified theme with correct module ClassLoader.
     *
     * @param base  the base context
     * @param theme the resource ID of the theme to be applied on top of the base context's theme
     */
    public CommonContextWrapper(@NonNull Context base, int theme) {
        this(base, theme, null);
    }

    /**
     * Creates a new context wrapper with the specified theme with correct module ClassLoader.
     *
     * @param base          the base context
     * @param theme         the resource ID of the theme to be applied on top of the base context's theme
     * @param configuration the configuration to override the base one
     */
    public CommonContextWrapper(@NonNull Context base, int theme,
                                @Nullable Configuration configuration) {
        super(base, theme);
        if (configuration != null) {
            mOverrideResources = base.createConfigurationContext(configuration).getResources();
        }
        Parasitics.injectModuleResources(getResources());
    }

    private ClassLoader mXref = null;
    private Resources mOverrideResources;

    @NonNull
    @Override
    public ClassLoader getClassLoader() {
        if (mXref == null) {
            mXref = new SavedInstanceStatePatchedClassReferencer(
                    CommonContextWrapper.class.getClassLoader());
        }
        return mXref;
    }

    @Nullable
    private static Configuration recreateNighModeConfig(@NonNull Context base, int uiNightMode) {
        Objects.requireNonNull(base, "base is null");
        Configuration baseConfig = base.getResources().getConfiguration();
        if ((baseConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == uiNightMode) {
            // config for base context is already what we want,
            // just return null to avoid unnecessary override
            return null;
        }
        Configuration conf = new Configuration();
        conf.uiMode = uiNightMode | (baseConfig.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
        return conf;
    }

    @NonNull
    @Override
    public Resources getResources() {
        if (mOverrideResources == null) {
            return super.getResources();
        } else {
            return mOverrideResources;
        }
    }

    public static boolean isAppCompatContext(@NonNull Context context) {
        if (!checkContextClassLoader(context)) {
            return false;
        }
        TypedArray a = context.obtainStyledAttributes(androidx.appcompat.R.styleable.AppCompatTheme);
        try {
            return a.hasValue(androidx.appcompat.R.styleable.AppCompatTheme_windowActionBar);
        } finally {
            a.recycle();
        }
    }

    public static boolean checkContextClassLoader(@NonNull Context context) {
        try {
            ClassLoader cl = context.getClassLoader();
            if (cl == null) {
                return false;
            }
            return cl.loadClass(AppCompatActivity.class.getName()) == AppCompatActivity.class;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static int getNightModeMasked() {
        return Theme.isDarkTheme() ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
    }

    @NonNull
    public static Context createAppCompatContext(@NonNull Context base) {
        if (isAppCompatContext(base)) {
            return base;
        }
        return new CommonContextWrapper(base, androidx.appcompat.R.style.Theme_AppCompat_DayNight,
                recreateNighModeConfig(base, getNightModeMasked()));
    }
}
