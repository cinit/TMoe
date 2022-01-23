package cc.ioctl.tmoe.ui.wrapper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

import cc.ioctl.tmoe.ui.Theme;
import cc.ioctl.tmoe.util.Initiator;

public class HeaderCell {
    private static Class<?> sTargetClass = null;
    private static Constructor<?> sTargetConstructor = null;
    private final ViewGroup mTarget;

    public HeaderCell(Context context) {
        this(context, Theme.key_windowBackgroundWhiteBlueHeader, 21, 15, false, null);
    }

    public HeaderCell(Context context, Object resourcesProvider) {
        this(context, Theme.key_windowBackgroundWhiteBlueHeader, 21, 15, false, resourcesProvider);
    }

    public HeaderCell(Context context, int padding) {
        this(context, Theme.key_windowBackgroundWhiteBlueHeader, padding, 15, false, null);
    }

    public HeaderCell(Context context, String textColorKey, int padding, int topMargin, boolean text2) {
        this(context, textColorKey, padding, topMargin, text2, null);
    }

    public HeaderCell(Context context, String textColorKey, int padding, int topMargin, boolean text2, Object resourcesProvider) {
        if (sTargetConstructor == null) {
            Class<?> sTargetClass = Initiator.load("org.telegram.ui.Cells.HeaderCell");
            if (sTargetClass == null) {
                throw new NoClassDefFoundError("org.telegram.ui.Cells.HeaderCell");
            }
            for (Constructor<?> constructor : sTargetClass.getDeclaredConstructors()) {
                if (constructor.getParameterTypes().length == 6) {
                    sTargetConstructor = constructor;
                    break;
                }
            }
            if (sTargetConstructor == null) {
                throw new NoSuchMethodError("org.telegram.ui.Cells.HeaderCell.<init>(Context, String, int, int, boolean, Theme.ResourcesProvider)");
            }
        }
        Objects.requireNonNull(context, "context == null");
        try {
            mTarget = (ViewGroup) sTargetConstructor.newInstance(context, textColorKey, padding, topMargin, text2, resourcesProvider);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public HeaderCell wrap(View cell) {
        if (sTargetClass == null) {
            sTargetClass = Initiator.load("org.telegram.ui.Cells.HeaderCell");
            if (sTargetClass == null) {
                throw new NoClassDefFoundError("org.telegram.ui.Cells.HeaderCell");
            }
        }
        // check if the cell is a HeaderCell
        if (sTargetClass.isInstance(cell)) {
            return new HeaderCell((ViewGroup) cell);
        } else {
            throw new ClassCastException(cell + " is not " + sTargetClass.getName());
        }
    }

    private HeaderCell(ViewGroup target) {
        mTarget = target;
    }

    public ViewGroup getView() {
        return mTarget;
    }

    public void setText(CharSequence text) {
        getTextView().setText(text);
    }

    private static Method sSetText2 = null;

    public void setText2(CharSequence text) {
        if (sSetText2 == null) {
            try {
                sSetText2 = sTargetClass.getDeclaredMethod("setText2", CharSequence.class);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError("org.telegram.ui.Cells.HeaderCell.setText2(CharSequence)");
            }
        }
        try {
            sSetText2.invoke(getTextView(), text);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sGetTextView = null;

    public TextView getTextView() {
        if (sGetTextView == null) {
            try {
                sGetTextView = sTargetClass.getDeclaredMethod("getTextView");
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError("org.telegram.ui.Cells.HeaderCell.getTextView()");
            }
        }
        try {
            return (TextView) sGetTextView.invoke(getView());
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Method sGetTextView2 = null;

    public View getTextView2() {
        if (sGetTextView2 == null) {
            try {
                sGetTextView2 = sTargetClass.getDeclaredMethod("getTextView2");
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError("org.telegram.ui.Cells.HeaderCell.getTextView2()");
            }
        }
        try {
            return (View) sGetTextView2.invoke(getView());
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
