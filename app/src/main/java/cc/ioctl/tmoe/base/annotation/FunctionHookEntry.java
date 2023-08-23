package cc.ioctl.tmoe.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denoting a hook(has nothing to do with the UI).
 * <p>
 * It should be a Kotlin object, or a Java class with a public static final INSTANCE field.
 * <p>
 * Target should be an instance of {@link cc.ioctl.tmoe.hook.base.BaseDynamicHook}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface FunctionHookEntry {
}
