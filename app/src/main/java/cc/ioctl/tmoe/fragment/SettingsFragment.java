package cc.ioctl.tmoe.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.base.BaseProxyFragment;
import cc.ioctl.tmoe.hook.func.EnableDebugMode;
import cc.ioctl.tmoe.hook.func.RestrictSaveMitigation;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.ui.Theme;
import cc.ioctl.tmoe.ui.wrapper.TextCheckCell;

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


        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        rootView.addView(scrollView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(ll, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        {
            TextCheckCell cell = new TextCheckCell(context);
            cell.setTextAndCheck(LocaleController.getString("EnableDebugMode", R.string.EnableDebugMode), EnableDebugMode.INSTANCE.isEnabled(), true);
            cell.setOnClickListener(v -> {
                TextCheckCell c = (TextCheckCell) v;
                boolean checked = c.toggle();
                EnableDebugMode.INSTANCE.setEnabledByUser(checked);
                if (checked && !EnableDebugMode.INSTANCE.isInitialized()) {
                    EnableDebugMode.INSTANCE.initialize();
                }
            });
            ll.addView(cell, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        {
            TextCheckCell cell = new TextCheckCell(context);
            cell.setTextAndCheck(LocaleController.getString("RestrictSaveMitigation", R.string.RestrictSaveMitigation),
                    RestrictSaveMitigation.INSTANCE.isEnabledByUser(), true);
            cell.setOnClickListener(v -> {
                TextCheckCell c = (TextCheckCell) v;
                boolean checked = c.toggle();
                RestrictSaveMitigation.INSTANCE.setEnabledByUser(checked);
                if (checked && !RestrictSaveMitigation.INSTANCE.isInitialized()) {
                    RestrictSaveMitigation.INSTANCE.initialize();
                }
            });
            ll.addView(cell, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }

        setFragmentView(rootView);
        return rootView;
    }
}
