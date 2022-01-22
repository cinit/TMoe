package cc.ioctl.tmoe.fragment;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.base.BaseProxyFragment;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.ui.Theme;

public class SettingsFragment extends BaseProxyFragment {

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public View onCreateView(@NonNull Context context) {
        ActionBarWrapper actionBar = getActionBarWrapper();
        if (actionBar != null) {
            actionBar.setTitle(LocaleController.getString("TMoeSettings", R.string.TMoeSettings));
            int backImgId = context.getResources().getIdentifier("ic_ab_back", "drawable", context.getPackageName());
            if (backImgId != 0) {
                actionBar.setBackButtonImage(backImgId);
                actionBar.setActionBarMenuOnItemClick(v -> finishFragment());
            }
        }
        FrameLayout rootView = new FrameLayout(context);
        rootView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));


        setFragmentView(rootView);
        return rootView;
    }
}
