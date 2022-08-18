package cc.ioctl.tmoe.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.StateSet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import cc.ioctl.tmoe.rtti.deobf.ClassLocator;
import cc.ioctl.tmoe.util.Utils;

public class Theme {
    private Theme() {
        throw new IllegalStateException("no instance");
    }

    public static final String key_windowBackgroundWhite = "windowBackgroundWhite";
    public static final String key_windowBackgroundUnchecked = "windowBackgroundUnchecked";
    public static final String key_windowBackgroundChecked = "windowBackgroundChecked";
    public static final String key_windowBackgroundCheckText = "windowBackgroundCheckText";
    public static final String key_progressCircle = "progressCircle";
    public static final String key_listSelector = "listSelectorSDK21";
    public static final String key_windowBackgroundGray = "windowBackgroundGray";
    public static final String key_windowBackgroundGrayShadow = "windowBackgroundGrayShadow";
    public static final String key_windowBackgroundWhiteInputField = "windowBackgroundWhiteInputField";
    public static final String key_windowBackgroundWhiteInputFieldActivated = "windowBackgroundWhiteInputFieldActivated";
    public static final String key_windowBackgroundWhiteGrayIcon = "windowBackgroundWhiteGrayIcon";
    public static final String key_windowBackgroundWhiteBlueText = "windowBackgroundWhiteBlueText";
    public static final String key_windowBackgroundWhiteBlueText2 = "windowBackgroundWhiteBlueText2";
    public static final String key_windowBackgroundWhiteBlueText3 = "windowBackgroundWhiteBlueText3";
    public static final String key_windowBackgroundWhiteBlueText4 = "windowBackgroundWhiteBlueText4";
    public static final String key_windowBackgroundWhiteBlueText5 = "windowBackgroundWhiteBlueText5";
    public static final String key_windowBackgroundWhiteBlueText6 = "windowBackgroundWhiteBlueText6";
    public static final String key_windowBackgroundWhiteBlueText7 = "windowBackgroundWhiteBlueText7";
    public static final String key_windowBackgroundWhiteBlueButton = "windowBackgroundWhiteBlueButton";
    public static final String key_windowBackgroundWhiteBlueIcon = "windowBackgroundWhiteBlueIcon";
    public static final String key_windowBackgroundWhiteGreenText = "windowBackgroundWhiteGreenText";
    public static final String key_windowBackgroundWhiteGreenText2 = "windowBackgroundWhiteGreenText2";
    public static final String key_windowBackgroundWhiteRedText = "windowBackgroundWhiteRedText";
    public static final String key_windowBackgroundWhiteRedText2 = "windowBackgroundWhiteRedText2";
    public static final String key_windowBackgroundWhiteRedText3 = "windowBackgroundWhiteRedText3";
    public static final String key_windowBackgroundWhiteRedText4 = "windowBackgroundWhiteRedText4";
    public static final String key_windowBackgroundWhiteRedText5 = "windowBackgroundWhiteRedText5";
    public static final String key_windowBackgroundWhiteRedText6 = "windowBackgroundWhiteRedText6";
    public static final String key_windowBackgroundWhiteGrayText = "windowBackgroundWhiteGrayText";
    public static final String key_windowBackgroundWhiteGrayText2 = "windowBackgroundWhiteGrayText2";
    public static final String key_windowBackgroundWhiteGrayText3 = "windowBackgroundWhiteGrayText3";
    public static final String key_windowBackgroundWhiteGrayText4 = "windowBackgroundWhiteGrayText4";
    public static final String key_windowBackgroundWhiteGrayText5 = "windowBackgroundWhiteGrayText5";
    public static final String key_windowBackgroundWhiteGrayText6 = "windowBackgroundWhiteGrayText6";
    public static final String key_windowBackgroundWhiteGrayText7 = "windowBackgroundWhiteGrayText7";
    public static final String key_windowBackgroundWhiteGrayText8 = "windowBackgroundWhiteGrayText8";
    public static final String key_windowBackgroundWhiteGrayLine = "windowBackgroundWhiteGrayLine";
    public static final String key_windowBackgroundWhiteBlackText = "windowBackgroundWhiteBlackText";
    public static final String key_windowBackgroundWhiteHintText = "windowBackgroundWhiteHintText";
    public static final String key_windowBackgroundWhiteValueText = "windowBackgroundWhiteValueText";
    public static final String key_windowBackgroundWhiteLinkText = "windowBackgroundWhiteLinkText";
    public static final String key_windowBackgroundWhiteLinkSelection = "windowBackgroundWhiteLinkSelection";
    public static final String key_windowBackgroundWhiteBlueHeader = "windowBackgroundWhiteBlueHeader";

