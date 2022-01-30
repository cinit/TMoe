package cc.ioctl.tmoe.rtti;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cc.ioctl.tmoe.base.BaseProxyFragment;
import cc.ioctl.tmoe.util.HostFirstClassReferencer;
import cc.ioctl.tmoe.util.HostInfo;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;
import dalvik.system.DexClassLoader;

public class ProxyFragmentRttiHandler {

    private final BaseProxyFragment mTargetFragment;
    private IProxyFragmentObject mProxyInstance = null;

    public ProxyFragmentRttiHandler(BaseProxyFragment fragment) {
        mTargetFragment = Objects.requireNonNull(fragment);
    }

    public Object getProxyInstance() {
        if (mProxyInstance == null) {
            mProxyInstance = newProxyFragmentInstance0(this, mTargetFragment.getArguments());
        }
        return mProxyInstance;
    }

    // -----------------------------------------------
    // reflect implementation

    public void setFragmentView(View view) {
        Object proxy = getProxyInstance();
        try {
            Reflex.setInstanceObject(proxy, "fragmentView", view);
        } catch (NoSuchFieldException e) {
            // if this fails, we're probably unable to do anything
            throw new RuntimeException(e);
        }
    }

    public View getFragmentView() {
        Object proxy = getProxyInstance();
        return (View) Reflex.getInstanceObjectOrNull(proxy, "fragmentView");
    }

    public ViewGroup getActionBar() {
        Object obj = getProxyInstance();
        return (ViewGroup) Reflex.getInstanceObjectOrNull(obj, "actionBar");
    }

    public BaseProxyFragment.ActionBarWrapper getActionBarWrapper() {
        ViewGroup actionBar = getActionBar();
        if (actionBar == null) {
            return null;
        }
        return new TMsgActionBarWrapper(actionBar);
    }

    private static class TMsgActionBarWrapper implements BaseProxyFragment.ActionBarWrapper {
        ViewGroup actionBar;

        public TMsgActionBarWrapper(ViewGroup r) {
            actionBar = Objects.requireNonNull(r);
        }

        @Override
        public void setBackButtonImage(int resource) {
            try {
                Reflex.invokeVirtual(actionBar, "setBackButtonImage", resource, int.class, void.class);
            } catch (Exception e) {
                Utils.loge(e);
            }
        }

        @Override
        public void setTitle(CharSequence value) {
            try {
                Reflex.invokeVirtual(actionBar, "setTitle", value, CharSequence.class, void.class);
            } catch (Exception e) {
                Utils.loge(e);
            }
        }

        @Override
        public String getTitle() {
            try {
                return (String) Reflex.invokeVirtual(actionBar, "getTitle", String.class);
            } catch (Exception e) {
                Utils.loge(e);
                return null;
            }
        }

        @Override
        public String getSubtitle() {
            try {
                return (String) Reflex.invokeVirtual(actionBar, "getSubtitle", String.class);
            } catch (Exception e) {
                Utils.loge(e);
                return null;
            }
        }

        @Override
        public void setSubtitle(CharSequence value) {
            try {
                Reflex.invokeVirtual(actionBar, "setSubtitle", value, CharSequence.class, void.class);
            } catch (Exception e) {
                Utils.loge(e);
            }
        }

        @Override
        public ImageView getBackButton() {
            try {
                return (ImageView) Reflex.invokeVirtual(actionBar, "getBackButton", ImageView.class);
            } catch (Exception e) {
                Utils.loge(e);
                return null;
            }
        }

        @Override
        public void setActionBarMenuOnItemClick(View.OnClickListener listener) {
            ImageView backButton = getBackButton();
            if (backButton != null) {
                backButton.setOnClickListener(listener);
            }
        }
    }

    @Nullable
    public ViewGroup getParentLayout() {
        Object obj = getProxyInstance();
        return staticGetParentLayout(obj);
    }

    public static ViewGroup staticGetParentLayout(Object fragment) {
        return (ViewGroup) Reflex.getInstanceObjectOrNull(fragment, "parentLayout");
    }

    public static boolean staticPresentFragment(@NonNull ViewGroup parentLayout, @NonNull Object fragment, boolean removeLast) {
        return staticPresentFragment(parentLayout, fragment, removeLast, false, true, false, null);
    }

