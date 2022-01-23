package cc.ioctl.tmoe.ui.wrapper;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import cc.ioctl.tmoe.util.Initiator;

public class TextSettingsCell implements CellWrapper {
    private static final String TARGET_CLASS_NAME = "org.telegram.ui.Cells.TextSettingsCell";
    private static Class<?> sTargetClass = null;
    private static Constructor<?> sTargetConstructor = null;
    private final ViewGroup mTarget;

    public TextSettingsCell(Context context) {
        this(context, 21);
    }

    public TextSettingsCell(Context context, int padding) {
        if (sTargetConstructor == null) {
            sTargetClass = Initiator.load(TARGET_CLASS_NAME);
            if (sTargetClass == null) {
                throw new NoClassDefFoundError(TARGET_CLASS_NAME);
            }
            try {
                sTargetConstructor = sTargetClass.getConstructor(Context.class, int.class);
            } catch (ReflectiveOperationException e) {
                throw new UnsupportedOperationException(e);
            }
        }
        Objects.requireNonNull(context, "context == null");
        try {
            mTarget = (ViewGroup) sTargetConstructor.newInstance(context, padding);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private TextSettingsCell(ViewGroup target) {
        mTarget = target;
    }

    public TextSettingsCell wrap(View cell) {
        Objects.requireNonNull(cell, "cell == null");
        if (sTargetClass == null) {
            sTargetClass = Initiator.load(TARGET_CLASS_NAME);
            if (sTargetClass == null) {
                throw new NoClassDefFoundError(TARGET_CLASS_NAME);
            }
        }
        // check if the cell is a TextSettingsCell
        if (sTargetClass.isInstance(cell)) {
            return new TextSettingsCell((ViewGroup) cell);
        } else {
            throw new ClassCastException(cell + " is not " + sTargetClass.getName());
        }
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return mTarget;
    }

    private static Method sGetTextView = null;

    public TextView getTextView() {
        if (sGetTextView == null) {
            try {
                sGetTextView = mTarget.getClass().getMethod("getTextView");
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.getTextView()");
            }
        }
        try {
            return (TextView) sGetTextView.invoke(mTarget);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetCanDisable = null;

    public void setCanDisable(boolean value) {
        if (sSetCanDisable == null) {
            try {
                sSetCanDisable = mTarget.getClass().getMethod("setCanDisable", boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.setCanDisable(boolean)");
            }
        }
        try {
            sSetCanDisable.invoke(mTarget, value);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sGetValueTextView = null;

    public TextView getValueTextView() {
        if (sGetValueTextView == null) {
            try {
                sGetValueTextView = mTarget.getClass().getMethod("getValueTextView");
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.getValueTextView()");
            }
        }
        try {
            return (TextView) sGetValueTextView.invoke(mTarget);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public void setTextColor(int color) {
        getTextView().setTextColor(color);
    }

    public void setTextValueColor(int color) {
        getValueTextView().setTextColor(color);
    }

    private static Method sSetText = null;

    public void setText(String text, boolean divider) {
        if (sSetText == null) {
            try {
                sSetText = mTarget.getClass().getMethod("setText", String.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.setText(String, boolean)");
            }
        }
        try {
            sSetText.invoke(mTarget, text, divider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetTextAndValue = null;

    public void setTextAndValue(String text, CharSequence value, boolean divider) {
        if (sSetTextAndValue == null) {
            try {
                sSetTextAndValue = mTarget.getClass().getMethod("setTextAndValue", String.class, CharSequence.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.setTextAndValue(String, CharSequence, boolean)");
            }
        }
        try {
            sSetTextAndValue.invoke(mTarget, text, value, divider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetTextAndIcon = null;

    public void setTextAndIcon(String text, int resId, boolean divider) {
        if (sSetTextAndIcon == null) {
            try {
                sSetTextAndIcon = mTarget.getClass().getMethod("setTextAndIcon", String.class, int.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.setTextAndIcon(String, int, boolean)");
            }
        }
        try {
            sSetTextAndIcon.invoke(mTarget, text, resId, divider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sSetEnabled = null;

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        if (sSetEnabled == null) {
            try {
                sSetEnabled = mTarget.getClass().getMethod("setEnabled", boolean.class, ArrayList.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.setEnabled(boolean, ArrayList<Animator>)");
            }
        }
        try {
            sSetEnabled.invoke(mTarget, value, animators);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public void setEnabled(boolean value) {
        mTarget.setEnabled(value);
    }

    public boolean isEnabled() {
        return mTarget.isEnabled();
    }

    private static Method sSetDrawLoading = null;

    public void setDrawLoading(boolean drawLoading, int size, boolean animated) {
        if (sSetDrawLoading == null) {
            try {
                sSetDrawLoading = mTarget.getClass().getMethod("setDrawLoading", boolean.class, int.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.setDrawLoading(boolean, int, boolean)");
            }
        }
        try {
            sSetDrawLoading.invoke(mTarget, drawLoading, size, animated);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sGetValueBackupImageView = null;

    public View getValueBackupImageView() {
        if (sGetValueBackupImageView == null) {
            try {
                sGetValueBackupImageView = mTarget.getClass().getMethod("getValueBackupImageView");
            } catch (ReflectiveOperationException e) {
                throw new NoSuchMethodError("TextSettingsCell.getValueBackupImageView()");
            }
        }
        try {
            return (View) sGetValueBackupImageView.invoke(mTarget);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
