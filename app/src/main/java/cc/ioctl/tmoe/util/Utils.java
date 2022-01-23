package cc.ioctl.tmoe.util;

import android.util.Log;

import cc.ioctl.tmoe.BuildConfig;
import de.robv.android.xposed.XposedBridge;

public class Utils {

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
}