    public static boolean staticPresentFragment(@NonNull ViewGroup parentLayout, @NonNull Object fragment,
                                                boolean removeLast, boolean forceWithoutAnimation,
                                                boolean check, boolean preview, @Nullable View menu) {
        Objects.requireNonNull(parentLayout, "parentLayout == null");
        Objects.requireNonNull(fragment, "fragment == null");
        if (sHostActionBarLayoutClass == null || sHostBaseFragmentClass == null) {
            throw new IllegalStateException("attempt to call presentFragment() before sHostBaseFragmentClass is initialized");
        }
        // check cast
        if (fragment instanceof BaseProxyFragment) {
            fragment = ((BaseProxyFragment) fragment).getProxyObject();
        }
        if (!sHostBaseFragmentClass.isInstance(fragment)) {
            throw new ClassCastException("fragment is not instance of " + sHostBaseFragmentClass.getName());
        }
        try {
            return (Boolean) Objects.requireNonNull(sHostActionBarLayoutClass.getMethod("presentFragment", sHostBaseFragmentClass,
                            boolean.class, boolean.class, boolean.class, boolean.class, View.class)
                    .invoke(parentLayout, fragment, removeLast, forceWithoutAnimation, check, preview, menu));
        } catch (ReflectiveOperationException e) {
            Utils.loge(e);
            return false;
        }
    }

    // -----------------------------------------------

    /**
     * [{method name, type}]
     * type:
     * 1. invoke
     * 2. callback
     * 3. invoke and callback(override)
     *
     * @see BaseProxyFragment for more details
     */
    public static final Map<String, Integer> PROXY_METHODS = new HashMap<>();

    static {
        PROXY_METHODS.put("onCreateView", 2);
        PROXY_METHODS.put("isSwipeBackEnabled", 2);
        PROXY_METHODS.put("onPreviewOpenAnimationEnd", 2);
        PROXY_METHODS.put("hideKeyboardOnShow", 2);
        PROXY_METHODS.put("clearViews", 1);
        PROXY_METHODS.put("onRemoveFromParent", 2);
        PROXY_METHODS.put("setParentFragment", 1);
        PROXY_METHODS.put("setParentLayout", 1);
        PROXY_METHODS.put("createActionBar", 3);
        PROXY_METHODS.put("finishFragment", 3);
        PROXY_METHODS.put("removeSelfFromStack", 3);
        PROXY_METHODS.put("onFragmentCreate", 2);
        PROXY_METHODS.put("onFragmentDestroy", 3);
        PROXY_METHODS.put("needDelayOpenAnimation", 2);
        PROXY_METHODS.put("resumeDelayedFragmentAnimation", 2);
        PROXY_METHODS.put("onResume", 3);
        PROXY_METHODS.put("onPause", 3);
        PROXY_METHODS.put("onConfigurationChanged", 2);
        PROXY_METHODS.put("onBackPressed", 2);
        PROXY_METHODS.put("onActivityResultFragment", 2);
        PROXY_METHODS.put("onRequestPermissionsResultFragment", 2);
        PROXY_METHODS.put("saveSelfArgs", 2);
        PROXY_METHODS.put("restoreSelfArgs", 2);
        PROXY_METHODS.put("isLastFragment", 1);
        PROXY_METHODS.put("getParentActivity", 1);
        PROXY_METHODS.put("startActivityForResult", 1);
        PROXY_METHODS.put("dismissCurrentDialog", 3);
        PROXY_METHODS.put("dismissDialogOnPause", 1);
        PROXY_METHODS.put("canBeginSlide", 2);
        PROXY_METHODS.put("onTransitionAnimationProgress", 2);
        PROXY_METHODS.put("onTransitionAnimationStart", 2);
        PROXY_METHODS.put("onTransitionAnimationEnd", 2);
        PROXY_METHODS.put("onBecomeFullyVisible", 3);
        PROXY_METHODS.put("getPreviewHeight", 1);
        PROXY_METHODS.put("onBecomeFullyHidden", 2);
        PROXY_METHODS.put("onCustomTransitionAnimation", 2);
        PROXY_METHODS.put("onDialogDismiss", 2);
    }

    // -------------------------------------------------------
    // proxy methods

    public View createView$dispatcher(Context context) {
        return mTargetFragment.onCreateView(context);
    }

    public boolean isSwipeBackEnabled$dispatcher(MotionEvent event) {
        return mTargetFragment.isSwipeBackEnabled(event);
    }

    public void setInBubbleMode(boolean value) {
        throw new UnsupportedOperationException("dex method not found");
    }

    public boolean isInBubbleMode() {
        throw new UnsupportedOperationException("dex method not found");
    }

    public boolean getInPreviewMode() {
        throw new UnsupportedOperationException("dex method not found");
    }

    public boolean getInPassivePreviewMode() {
        throw new UnsupportedOperationException("dex method not found");
    }

    public void setInPreviewModeSuper(boolean value) {
        throw new UnsupportedOperationException("dex method not found");
    }

    public void setInMenuMode(boolean value) {
        throw new UnsupportedOperationException("dex method not found");
    }

