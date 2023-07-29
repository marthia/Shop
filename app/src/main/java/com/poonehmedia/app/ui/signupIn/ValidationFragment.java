package com.poonehmedia.app.ui.signupIn;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentPhoneValidationBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.MyTextWatcher;
import com.poonehmedia.app.util.ui.UiComponents;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ValidationFragment extends BaseFragment {

    private final String TAG = getClass().getSimpleName();
    private ValidationViewModel viewModel;
    private FragmentPhoneValidationBinding binding;
    private String link = "";
    private String resendCodeLink = "";
    private int tokenSize = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ValidationViewModel.class);
    }

    @SuppressLint("FragmentLiveDataObserve")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentPhoneValidationBinding.inflate(inflater, container, false);

        init();
        subscribe();

        return binding.getRoot();
    }

    private void init() {
        overrideBackPress(o -> {
            JsonObject backAction = viewModel.getBackAction().getValue();
            if (backAction != null && JsonHelper.has(backAction, "link")) {
                navigator.popUpTo(binding.getRoot(),
                        backAction.get("link").getAsString(),
                        this::handleDefaultNavigationState
                );
            }
        });

        binding.confirmCode.addTextChangedListener(new MyTextWatcher(s -> {
            if (s.length() == tokenSize) submit();
        }));

        binding.btnResetToken.setOnClickListener(v -> {
            viewModel.resendCode(resendCodeLink);
        });
    }


    private void subscribe() {

        viewModel.resolveData();

        viewModel.getSubmitAction().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {
                    link = jsonObject.get("link").getAsString();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });

        viewModel.getTokenInfo().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {

                    binding.subtitle.setText(jsonObject.get("tokenMessage").getAsString());

                    JsonObject resendAction = jsonObject.get("resendAction").getAsJsonObject();
                    resendCodeLink = resendAction.get("link").getAsString();
                    tokenSize = jsonObject.get("size").getAsInt();
                    binding.confirmCode.setMaxLength(tokenSize);
                    binding.btnResetToken.setText(resendAction.get("text").getAsString());
                    binding.btnResetToken.setEnabled(false);
                    startTimer(jsonObject.get("resendTimer").getAsInt());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });

        viewModel.getTokenResent().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean)
                UiComponents.showSnack(requireActivity(), getString(R.string.validation_code_resent));
            else
                UiComponents.showSnack(requireActivity(), getString(R.string.cannot_send_validation_code));
        });
    }

    private void startTimer(int resendTimer) {
        new CountDownTimer(resendTimer * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                binding.resetTimer.setText("ارسال مجدد کد تا " + (millisUntilFinished / 1000) + " ثانیه دیگر");
            }

            public void onFinish() {
                binding.resetTimer.setVisibility(View.GONE);
                binding.btnResetToken.setEnabled(true);
            }
        }.start();
    }

    private void submit() {
        navigator.navigate(binding.getRoot(),
                link,
                viewModel.getPostData(binding.confirmCode.getText().toString()),
                this::handleDefaultNavigationState
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        resetBackAction();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

}
