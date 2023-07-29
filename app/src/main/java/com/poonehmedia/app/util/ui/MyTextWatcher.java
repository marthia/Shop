package com.poonehmedia.app.util.ui;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.core.util.Consumer;

public class MyTextWatcher implements TextWatcher {
    private final Consumer<String> action;

    /**
     * A shorter version of a {@link TextWatcher}. <p> The only reason to use this class is better readability of the code,
     * since in almost all cases we don't use {@link TextWatcher#beforeTextChanged(CharSequence, int, int, int)} nor <p>
     * {@link TextWatcher#onTextChanged(CharSequence, int, int, int)} provided by TextWatcher.
     *
     * @param action task to be executed in {@link TextWatcher#afterTextChanged(Editable)}.
     * @author Marthia
     */
    public MyTextWatcher(Consumer<String> action) {
        this.action = action;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s != null)
            action.accept(s.toString());
    }
}