    public void onPreviewOpenAnimationEnd$dispatcher() {
        mTargetFragment.onPreviewOpenAnimationEnd();
    }

    public boolean hideKeyboardOnShow$dispatcher() {
        return mTargetFragment.hideKeyboardOnShow();
    }

    public void clearViews() {
        mProxyInstance.clearViews$super();
    }

    public void onRemoveFromParent$dispatcher() {
        mTargetFragment.onRemoveFromParent();
    }

    public void setParentFragment(Object fragment) {
        mProxyInstance.setParentFragment$super(fragment);
    }

    public void setParentLayout(ViewGroup layout) {
        mProxyInstance.setParentLayout$super(layout);
    }

    public ViewGroup createActionBarSuper(Context context) {
        return mProxyInstance.createActionBar$super(context);
    }

    public ViewGroup createActionBar$dispatcher(Context context) {
        return mProxyInstance.createActionBar$super(context);
    }

    public void finishFragmentSuper() {
        mProxyInstance.finishFragment$super();
    }

    public void finishFragment$dispatcher() {
        mTargetFragment.finishFragment();
    }

    public void finishFragmentSuper(boolean animated) {
        mProxyInstance.finishFragment$super(animated);
    }

    public void finishFragment$dispatcher(boolean animated) {
        mTargetFragment.finishFragment(animated);
    }

    public void removeSelfFromStackSuper() {
        mProxyInstance.removeSelfFromStack$super();
    }

    public void removeSelfFromStack$dispatcher() {
        mTargetFragment.removeSelfFromStack();
    }

    public boolean isFinishing() {
        try {
            return Boolean.TRUE.equals(Reflex.getInstanceObject(mTargetFragment, "finishing", boolean.class));
        } catch (ReflectiveOperationException e) {
            Utils.loge(e);
            return false;
        }
    }

    public void onFragmentCreate() {
        mProxyInstance.onFragmentCreate$super();
    }

    public void onFragmentDestroySuper() {
        mProxyInstance.onFragmentDestroy$super();
    }

    public void onFragmentDestroy$dispatcher() {
        mTargetFragment.onFragmentDestroy();
    }

    public boolean needDelayOpenAnimation$dispatcher() {
        return mTargetFragment.needDelayOpenAnimation();
    }

    public void resumeDelayedFragmentAnimation$dispatcher() {
        mTargetFragment.resumeDelayedFragmentAnimation();
    }

    public void resumeDelayedFragmentAnimation() {
        mProxyInstance.resumeDelayedFragmentAnimation$super();
    }

    public void onResumeSuper() {
        mProxyInstance.onResume$super();
    }

    public void onResume$dispatcher() {
        mTargetFragment.onResume();
    }

    public void onPauseSuper() {
        mProxyInstance.onPause$super();
    }

    public void onPause$dispatcher() {
        mTargetFragment.onPause();
    }

    public void onConfigurationChangedSuper(Configuration newConfig) {
        mProxyInstance.onConfigurationChanged$super(newConfig);
    }

    public void onConfigurationChanged$dispatcher(Configuration newConfig) {
        mTargetFragment.onConfigurationChanged(newConfig);
    }

    public boolean onBackPressed$dispatcher() {
        return mTargetFragment.onBackPressed();
    }

