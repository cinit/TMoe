package cc.ioctl.tmoe.rtti;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.ViewGroup;

public interface IProxyFragmentObject {

    void clearViews$super();

    void setParentFragment$super(Object fragment);

    void setParentLayout$super(ViewGroup layout);

    ViewGroup createActionBar$super(Context context);

    void finishFragment$super();

    void finishFragment$super(boolean animated);

    void removeSelfFromStack$super();

    boolean onFragmentCreate$super();

    void onFragmentDestroy$super();

    void resumeDelayedFragmentAnimation$super();

    void onResume$super();

    void onPause$super();

    boolean isLastFragment$super();

    Activity getParentActivity$super();

    void startActivityForResult$super(Intent intent, int requestCode);

    void dismissCurrentDialog$super();

    boolean dismissDialogOnPause$super(Dialog dialog);

    void onBecomeFullyVisible$super();

    void onBecomeFullyHidden$super();

    void onConfigurationChanged$super(Configuration newConfig);
}
