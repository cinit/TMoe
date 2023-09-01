package cc.ioctl.tmoe.util;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CloneUtils {

    private CloneUtils() {
        throw new UnsupportedOperationException();
    }

    @SuppressLint("DiscouragedPrivateApi")
    @NonNull
    public static Object shadowClone(@NonNull Object obj) {
        Class<?> klass = obj.getClass();
        Class<?> kUnsafe;
        Object theUnsafe;
        Method allocateInstance;
        try {
            kUnsafe = Class.forName("sun.misc.Unsafe");
            theUnsafe = kUnsafe.getDeclaredField("theUnsafe").get(null);
            allocateInstance = kUnsafe.getDeclaredMethod("allocateInstance", Class.class);
        } catch (ReflectiveOperationException e) {
            throw IoUtils.unsafeThrow(e);
        }
        Object instance;
        try {
            instance = allocateInstance.invoke(theUnsafe, klass);
            assert instance != null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw IoUtils.unsafeThrowForIteCause(e);
        }
        Class<?> current = klass;
        while ((current = current.getSuperclass()) != null) {
            if (current == Object.class) {
                break;
            }
            for (Field f : current.getDeclaredFields()) {
                // skip static and transient fields
                if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    f.set(instance, f.get(obj));
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return instance;
    }
}