    public static final String key_dialogBackground = "dialogBackground";
    public static final String key_dialogBackgroundGray = "dialogBackgroundGray";
    public static final String key_dialogTextBlack = "dialogTextBlack";
    public static final String key_dialogTextLink = "dialogTextLink";
    public static final String key_dialogLinkSelection = "dialogLinkSelection";
    public static final String key_dialogTextRed = "dialogTextRed";
    public static final String key_dialogTextRed2 = "dialogTextRed2";
    public static final String key_dialogTextBlue = "dialogTextBlue";
    public static final String key_dialogTextBlue2 = "dialogTextBlue2";
    public static final String key_dialogTextBlue3 = "dialogTextBlue3";
    public static final String key_dialogTextBlue4 = "dialogTextBlue4";
    public static final String key_dialogTextGray = "dialogTextGray";
    public static final String key_dialogTextGray2 = "dialogTextGray2";
    public static final String key_dialogTextGray3 = "dialogTextGray3";
    public static final String key_dialogTextGray4 = "dialogTextGray4";
    public static final String key_dialogTextHint = "dialogTextHint";
    public static final String key_dialogInputField = "dialogInputField";
    public static final String key_dialogInputFieldActivated = "dialogInputFieldActivated";
    public static final String key_dialogCheckboxSquareBackground = "dialogCheckboxSquareBackground";
    public static final String key_dialogCheckboxSquareCheck = "dialogCheckboxSquareCheck";
    public static final String key_dialogCheckboxSquareUnchecked = "dialogCheckboxSquareUnchecked";
    public static final String key_dialogCheckboxSquareDisabled = "dialogCheckboxSquareDisabled";
    public static final String key_dialogScrollGlow = "dialogScrollGlow";
    public static final String key_dialogRoundCheckBox = "dialogRoundCheckBox";
    public static final String key_dialogRoundCheckBoxCheck = "dialogRoundCheckBoxCheck";
    public static final String key_dialogBadgeBackground = "dialogBadgeBackground";
    public static final String key_dialogBadgeText = "dialogBadgeText";
    public static final String key_dialogRadioBackground = "dialogRadioBackground";
    public static final String key_dialogRadioBackgroundChecked = "dialogRadioBackgroundChecked";
    public static final String key_dialogProgressCircle = "dialogProgressCircle";
    public static final String key_dialogLineProgress = "dialogLineProgress";
    public static final String key_dialogLineProgressBackground = "dialogLineProgressBackground";
    public static final String key_dialogButton = "dialogButton";
    public static final String key_dialogButtonSelector = "dialogButtonSelector";
    public static final String key_dialogIcon = "dialogIcon";
    public static final String key_dialogRedIcon = "dialogRedIcon";
    public static final String key_dialogGrayLine = "dialogGrayLine";
    public static final String key_dialogTopBackground = "dialogTopBackground";
    public static final String key_dialogCameraIcon = "dialogCameraIcon";
    public static final String key_dialog_inlineProgressBackground = "dialog_inlineProgressBackground";
    public static final String key_dialog_inlineProgress = "dialog_inlineProgress";
    public static final String key_dialogSearchBackground = "dialogSearchBackground";
    public static final String key_dialogSearchHint = "dialogSearchHint";
    public static final String key_dialogSearchIcon = "dialogSearchIcon";
    public static final String key_dialogSearchText = "dialogSearchText";
    public static final String key_dialogFloatingButton = "dialogFloatingButton";
    public static final String key_dialogFloatingButtonPressed = "dialogFloatingButtonPressed";
    public static final String key_dialogFloatingIcon = "dialogFloatingIcon";
    public static final String key_dialogShadowLine = "dialogShadowLine";
    public static final String key_dialogEmptyImage = "dialogEmptyImage";
    public static final String key_dialogEmptyText = "dialogEmptyText";
    public static final String key_dialogSwipeRemove = "dialogSwipeRemove";

