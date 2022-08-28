package cc.ioctl.tmoe.util;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

import cc.ioctl.tmoe.hook.base.CommonDynamicHook;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HookUtils {

    private HookUtils() {
        throw new AssertionError("no instance for you!");
    }

    public interface BeforeHookedMethod {
        void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public interface AfterHookedMethod {
        void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public interface BeforeAndAfterHookedMethod {
        void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;

        void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public static void hookAfterIfEnabled(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                          int priority, final @NonNull AfterHookedMethod afterHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, new XC_MethodHook(priority) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (this0.isEnabled()) {
                        afterHookedMethod.afterHookedMethod(param);
                    }
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        });
    }

    public static XC_MethodHook beforeIfEnabled(final @NonNull CommonDynamicHook this0, int priority,
                                                final @NonNull BeforeHookedMethod beforeHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        return new XC_MethodHook(priority) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (this0.isEnabled()) {
                        beforeHookedMethod.beforeHookedMethod(param);
                    }
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        };
    }

    public static XC_MethodHook afterIfEnabled(final @NonNull CommonDynamicHook this0, int priority,
                                               final @NonNull AfterHookedMethod afterHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        return new XC_MethodHook(priority) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (this0.isEnabled()) {
                        afterHookedMethod.afterHookedMethod(param);
                    }
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        };
    }

    public static XC_MethodHook afterAlways(final @NonNull CommonDynamicHook this0, int priority,
                                            final @NonNull AfterHookedMethod afterHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        return new XC_MethodHook(priority) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    afterHookedMethod.afterHookedMethod(param);
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        };
    }

    public static XC_MethodHook beforeIfEnabled(final @NonNull CommonDynamicHook this0,
                                                final @NonNull BeforeHookedMethod beforeHookedMethod) {
        return beforeIfEnabled(this0, 50, beforeHookedMethod);
    }

    public static XC_MethodHook afterIfEnabled(final @NonNull CommonDynamicHook this0,
                                               final @NonNull AfterHookedMethod afterHookedMethod) {
        return afterIfEnabled(this0, 50, afterHookedMethod);
    }

    public static void hookBeforeIfEnabled(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                           int priority, final @NonNull BeforeHookedMethod beforeHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, new XC_MethodHook(priority) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (this0.isEnabled()) {
                        beforeHookedMethod.beforeHookedMethod(param);
                    }
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        });
    }

    public static void hookAfterIfEnabled(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                          final @NonNull AfterHookedMethod afterHookedMethod) {
        hookAfterIfEnabled(this0, method, 50, afterHookedMethod);
    }

    public static void hookBeforeIfEnabled(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                           final @NonNull BeforeHookedMethod beforeHookedMethod) {
        hookBeforeIfEnabled(this0, method, 50, beforeHookedMethod);
    }

    public static void hookAfterAlways(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                       int priority, final @NonNull AfterHookedMethod afterHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, new XC_MethodHook(priority) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    afterHookedMethod.afterHookedMethod(param);
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        });
    }

    public static void hookAfterAlways(final @NonNull CommonDynamicHook this0, final @NonNull Constructor<?> ctor,
                                       int priority, final @NonNull AfterHookedMethod afterHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        Objects.requireNonNull(ctor, "ctor == null");
        XposedBridge.hookMethod(ctor, new XC_MethodHook(priority) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    afterHookedMethod.afterHookedMethod(param);
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        });
    }

    public static void hookBeforeAlways(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                        int priority, final @NonNull BeforeHookedMethod beforeHookedMethod) {
        Objects.requireNonNull(this0, "this0 == null");
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, new XC_MethodHook(priority) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    beforeHookedMethod.beforeHookedMethod(param);
                } catch (Throwable e) {
                    this0.logError(e);
                    throw e;
                }
            }
        });
    }

    public static void hookAfterAlways(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                       final @NonNull AfterHookedMethod afterHookedMethod) {
        hookAfterAlways(this0, method, 50, afterHookedMethod);
    }

    public static void hookAfterAlways(final @NonNull CommonDynamicHook this0, final @NonNull Constructor<?> ctor,
                                       final @NonNull AfterHookedMethod afterHookedMethod) {
        hookAfterAlways(this0, ctor, 50, afterHookedMethod);
    }

    public static void hookBeforeAlways(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                        final @NonNull BeforeHookedMethod beforeHookedMethod) {
        hookBeforeAlways(this0, method, 50, beforeHookedMethod);
    }

    public static void hookBeforeAndAfterIfEnabled(final @NonNull CommonDynamicHook this0, final @NonNull Method method,
                                                   int priority, final @NonNull BeforeAndAfterHookedMethod hook) {
        Objects.requireNonNull(this0, "this0 == null");
        Objects.requireNonNull(method, "method == null");
        XposedBridge.hookMethod(method, new XC_MethodHook(priority) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (this0.isEnabled()) {
                    try {
                        hook.beforeHookedMethod(param);
                    } catch (Throwable e) {
                        this0.logError(e);
                        throw e;
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (this0.isEnabled()) {
                    try {
                        hook.afterHookedMethod(param);
                    } catch (Throwable e) {
                        this0.logError(e);
                        throw e;
                    }
                }
            }
        });
    }
}
