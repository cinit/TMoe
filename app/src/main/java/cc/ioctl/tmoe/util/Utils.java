package cc.ioctl.tmoe.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import cc.ioctl.tmoe.BuildConfig;
import de.robv.android.xposed.XposedBridge;

public class Utils {

    private static final ExecutorService sExecutorService = Executors.newCachedThreadPool();

    public static void loge(String str) {
        Log.e("TMoe", str);
        try {
            XposedBridge.log(str);
        } catch (NoClassDefFoundError e) {
            Log.e("Xposed", str);
            Log.e("EdXposed-Bridge", str);
            Log.e("LSPosed-Bridge", str);
        }
    }

    public static void loge(Throwable th) {
        if (th == null) {
            return;
        }
        String msg = Log.getStackTraceString(th);
        Log.e("TMoe", msg);
        try {
            XposedBridge.log(th);
        } catch (NoClassDefFoundError e) {
            Log.e("Xposed", msg);
            Log.e("EdXposed-Bridge", msg);
            Log.e("LSPosed-Bridge", msg);
        }
    }

    public static void logd(String str) {
        if (BuildConfig.DEBUG) {
            Log.d("TMoe", str);
        }
    }

    public static void logi(String str) {
        try {
            Log.i("TMoe", str);
            XposedBridge.log(str);
        } catch (NoClassDefFoundError e) {
            Log.i("Xposed", str);
            Log.i("EdXposed-Bridge", str);
            Log.i("LSPosed-Bridge", str);
        }
    }

    public static void logw(String str) {
        Log.i("TMoe", str);
        try {
            XposedBridge.log(str);
        } catch (NoClassDefFoundError e) {
            Log.w("Xposed", str);
            Log.w("EdXposed-Bridge", str);
            Log.w("LSPosed-Bridge", str);
        }
    }

    public static void logw(Throwable th) {
        if (th == null) {
            return;
        }
        String msg = Log.getStackTraceString(th);
        Log.w("TMoe", msg);
        try {
            XposedBridge.log(th);
        } catch (NoClassDefFoundError e) {
            Log.w("Xposed", msg);
            Log.w("EdXposed-Bridge", msg);
            Log.w("LSPosed-Bridge", msg);
        }
    }

    public static void async(@Nullable Runnable r) {
        if (r == null) {
            return;
        }
        sExecutorService.execute(r);
    }

    private static Handler sAppHandler = null;

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (sAppHandler == null) {
            sAppHandler = new Handler(Looper.getMainLooper());
        }
        if (delay == 0) {
            sAppHandler.post(runnable);
        } else {
            sAppHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        if (sAppHandler == null) {
            return;
        }
        sAppHandler.removeCallbacks(runnable);
    }
}