    public static final String key_switchTrack = "switchTrack";
    public static final String key_switchTrackChecked = "switchTrackChecked";
    public static final String key_switchTrackBlue = "switchTrackBlue";
    public static final String key_switchTrackBlueChecked = "switchTrackBlueChecked";
    public static final String key_switchTrackBlueThumb = "switchTrackBlueThumb";
    public static final String key_switchTrackBlueThumbChecked = "switchTrackBlueThumbChecked";
    public static final String key_switchTrackBlueSelector = "switchTrackBlueSelector";
    public static final String key_switchTrackBlueSelectorChecked = "switchTrackBlueSelectorChecked";
    public static final String key_switch2Track = "switch2Track";
    public static final String key_switch2TrackChecked = "switch2TrackChecked";
    public static final String key_checkboxSquareBackground = "checkboxSquareBackground";
    public static final String key_checkboxSquareCheck = "checkboxSquareCheck";
    public static final String key_checkboxSquareUnchecked = "checkboxSquareUnchecked";
    public static final String key_checkboxSquareDisabled = "checkboxSquareDisabled";

    public static final String key_divider = "divider";
    public static final String key_graySection = "graySection";
    public static final String key_graySectionText = "key_graySectionText";
    public static final String key_radioBackground = "radioBackground";
    public static final String key_radioBackgroundChecked = "radioBackgroundChecked";
    public static final String key_checkbox = "checkbox";
    public static final String key_checkboxDisabled = "checkboxDisabled";
    public static final String key_checkboxCheck = "checkboxCheck";
    public static final String key_fastScrollActive = "fastScrollActive";
    public static final String key_fastScrollInactive = "fastScrollInactive";
    public static final String key_fastScrollText = "fastScrollText";

    public static final String key_actionBarDefault = "actionBarDefault";
    public static final String key_actionBarDefaultSelector = "actionBarDefaultSelector";
    public static final String key_actionBarWhiteSelector = "actionBarWhiteSelector";
    public static final String key_actionBarDefaultIcon = "actionBarDefaultIcon";
    public static final String key_actionBarTipBackground = "actionBarTipBackground";
    public static final String key_actionBarActionModeDefault = "actionBarActionModeDefault";
    public static final String key_actionBarActionModeDefaultTop = "actionBarActionModeDefaultTop";
    public static final String key_actionBarActionModeDefaultIcon = "actionBarActionModeDefaultIcon";
    public static final String key_actionBarActionModeDefaultSelector = "actionBarActionModeDefaultSelector";
    public static final String key_actionBarDefaultTitle = "actionBarDefaultTitle";
    public static final String key_actionBarDefaultSubtitle = "actionBarDefaultSubtitle";
    public static final String key_actionBarDefaultSearch = "actionBarDefaultSearch";
    public static final String key_actionBarDefaultSearchPlaceholder = "actionBarDefaultSearchPlaceholder";
    public static final String key_actionBarDefaultSubmenuItem = "actionBarDefaultSubmenuItem";
    public static final String key_actionBarDefaultSubmenuItemIcon = "actionBarDefaultSubmenuItemIcon";
    public static final String key_actionBarDefaultSubmenuBackground = "actionBarDefaultSubmenuBackground";
    public static final String key_actionBarTabActiveText = "actionBarTabActiveText";
    public static final String key_actionBarTabUnactiveText = "actionBarTabUnactiveText";
    public static final String key_actionBarTabLine = "actionBarTabLine";
    public static final String key_actionBarTabSelector = "actionBarTabSelector";
    public static final String key_actionBarDefaultArchived = "actionBarDefaultArchived";
    public static final String key_actionBarDefaultArchivedSelector = "actionBarDefaultArchivedSelector";
    public static final String key_actionBarDefaultArchivedIcon = "actionBarDefaultArchivedIcon";
    public static final String key_actionBarDefaultArchivedTitle = "actionBarDefaultArchivedTitle";
    public static final String key_actionBarDefaultArchivedSearch = "actionBarDefaultArchivedSearch";
    public static final String key_actionBarDefaultArchivedSearchPlaceholder = "actionBarDefaultSearchArchivedPlaceholder";

