package com.poonehmedia.app.util.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.R;

import org.jetbrains.annotations.NotNull;

public class DividerDecor extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private final Rect mBounds = new Rect();
    private final int offsetPx;
    private Drawable mDivider;
    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    private int mOrientation;

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     * {@link LinearLayoutManager} or {@link GridLayoutManager}. Make sure you define divider drawable
     * in app's themes otherwise this class cannot work.
     *
     * @param context     Current context, it will be used to access resources.
     * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     * @param offsetDP    android dp measure to offset from start/end or top/bottom if vertical.
     * @author Marthia
     */
    public DividerDecor(Context context, int offsetDP, int orientation) {
        offsetPx = (int) getPixels(context, offsetDP);
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider);

        setOrientation(orientation);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp, depending on device density
     */
    private float getPixels(Context context, float dp) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation. otherwise the value from xml will be used.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
    }

    /**
     * @return the {@link Drawable} for this divider.
     */
    @Nullable
    public Drawable getDrawable() {
        return mDivider;
    }

    /**
     * Sets the {@link Drawable} for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    public void setDrawable(@NonNull Drawable drawable) {
        mDivider = drawable;
    }

    @Override
    public void onDraw(@NotNull Canvas canvas, RecyclerView parent, @NotNull RecyclerView.State state) {
        if (parent.getLayoutManager() == null || mDivider == null) return;
        if (mOrientation == VERTICAL) drawVertical(canvas, parent);
        else drawHorizontal(canvas, parent);
    }

    /**
     * this should draw dividers horizontally in a vertical recycler view. offsets should be applied to left and right.
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;

        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        int childCount = parent.getChildCount();
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            int leftItems = childCount % ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            if (leftItems == 0) {
                leftItems = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            }
            //Identify last row, and don't draw divider for these items
            childCount -= leftItems;
        }

        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
            final int top = bottom - mDivider.getIntrinsicHeight();
            mDivider.setBounds(left + offsetPx, top, right - offsetPx, bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    /**
     * this should draw dividers vertically in a horizontal recycler view. offset should apply on top and bottom.
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int top;
        final int bottom;

        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
            final int right = mBounds.right + Math.round(child.getTranslationX());
            final int left = right - mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top + offsetPx, right, bottom - offsetPx);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent,
                               @NotNull RecyclerView.State state) {

        if (mDivider == null) {
            outRect.setEmpty();
            return;
        }

        if (mOrientation == VERTICAL) outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        else outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
    }
}
