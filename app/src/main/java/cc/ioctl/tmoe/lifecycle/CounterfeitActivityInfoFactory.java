package cc.ioctl.tmoe.lifecycle;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.util.HostInfo;

public class CounterfeitActivityInfoFactory {

    @Nullable
    public static ActivityInfo makeProxyActivityInfo(@NonNull String className, int flags) {
        try {
            Context ctx = HostInfo.getApplication();
            Class<?> cl = Class.forName(className);
            try {
                ActivityInfo proto = ctx.getPackageManager().getActivityInfo(new ComponentName(
                        ctx.getPackageName(), "org.telegram.ui.LaunchActivity"), flags);
                if (AppCompatActivity.class.isAssignableFrom(cl)) {
                    // init style here, comment it out if it crashes on Android >= 10
                    // AppCompatActivity requires a style to be set
                    proto.theme = R.style.Theme_TMoe;
                }
                return initCommon(proto, className);
            } catch (PackageManager.NameNotFoundException e) {
                throw new IllegalStateException("LaunchActivity not found, are we in the host?", e);
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static ActivityInfo initCommon(ActivityInfo ai, String name) {
        ai.targetActivity = null;
        ai.taskAffinity = null;
        ai.descriptionRes = 0;
        ai.name = name;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ai.splitName = null;
        }
        return ai;
    }
}