    public void onActivityResultFragment$dispatcher(int requestCode, int resultCode, Intent data) {
        mTargetFragment.onActivityResultFragment(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResultFragment$dispatcher(int requestCode, String[] permissions, int[] grantResults) {
        mTargetFragment.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
    }

    public ArrayList<Object> getThemeDescriptions$dispatcher() {
        return mTargetFragment.getThemeDescriptions();
    }

    public void saveSelfArgs$dispatcher(Bundle args) {
        mTargetFragment.saveSelfArgs(args);
    }

    public void restoreSelfArgs$dispatcher(Bundle args) {
        mTargetFragment.restoreSelfArgs(args);
    }

    public boolean isLastFragment() {
        return mProxyInstance.isLastFragment$super();
    }

    public Activity getParentActivity() {
        return mProxyInstance.getParentActivity$super();
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        mProxyInstance.startActivityForResult$super(intent, requestCode);
    }

    public void dismissCurrentDialog$dispatcher() {
        mTargetFragment.dismissCurrentDialog();
    }

    public void dismissCurrentDialogSuper() {
        mProxyInstance.dismissCurrentDialog$super();
    }

    public void dismissDialogOnPause$dispatcher(Dialog dialog) {
        mTargetFragment.dismissDialogOnPause(dialog);
    }

    public boolean canBeginSlide$dispatcher() {
        return mTargetFragment.canBeginSlide();
    }

    public void onTransitionAnimationProgress$dispatcher(boolean isOpen, float progress) {
        mTargetFragment.onTransitionAnimationProgress(isOpen, progress);
    }

    public void onTransitionAnimationStart$dispatcher(boolean isOpen, boolean backward) {
        mTargetFragment.onTransitionAnimationStart(isOpen, backward);
    }

    public void onTransitionAnimationEnd$dispatcher(boolean isOpen, boolean backward) {
        mTargetFragment.onTransitionAnimationEnd(isOpen, backward);
    }

    public void onBecomeFullyVisible$dispatcher() {
        mTargetFragment.onBecomeFullyVisible();
    }

    public void onBecomeFullyVisibleSuper() {
        mProxyInstance.onBecomeFullyVisible$super();
    }

    public int getPreviewHeight$dispatcher() {
        return mTargetFragment.getPreviewHeight();
    }

    public void onBecomeFullyHidden$dispatcher() {
        mTargetFragment.onBecomeFullyHidden();
    }

    public void onBecomeFullyHiddenSuper() {
        mProxyInstance.onBecomeFullyHidden$super();
    }

    public AnimatorSet onCustomTransitionAnimation$dispatcher(boolean isOpen, Runnable callback) {
        return mTargetFragment.onCustomTransitionAnimation(isOpen, callback);
    }

    public void onDialogDismiss$dispatcher(Dialog dialog) {
        mTargetFragment.onDialogDismiss(dialog);
    }

    // -------------------------------------------------------
    // static methods

    private static Class<?> sRttiProxyFragmentClass;
    private static Class<?> sHostBaseFragmentClass;
    private static Class<?> sHostActionBarLayoutClass;
    private static Class<?> sHostActionBarClass;

    public static void initProxyFragmentClass(Class<?> hostBaseFragmentClass)
            throws ReflectiveOperationException {
        // BaseFragment must has field ActionBarLayout parentLayout and ActionBar actionBar
        // if these field are not existent, it means that the BaseFragment is not valid or not supported
        // find the classes: org.telegram.ui.ActionBar.ActionBarLayout, org.telegram.ui.ActionBar.ActionBar
        sHostActionBarClass = hostBaseFragmentClass.getDeclaredField("actionBar").getType();
        sHostActionBarLayoutClass = hostBaseFragmentClass.getDeclaredField("parentLayout").getType();
        sHostBaseFragmentClass = hostBaseFragmentClass;
        try {
            File dexFile = ProxyFragmentImplDexCreator.createProxyFragmentImplDex(hostBaseFragmentClass,
                    sHostActionBarClass, sHostActionBarLayoutClass);
            if (!dexFile.exists() || dexFile.length() == 0) {
                throw new RuntimeException("create proxy fragment impl dex failed for unknown reason");
            }
            // load the dex file
            Context ctx = HostInfo.getApplication();
            ClassLoader parentClassLoader = new HostFirstClassReferencer();
            DexClassLoader dexClassLoader = new DexClassLoader(dexFile.getAbsolutePath(),
                    ctx.getDir("dex_opt", 0).getAbsolutePath(), null, parentClassLoader);
            // load class: cc.ioctl.tmoe.dynamic.ProxyFragmentImpl, exception: java.lang.ClassNotFoundException
            sRttiProxyFragmentClass = dexClassLoader.loadClass("cc.ioctl.tmoe.dynamic.ProxyFragmentImpl");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static boolean checkProxyFragmentRttiStatus() {
        return sRttiProxyFragmentClass != null && sHostBaseFragmentClass != null;
    }

    /**
     * Create a new ProxyFragmentImpl instance with the given handle.
     * You should call {@link #checkProxyFragmentRttiStatus()} first to check if the ProxyFragmentImpl class is loaded.
     * If the class is not loaded, please init the class with {@link #initProxyFragmentClass(Class)}
     *
     * @param handler the handle of the ProxyFragmentImpl instance
     * @param args    the optional arguments, may be null
     * @return a new ProxyFragmentImpl instance
     * @throws IllegalStateException if the ProxyFragmentImpl class is not initialized
     */
    static IProxyFragmentObject newProxyFragmentInstance0(ProxyFragmentRttiHandler handler, Bundle args) {
        if (!checkProxyFragmentRttiStatus()) {
            throw new IllegalStateException("proxy fragment rtti is not initialized");
        }
        if (handler == null) {
            throw new NullPointerException("handler is null");
        }
        try {
            Constructor<?> constructor = sRttiProxyFragmentClass.getConstructor(ProxyFragmentRttiHandler.class, Bundle.class);
            return (IProxyFragmentObject) constructor.newInstance(handler, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
