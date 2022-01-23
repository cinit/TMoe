package cc.ioctl.tmoe.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.Toast;

import androidx.annotation.NonNull;

import cc.ioctl.tmoe.R;
import cc.ioctl.tmoe.base.BaseProxyFragment;
import cc.ioctl.tmoe.ui.LocaleController;
import cc.ioctl.tmoe.ui.Theme;
import cc.ioctl.tmoe.ui.wrapper.TextCheckCell;
import cc.ioctl.tmoe.ui.wrapper.TextSettingsCell;
import cc.ioctl.tmoe.util.Initiator;
import cc.ioctl.tmoe.util.Reflex;
import cc.ioctl.tmoe.util.Utils;

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
            boolean debugMode = false;
            try {
                debugMode = (boolean) Reflex.sget_object(Initiator.load("org.telegram.messenger.BuildVars"), "DEBUG_VERSION");
            } catch (Exception e) {
                Utils.logw(e);
            }
            TextCheckCell cell = new TextCheckCell(context);
            cell.setTextAndCheck(LocaleController.getString("EnableDebugMode", R.string.EnableDebugMode), debugMode, true);
            cell.setOnCellClickListener(c -> {
                boolean checked = c.toggle();
                try {
                    Reflex.sput_object(Initiator.load("org.telegram.messenger.BuildVars"), "DEBUG_VERSION", checked);
                    Reflex.sput_object(Initiator.load("org.telegram.messenger.BuildVars"), "DEBUG_PRIVATE_VERSION", checked);
                } catch (Exception e) {
                    Toast.makeText(c.getView().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            ll.addView(cell.getView(), new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }

        setFragmentView(rootView);
        return rootView;
    }
}
