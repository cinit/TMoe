package cc.ioctl.tmoe.base;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import cc.ioctl.tmoe.rtti.Bidirectional;
import cc.ioctl.tmoe.rtti.ProxyFragmentRttiHandler;
import cc.ioctl.tmoe.ui.Theme;

public abstract class BaseProxyFragment {

    private Bundle mArgs;
    private boolean mIsPaused = true;
    private final ProxyFragmentRttiHandler mProxyRttiHandler;

    protected BaseProxyFragment() {
        this(null);
    }

    protected BaseProxyFragment(@Nullable Bundle args) {
        mArgs = args;
        mProxyRttiHandler = new ProxyFragmentRttiHandler(this);
    }

    public ProxyFragmentRttiHandler getProxyRttiHandler() {
        return mProxyRttiHandler;
    }

    public Object getProxyObject() {
        return mProxyRttiHandler.getProxyInstance();
    }

    public interface ActionBarWrapper {
        void setBackButtonImage(int resource);

        @Nullable
        ImageView getBackButton();

        void setActionBarMenuOnItemClick(View.OnClickListener listener);

        void setTitle(CharSequence value);

        String getTitle();

        String getSubtitle();

        void setSubtitle(CharSequence value);
    }

    @Nullable
    public ViewGroup getActionBar() {
        return mProxyRttiHandler.getActionBar();
    }

    @Nullable
    public ActionBarWrapper getActionBarWrapper() {
        return mProxyRttiHandler.getActionBarWrapper();
    }

    public abstract View onCreateView(@NonNull Context context);

    public Bundle getArguments() {
        return mArgs;
    }

    public boolean isSwipeBackEnabled(MotionEvent event) {
        return true;
    }

    protected void setFragmentView(View view) {
        mProxyRttiHandler.setFragmentView(view);
    }

    @Nullable
    protected View getFragmentView() {
        return mProxyRttiHandler.getFragmentView();
    }

    public void setInBubbleMode(boolean value) {
        mProxyRttiHandler.setInBubbleMode(value);
    }

    public boolean isInBubbleMode() {
        return mProxyRttiHandler.isInBubbleMode();
    }

    public boolean isInPreviewMode() {
        return mProxyRttiHandler.getInPreviewMode();
    }

    public boolean isInPassivePreviewMode() {
        return mProxyRttiHandler.getInPassivePreviewMode();
    }

    @Bidirectional
    public void setInPreviewMode(boolean value) {
        mProxyRttiHandler.setInPreviewModeSuper(value);
    }

    protected void setInMenuMode(boolean value) {
        mProxyRttiHandler.setInMenuMode(value);
    }

    public void onPreviewOpenAnimationEnd() {
    }

    public boolean hideKeyboardOnShow() {
        return true;
    }

    protected void clearViews() {
        mProxyRttiHandler.clearViews();
    }

    public void onRemoveFromParent() {
    }

    public void setParentFragment(Object fragment) {
        mProxyRttiHandler.setParentFragment(fragment);
    }

    protected void setParentLayout(ViewGroup layout) {
        mProxyRttiHandler.setParentLayout(layout);
    }

    @Bidirectional
    public ViewGroup createActionBar(Context context) {
        return mProxyRttiHandler.createActionBarSuper(context);
    }

    @Nullable
    protected ViewGroup getParentLayout() {
        return mProxyRttiHandler.getParentLayout();
    }

    public boolean presentFragment(@NonNull Object fragment) {
        return presentFragment(fragment, false, false);
    }

    public boolean presentFragment(@NonNull Object fragment, boolean removeLast) {
        return presentFragment(fragment, removeLast, false);
    }

    public boolean presentFragment(@NonNull Object fragment, boolean removeLast, boolean forceWithoutAnimation) {
        Objects.requireNonNull(fragment, "fragment");
        if (!allowPresentFragment()) {
            return false;
        }
        ViewGroup parentLayout = getParentLayout();
        if (parentLayout == null) {
            return false;
        }
        Object targetFragment = fragment instanceof BaseProxyFragment ? ((BaseProxyFragment) fragment).getProxyObject() : fragment;
        return ProxyFragmentRttiHandler.staticPresentFragment(parentLayout, targetFragment,
                removeLast, forceWithoutAnimation, true, false, null);
    }

    protected boolean allowPresentFragment() {
        return true;
    }


    @Bidirectional
    public void finishFragment() {
        mProxyRttiHandler.finishFragmentSuper();
    }

    public void finishFragment(boolean animated) {
        mProxyRttiHandler.finishFragmentSuper(animated);
    }

    @Bidirectional
    public void removeSelfFromStack() {
        mProxyRttiHandler.removeSelfFromStackSuper();
    }

    protected boolean isFinishing() {
        return mProxyRttiHandler.isFinishing();
    }

    public boolean onFragmentCreate() {
        Theme.invalidate();
        return true;
    }

    @Bidirectional
    public void onFragmentDestroy() {
        mProxyRttiHandler.onFragmentDestroySuper();
    }

    public boolean needDelayOpenAnimation() {
        return false;
    }

    public void resumeDelayedFragmentAnimation() {
        mProxyRttiHandler.resumeDelayedFragmentAnimation();
    }

    @Bidirectional
    public void onResume() {
        mProxyRttiHandler.onResumeSuper();
        mIsPaused = false;
    }

    @Bidirectional
    public void onPause() {
        mProxyRttiHandler.onPauseSuper();
        mIsPaused = true;
    }

    protected boolean isPaused() {
        return mIsPaused;
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
    }

    public boolean onBackPressed() {
        return true;
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
    }

    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
    }

    public void saveSelfArgs(Bundle args) {
    }

    public void restoreSelfArgs(Bundle args) {
    }

    public boolean isLastFragment() {
        return mProxyRttiHandler.isLastFragment();
    }

    public Activity getParentActivity() {
        return mProxyRttiHandler.getParentActivity();
    }

    protected void setParentActivityTitle(CharSequence title) {
        Activity activity = getParentActivity();
        if (activity != null) {
            activity.setTitle(title);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        mProxyRttiHandler.startActivityForResult(intent, requestCode);
    }

    public ArrayList<Object> getThemeDescriptions() {
        return new ArrayList<>(0);
    }

    @Bidirectional
    public void dismissCurrentDialog() {
        mProxyRttiHandler.dismissCurrentDialogSuper();
    }

    public boolean dismissDialogOnPause(Dialog dialog) {
        return true;
    }

    public boolean canBeginSlide() {
        return true;
    }

    public void onTransitionAnimationProgress(boolean isOpen, float progress) {
    }

    public void onTransitionAnimationStart(boolean isOpen, boolean backward) {
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
    }

    @Bidirectional
    public void onBecomeFullyVisible() {
        mProxyRttiHandler.onBecomeFullyVisibleSuper();
    }

    public int getPreviewHeight() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public void onBecomeFullyHidden() {
    }

    public AnimatorSet onCustomTransitionAnimation(boolean isOpen, final Runnable callback) {
        return null;
    }

    public void onDialogDismiss(Dialog dialog) {
    }
}
