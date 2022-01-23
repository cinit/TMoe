package cc.ioctl.tmoe.ui;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import java.util.HashMap;

import cc.ioctl.tmoe.BuildConfig;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Utils;

public class LayoutHelper {

    public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static final RectF rectTmp = new RectF();
    public static final Rect rectTmp2 = new Rect();

    private static int getSize(float size) {
        return (int) (size < 0 ? size : dip2px(size));
    }

    private static int getAbsoluteGravity(int gravity) {
        return Gravity.getAbsoluteGravity(gravity, LocaleController.isRTL() ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
    }

    @SuppressLint("RtlHardcoded")
    public static int getAbsoluteGravityStart() {
        return LocaleController.isRTL() ? Gravity.RIGHT : Gravity.LEFT;
    }

    @SuppressLint("RtlHardcoded")
    public static int getAbsoluteGravityEnd() {
        return LocaleController.isRTL() ? Gravity.LEFT : Gravity.RIGHT;
    }

    public static float dip2px(float dip) {
        Resources r = HostInfo.getApplication().getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
    }

    public static int dp(float dp) {
        return (int) dip2px(dp);
    }

    public static float dpf2(float value) {
        return HostInfo.getApplication().getResources().getDisplayMetrics().density * value;
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height, int gravity, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
        layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin));
        return layoutParams;
    }

    public static FrameLayout.LayoutParams createFrame(int width, int height, int gravity) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height));
    }

    public static FrameLayout.LayoutParams createFrame(float width, float height, int gravity) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
    }

    private static final HashMap<String, Typeface> typefaceCache = new HashMap<>();

    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t = null;
                    switch (assetPath) {
                        case "fonts/rmedium.ttf":
                            t = Typeface.create("sans-serif-medium", Typeface.NORMAL);
                            break;
                        case "fonts/ritalic.ttf":
                            t = Typeface.create("sans-serif", Typeface.ITALIC);
                            break;
                        case "fonts/rmediumitalic.ttf":
                            t = Typeface.create("sans-serif-medium", Typeface.ITALIC);
                            break;
                        case "fonts/rmono.ttf":
                            t = Typeface.MONOSPACE;
                            break;
                        case "fonts/mw_bold.ttf":
                            t = Typeface.create("serif", Typeface.BOLD);
                            break;
                        default:
                            // should not reach here
                            break;
                    }
                    if (t == null) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            Typeface.Builder builder = new Typeface.Builder(HostInfo.getApplication().getAssets(), assetPath);
                            if (assetPath.contains("medium")) {
                                builder.setWeight(700);
                            }
                            if (assetPath.contains("italic")) {
                                builder.setItalic(true);
                            }
                            t = builder.build();
                        } else {
                            t = Typeface.createFromAsset(HostInfo.getApplication().getAssets(), assetPath);
                        }
                    }
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Utils.loge("Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    }
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

}
