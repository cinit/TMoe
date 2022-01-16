package cc.ioctl.tmoe.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cc.ioctl.tmoe.util.SavedInstanceStatePatchedClassReferencer;

public class AppCompatTransferActivity extends AppCompatActivity {

    private ClassLoader mXref = null;

    @Override
    public ClassLoader getClassLoader() {
        if (mXref == null) {
            mXref = new SavedInstanceStatePatchedClassReferencer(
                    AppCompatTransferActivity.class.getClassLoader());
        }
        return mXref;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Bundle windowState = savedInstanceState.getBundle("android:viewHierarchyState");
            if (windowState != null) {
                windowState.setClassLoader(AppCompatTransferActivity.class.getClassLoader());
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
