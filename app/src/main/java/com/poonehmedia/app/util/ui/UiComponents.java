package com.poonehmedia.app.util.ui;

import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Strings;
import com.poonehmedia.app.R;

public class UiComponents {

    /**
     * Convenient method to unify snackBar implementation and position on screen. there's a area
     * in main activity devoted to snackBar.
     */
    public static void showSnack(FragmentActivity activity, String text) {

        if (Strings.isNullOrEmpty(text))
            return;

        View root = activity.findViewById(R.id.snack_bar_container);
        Snackbar snackbar = Snackbar.make(root, text, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
