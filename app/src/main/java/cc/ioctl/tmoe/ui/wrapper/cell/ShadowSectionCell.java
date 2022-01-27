/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */
package cc.ioctl.tmoe.ui.wrapper.cell;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import cc.ioctl.tmoe.ui.LayoutHelper;
import cc.ioctl.tmoe.ui.Theme;
import cc.ioctl.tmoe.ui.util.CombinedDrawable;
import cc.ioctl.tmoe.util.Utils;

public class ShadowSectionCell extends View {
    private int size;
    private static int sGreyDividerResId = 0;

    public ShadowSectionCell(Context context) {
        this(context, 12);
    }

    public ShadowSectionCell(Context context, int s) {
        super(context);
        if (sGreyDividerResId == 0) {
            sGreyDividerResId = context.getResources().getIdentifier("greydivider", "drawable", context.getPackageName());
            if (sGreyDividerResId == 0) {
                Utils.loge("unable to find R.drawable.greydivider");
            }
        }
        setBackgroundDrawable(Theme.getThemedDrawable(context, sGreyDividerResId, Theme.key_windowBackgroundGrayShadow));
        size = s;
    }

    public ShadowSectionCell(Context context, int s, int backgroundColor) {
        super(context);
        if (sGreyDividerResId == 0) {
            sGreyDividerResId = context.getResources().getIdentifier("greydivider", "drawable", context.getPackageName());
            if (sGreyDividerResId == 0) {
                Utils.loge("unable to find R.drawable.greydivider");
            }
        }
        Drawable shadowDrawable = Theme.getThemedDrawable(context, sGreyDividerResId, Theme.key_windowBackgroundGrayShadow);
        Drawable background = new ColorDrawable(backgroundColor);
        CombinedDrawable combinedDrawable = new CombinedDrawable(background, shadowDrawable, 0, 0);
        combinedDrawable.setFullsize(true);
        setBackgroundDrawable(combinedDrawable);
        size = s;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(LayoutHelper.dp(size), MeasureSpec.EXACTLY));
    }
}
