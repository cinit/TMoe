package cc.ioctl.tmoe.ui.wrapper;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.util.Objects;

import cc.ioctl.tmoe.util.Initiator;

public class ShadowSectionCell implements CellWrapper {
    private static final String TARGET_CLASS_NAME = "org.telegram.ui.Cells.ShadowSectionCell";
    private static Class<?> sTargetClass = null;
    private static Constructor<?> sTargetConstructor = null;
    private final ViewGroup mTarget;

    public ShadowSectionCell(Context context) {
        this(context, 12);
    }

    public ShadowSectionCell(Context context, int s) {
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
            mTarget = (ViewGroup) sTargetConstructor.newInstance(context, s);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return mTarget;
    }
}
