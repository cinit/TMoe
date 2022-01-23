package cc.ioctl.tmoe.ui.wrapper;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import cc.ioctl.tmoe.util.Initiator;

public class TextCheckCell implements CellWrapper {
    private static final String TARGET_CLASS_NAME = "org.telegram.ui.Cells.TextCheckCell";
    private static Class<?> sTargetClass = null;
    private static Constructor<?> sTargetConstructor = null;
    private final ViewGroup mTarget;

    public TextCheckCell(Context context) {
        this(context, 21);
    }

    public TextCheckCell(Context context, int padding) {
        this(context, padding, false);
    }

    public TextCheckCell(Context context, int padding, boolean dialog) {
        if (sTargetConstructor == null) {
            sTargetClass = Initiator.load(TARGET_CLASS_NAME);
            if (sTargetClass == null) {
                throw new NoClassDefFoundError(TARGET_CLASS_NAME);
            }
            try {
                sTargetConstructor = sTargetClass.getConstructor(Context.class, int.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new UnsupportedOperationException(e);
            }
        }
        Objects.requireNonNull(context, "context == null");
        try {
            mTarget = (ViewGroup) sTargetConstructor.newInstance(context, padding, dialog);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public TextCheckCell wrap(View cell) {
        Objects.requireNonNull(cell, "cell == null");
        if (sTargetClass == null) {
            sTargetClass = Initiator.load(TARGET_CLASS_NAME);
            if (sTargetClass == null) {
                throw new NoClassDefFoundError(TARGET_CLASS_NAME);
            }
        }
        // check if the cell is a TextCheckCell
        if (sTargetClass.isInstance(cell)) {
            return new TextCheckCell((ViewGroup) cell);
        } else {
            throw new ClassCastException(cell + " is not " + sTargetClass.getName());
        }
    }

    private TextCheckCell(ViewGroup target) {
        mTarget = target;
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return mTarget;
    }

    public interface OnCellClickListener {
        void onClick(TextCheckCell cell);
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        if (listener == null) {
            mTarget.setOnClickListener(null);
        } else {
            mTarget.setOnClickListener(v -> listener.onClick(TextCheckCell.this));
        }
    }

    private static Method sSetDivider = null;

    public void setDivider(boolean divider) {
        if (sSetDivider == null) {
            try {
                sSetDivider = mTarget.getClass().getMethod("setDivider", boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setDivider");
            }
        }
        try {
            sSetDivider.invoke(mTarget, divider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetTextAndCheck = null;

    public void setTextAndCheck(String text, boolean checked, boolean divider) {
        if (sSetTextAndCheck == null) {
            try {
                sSetTextAndCheck = mTarget.getClass().getMethod("setTextAndCheck", String.class, boolean.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setTextAndCheck");
            }
        }
        try {
            sSetTextAndCheck.invoke(mTarget, text, checked, divider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetDrawCheckRipple = null;

    public void setDrawCheckRipple(boolean value) {
        if (sSetDrawCheckRipple == null) {
            try {
                sSetDrawCheckRipple = mTarget.getClass().getMethod("setDrawCheckRipple", boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setDrawCheckRipple");
            }
        }
        try {
            sSetDrawCheckRipple.invoke(mTarget, value);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetTextAndValueAndCheck = null;

    public void setTextAndValueAndCheck(String text, String value, boolean checked, boolean multiline, boolean divider) {
        if (sSetTextAndValueAndCheck == null) {
            try {
                sSetTextAndValueAndCheck = mTarget.getClass().getMethod("setTextAndValueAndCheck", String.class, String.class, boolean.class, boolean.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setTextAndValueAndCheck");
            }
        }
        try {
            sSetTextAndValueAndCheck.invoke(mTarget, text, value, checked, multiline, divider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetChecked = null;

    public void setChecked(boolean checked) {
        if (sSetChecked == null) {
            try {
                sSetChecked = mTarget.getClass().getMethod("setChecked", boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setChecked");
            }
        }
        try {
            sSetChecked.invoke(mTarget, checked);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sIsChecked = null;

    public boolean isChecked() {
        if (sIsChecked == null) {
            try {
                sIsChecked = mTarget.getClass().getMethod("isChecked");
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.isChecked");
            }
        }
        try {
            return (boolean) sIsChecked.invoke(mTarget);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public boolean toggle() {
        boolean checked = !isChecked();
        setChecked(checked);
        return checked;
    }

    private static Method sSetEnabled = null;

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        if (sSetEnabled == null) {
            try {
                sSetEnabled = mTarget.getClass().getMethod("setEnabled", boolean.class, ArrayList.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setEnabled");
            }
        }
        try {
            sSetEnabled.invoke(mTarget, value, animators);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public void setEnabled(boolean value) {
        setEnabled(value, null);
    }

    public boolean isEnabled() {
        return mTarget.isEnabled();
    }

    public void setBackgroundColor(int color) {
        mTarget.setBackgroundColor(color);
    }

    private static Method sSetBackgroundColorAnimated = null;

    public void setBackgroundColorAnimated(boolean checked, int color) {
        if (sSetBackgroundColorAnimated == null) {
            try {
                sSetBackgroundColorAnimated = mTarget.getClass().getMethod("setBackgroundColorAnimated", boolean.class, int.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setBackgroundColorAnimated");
            }
        }
        try {
            sSetBackgroundColorAnimated.invoke(mTarget, checked, color);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetAnimationProgress = null;

    private void setAnimationProgress(float value) {
        if (sSetAnimationProgress == null) {
            try {
                sSetAnimationProgress = mTarget.getClass().getMethod("setAnimationProgress", float.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextCheckCell.setAnimationProgress");
            }
        }
        try {
            sSetAnimationProgress.invoke(mTarget, value);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
