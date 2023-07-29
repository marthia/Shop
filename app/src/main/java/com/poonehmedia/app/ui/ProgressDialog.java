package com.poonehmedia.app.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;

import com.poonehmedia.app.databinding.ProgressDialogBinding;

public class ProgressDialog extends Dialog {
    /**
     * dismiss the dialog after this duration (in millisecond) is passed, if by any means it didn't get dismissed by the caller.
     */
    private final int TIME_OUT_DISMISS = 60000;

    /**
     * timer that will dismiss this dialog at onFinish()
     */
    private CountDownTimer futureDismiss = null;

    public ProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.poonehmedia.app.databinding.ProgressDialogBinding binding = ProgressDialogBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setCancelable(false);
    }

    @Override
    public void show() {
        super.show();

        futureDismiss = new CountDownTimer(TIME_OUT_DISMISS, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                ProgressDialog.super.dismiss();
            }
        }.start();


    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (futureDismiss != null)
            futureDismiss.cancel();
    }
}
