package com.poonehmedia.app.util.ui;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.core.util.Consumer;

public class DelayedTextWatcher implements TextWatcher {
    private final Consumer<String> action;
    private final int delay;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedTask = null;


    /**
     * delayed version of a text watcher.
     * waits for a specified time (milliseconds) before the actual execution of the action
     *
     * @author Marthia
     */
    public DelayedTextWatcher(Consumer<String> action, int milliSeconds) {
        this.action = action;
        this.delay = milliSeconds;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        // cancel the current because the user has inputted new value before the previous action getting executed
        if (delayedTask != null) {
            handler.removeCallbacks(delayedTask);
        }

        delayedTask = () -> action.accept(s.toString());

        handler.postDelayed(delayedTask, delay);
    }
}
