package com.poonehmedia.app.util.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private int position;
    private float translationX;
    private RecyclerView recyclerView;

    public RecyclerItemTouchHelper(int swipeDirs) {
        super(0, swipeDirs);
    }

    @Override
    public void clearView(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof RecyclerItemTouchHelperListener) {
            final View foregroundView = ((RecyclerItemTouchHelperListener) viewHolder).getForeground();
            getDefaultUIUtil().clearView(foregroundView);
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder instanceof RecyclerItemTouchHelperListener) {
            position = viewHolder.getAbsoluteAdapterPosition();
            ((RecyclerItemTouchHelperListener) viewHolder).getForeground().setTranslationX(0);
            super.onSelectedChanged(viewHolder, actionState);
        }
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView,
                            @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        if (viewHolder instanceof RecyclerItemTouchHelperListener &&
                actionState == ItemTouchHelper.ACTION_STATE_SWIPE &&
                position == viewHolder.getAbsoluteAdapterPosition() &&
                translationX != (dX / 1.6f)
        ) {
            this.recyclerView = recyclerView;
            translationX = dX / 1.6f;
            final View foregroundView = ((RecyclerItemTouchHelperListener) viewHolder).getForeground();
            foregroundView.setTranslationX(translationX);
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, translationX, dY,
                    actionState, isCurrentlyActive);
            Log.i("ondrawChild", "translating " + translationX);
        }
    }

    public void reset(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof RecyclerItemTouchHelperListener) {
            final View foregroundView = ((RecyclerItemTouchHelperListener) viewHolder).getForeground();
            ValueAnimator animator = new ValueAnimator();
            animator.setDuration(600);
            float translationX = foregroundView.getTranslationX();

            List<Float> translationXList = new ArrayList<>();
            translationXList.add(translationX);
            for (int i = 1; i < 12; i++) {
                float v = translationX + (50 * i);
                if (v < 0)
                    translationXList.add(translationX + (50 * i));
            }
            translationXList.add(0f);

            float[] translationXs = new float[translationXList.size()];
            for (int i = 0; i < translationXList.size(); i++) {
                translationXs[i] = translationXList.get(i);
            }
            animator.setFloatValues(translationXs);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    clearView(recyclerView, viewHolder);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.addUpdateListener(animation -> {

                Float value = (Float) animation.getAnimatedValue();
                Log.i("animationCompare", "translating to: " + value);
                foregroundView.setTranslationX(value);
            });

            animator.start();
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (viewHolder instanceof RecyclerItemTouchHelperListener) {
            ((RecyclerItemTouchHelperListener) viewHolder)
                    .onSwiped(viewHolder, direction, viewHolder.getAbsoluteAdapterPosition());
            new Handler().postDelayed(() -> reset(viewHolder), 500);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    public interface RecyclerItemTouchHelperListener {

        View getForeground();

        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}