    public static final String key_actionBarBrowser = "actionBarBrowser";

    private static final Paint sMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static int getColor(String key) {
        return getColor(key, null, false);
    }

    public static int getColor(String key, boolean[] isDefault) {
        return getColor(key, isDefault, false);
    }

    static Random random = new Random();
    private static Method sThemeGetColor3 = null;

    public static int getColor(String key, boolean[] isDefault, boolean ignoreAnimation) {
        if (sThemeGetColor3 == null) {
            Class<?> kTheme = ClassLocator.getThemeClass();
            if (kTheme != null) {
                // find method public static int *(String, boolean[], boolean)
                for (Method method : kTheme.getDeclaredMethods()) {
                    if (method.getReturnType() == Integer.TYPE) {
                        Class<?>[] params = method.getParameterTypes();
                        if (params.length == 3 && params[0] == String.class
                                && params[1] == boolean[].class && params[2] == boolean.class) {
                            sThemeGetColor3 = method;
                            break;
                        }
                    }
                }
                if (sThemeGetColor3 == null) {
                    Utils.loge("Failed to find method Theme.getColor(String, boolean[], boolean), class: " + kTheme);
                }
            }
        }
        if (sThemeGetColor3 != null) {
            try {
                return (Integer) sThemeGetColor3.invoke(null, key, isDefault, ignoreAnimation);
            } catch (ReflectiveOperationException e) {
                Utils.loge("Failed to invoke Theme.getColor(String, boolean[], boolean): " + sThemeGetColor3);
                Utils.loge(e);
            }
        }
        // error occurs, use random color to avoid crash and alert user
        return (key.hashCode() ^ random.nextInt()) | 0x80000000;
    }

    public static Drawable getThemedDrawable(Context context, int resId, String key) {
        return getThemedDrawable(context, resId, getColor(key));
    }

    public static Drawable getThemedDrawable(Context context, int resId, int color) {
        if (context == null) {
            return null;
        }
        Drawable drawable = context.getResources().getDrawable(resId).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return drawable;
    }

    private static Paint sDividerPaint = null;

