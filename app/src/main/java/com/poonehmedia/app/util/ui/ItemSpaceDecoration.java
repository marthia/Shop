package com.poonehmedia.app.util.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemSpaceDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private Face excludeFace = Face.NONE;

    public ItemSpaceDecoration(Context context, int dp) {
        this.space = (int) getPixels(context, dp);
    }

    public ItemSpaceDecoration(int pixels) {
        this.space = pixels;
    }

    /**
     * Exclude any of the faces of the rect to spawn any space from that face.
     *
     * @param face {@link Face} of four faces of a rectangle. or NONE to exclude nothing.
     *             the default value is none.
     */
    public void excludeFace(Face face) {
        this.excludeFace = face;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    private float getPixels(Context context, float dp) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (excludeFace != Face.LEFT)
            outRect.left = space;
        if (excludeFace != Face.RIGHT)
            outRect.right = space;
        if (excludeFace != Face.BOTTOM)
            outRect.bottom = space;
        if (excludeFace != Face.TOP)
            outRect.top = space;

    }


    public enum Face {NONE, TOP, BOTTOM, LEFT, RIGHT}
}