    public static Paint getDividerPaint() {
        if (sDividerPaint == null) {
            sDividerPaint = new Paint();
        }
        sDividerPaint.setStrokeWidth(1);
        sDividerPaint.setColor(getColor(key_divider));
        return sDividerPaint;
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor) {
        return createSimpleSelectorRoundRectDrawable(rad, defaultColor, pressedColor, pressedColor);
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor, int maskColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        pressedDrawable.getPaint().setColor(maskColor);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{pressedColor}
        );
        return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
    }

    public static Drawable createSelectorDrawableFromDrawables(Drawable normal, Drawable pressed) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressed);
        stateListDrawable.addState(StateSet.WILD_CARD, normal);
        return stateListDrawable;
    }

    public static Drawable getRoundRectSelectorDrawable(int color) {
        Drawable maskDrawable = createRoundRectDrawable(LayoutHelper.dp(3), 0xffffffff);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{(color & 0x00ffffff) | 0x19000000}
        );
        return new RippleDrawable(colorStateList, null, maskDrawable);
    }

    public static Drawable createSelectorWithBackgroundDrawable(int backgroundColor, int color) {
        Drawable maskDrawable = new ColorDrawable(backgroundColor);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
        );
        return new RippleDrawable(colorStateList, new ColorDrawable(backgroundColor), maskDrawable);
    }

    public static Drawable getSelectorDrawable(boolean whiteBackground) {
        return getSelectorDrawable(getColor(key_listSelector), whiteBackground);
    }

    public static Drawable getSelectorDrawable(int color, boolean whiteBackground) {
        if (whiteBackground) {
            return getSelectorDrawable(color, key_windowBackgroundWhite);
        } else {
            return createSelectorDrawable(color, 2);
        }
    }

    public static Drawable getSelectorDrawable(int color, String backgroundColor) {
        if (backgroundColor != null) {
            Drawable maskDrawable = new ColorDrawable(0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            return new RippleDrawable(colorStateList, new ColorDrawable(getColor(backgroundColor)), maskDrawable);
        } else {
            return createSelectorDrawable(color, 2);
        }
    }

    public static Drawable createSelectorDrawable(int color) {
        return createSelectorDrawable(color, 1, -1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType) {
        return createSelectorDrawable(color, maskType, -1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType, int radius) {
        Drawable maskDrawable = null;
        if ((maskType == 1 || maskType == 5) && Build.VERSION.SDK_INT >= 23) {
            maskDrawable = null;
        } else if (maskType == 1 || maskType == 3 || maskType == 4 || maskType == 5 || maskType == 6 || maskType == 7) {
            sMaskPaint.setColor(0xffffffff);
            maskDrawable = new Drawable() {
                RectF rect;

                @Override
                public void draw(Canvas canvas) {
                    android.graphics.Rect bounds = getBounds();
                    if (maskType == 7) {
                        if (rect == null) {
                            rect = new RectF();
                        }
                        rect.set(bounds);
                        canvas.drawRoundRect(rect, LayoutHelper.dp(6), LayoutHelper.dp(6), sMaskPaint);
                    } else {
                        int rad;
                        if (maskType == 1 || maskType == 6) {
                            rad = LayoutHelper.dp(20);
                        } else if (maskType == 3) {
                            rad = (Math.max(bounds.width(), bounds.height()) / 2);
                        } else {
                            rad = (int) Math.ceil(Math.sqrt((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX())
                                    + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())));
                        }
                        canvas.drawCircle(bounds.centerX(), bounds.centerY(), rad, sMaskPaint);
                    }
                }

                @Override
                public void setAlpha(int alpha) {
                    // not implemented
                }

                @Override
                public void setColorFilter(ColorFilter colorFilter) {
                    // not implemented
                }

                @Override
                public int getOpacity() {
                    return PixelFormat.UNKNOWN;
                }
            };
        } else if (maskType == 2) {
            maskDrawable = new ColorDrawable(0xffffffff);
        }
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
        );
        RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, null, maskDrawable);
        if (Build.VERSION.SDK_INT >= 23) {
            if (maskType == 1) {
                rippleDrawable.setRadius(radius <= 0 ? LayoutHelper.dp(20) : radius);
            } else if (maskType == 5) {
                rippleDrawable.setRadius(RippleDrawable.RADIUS_AUTO);
            }
        }
        return rippleDrawable;
    }

    public static Drawable createCircleSelectorDrawable(int color, int leftInset, int rightInset) {
        sMaskPaint.setColor(0xffffffff);
        Drawable maskDrawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                android.graphics.Rect bounds = getBounds();
                final int rad = (Math.max(bounds.width(), bounds.height()) / 2) + leftInset + rightInset;
                canvas.drawCircle(bounds.centerX() - leftInset + rightInset, bounds.centerY(), rad, sMaskPaint);
            }

            @Override
            public void setAlpha(int alpha) {
                // not implemented
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
                // not implemented
            }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }
        };
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
        );
        return new RippleDrawable(colorStateList, null, maskDrawable);
    }

    private static Method StateListDrawable_getStateDrawableMethod;
    private static Field BitmapDrawable_mColorFilter;

    @SuppressLint("PrivateApi")
    private static Drawable getStateDrawable(Drawable drawable, int index) {
        if (Build.VERSION.SDK_INT >= 29 && drawable instanceof StateListDrawable) {
            return ((StateListDrawable) drawable).getStateDrawable(index);
        } else {
            if (StateListDrawable_getStateDrawableMethod == null) {
                try {
                    StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
                } catch (Throwable ignore) {

                }
            }
            if (StateListDrawable_getStateDrawableMethod == null) {
                return null;
            }
            try {
                return (Drawable) StateListDrawable_getStateDrawableMethod.invoke(drawable, index);
            } catch (Exception ignore) {

            }
            return null;
        }
    }

    public static void setSelectorDrawableColor(Drawable drawable, int color, boolean selected) {
        if (drawable instanceof StateListDrawable) {
            try {
                Drawable state;
                if (selected) {
                    state = getStateDrawable(drawable, 0);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                    state = getStateDrawable(drawable, 1);
                } else {
                    state = getStateDrawable(drawable, 2);
                }
                if (state instanceof ShapeDrawable) {
                    ((ShapeDrawable) state).getPaint().setColor(color);
                } else {
                    state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
            } catch (Exception | LinkageError ignore) {
            }
        } else if (drawable instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) drawable;
            if (selected) {
                rippleDrawable.setColor(new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{color}
                ));
            } else {
                if (rippleDrawable.getNumberOfLayers() > 0) {
                    Drawable drawable1 = rippleDrawable.getDrawable(0);
                    if (drawable1 instanceof ShapeDrawable) {
                        ((ShapeDrawable) drawable1).getPaint().setColor(color);
                    } else {
                        drawable1.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            }
        }
    }

    public static class RippleRadMaskDrawable extends Drawable {
        private Path path = new Path();
        private RectF rect = new RectF();
        private float[] radii = new float[8];
        private int topRad;
        private int bottomRad;

        public RippleRadMaskDrawable(int top, int bottom) {
            topRad = top;
            bottomRad = bottom;
        }

        public void setRadius(int top, int bottom) {
            topRad = top;
            bottomRad = bottom;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            radii[0] = radii[1] = radii[2] = radii[3] = LayoutHelper.dp(topRad);
            radii[4] = radii[5] = radii[6] = radii[7] = LayoutHelper.dp(bottomRad);
            rect.set(getBounds());
            path.addRoundRect(rect, radii, Path.Direction.CW);
            canvas.drawPath(path, sMaskPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            // nop
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            // nop
        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }

    public static void setMaskDrawableRad(Drawable rippleDrawable, int top, int bottom) {
        if (rippleDrawable instanceof RippleDrawable) {
            RippleDrawable drawable = (RippleDrawable) rippleDrawable;
            int count = drawable.getNumberOfLayers();
            for (int a = 0; a < count; a++) {
                Drawable layer = drawable.getDrawable(a);
                if (layer instanceof RippleRadMaskDrawable) {
                    drawable.setDrawableByLayerId(android.R.id.mask, new RippleRadMaskDrawable(top, bottom));
                    break;
                }
            }
        }
    }

    public static Drawable createRadSelectorDrawable(int color, int topRad, int bottomRad) {
        sMaskPaint.setColor(0xffffffff);
        Drawable maskDrawable = new RippleRadMaskDrawable(topRad, bottomRad);
        ColorStateList colorStateList = new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{color});
        return new RippleDrawable(colorStateList, null, maskDrawable);
    }

    public static void invalidate() {
        sDividerPaint = null;
    }

    public static boolean isDarkTheme() {
        int backgroundColor = getColor(key_windowBackgroundWhite);
        int textColor = getColor(key_windowBackgroundWhiteBlackText);
        int bg = ((backgroundColor >> 16) & 0xff) * 2 + ((backgroundColor >> 8) & 0xff) * 7 + (backgroundColor & 0xff);
        int fg = ((textColor >> 16) & 0xff) * 2 + ((textColor >> 8) & 0xff) * 7 + (textColor & 0xff);
        return bg < fg;
    }
}
