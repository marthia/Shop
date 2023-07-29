package com.poonehmedia.app.ui.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentPasswordRecoveryBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PasswordRecoveryFragment extends BaseFragment {

    private LoginViewModel viewModel;
    private FragmentPasswordRecoveryBinding binding;
    private String phone;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {

            binding = FragmentPasswordRecoveryBinding.inflate(inflater, container, false);
            binding.setLifecycleOwner(this);

            init();
            subscribeUi();
        }

        return binding.getRoot();
    }

    private void init() {
        binding.phone.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                doSubmit();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });

        binding.btnSubmit.setOnClickListener(v -> {
            doSubmit();
        });
    }

    private void doSubmit() {
        if (!TextUtils.isEmpty(binding.phone.getText())) {
            phone = binding.phone.getText().toString();

            binding.btnSubmit.setEnabled(false);
            binding.swipe.setRefreshing(true);

            viewModel.getResetPasswordValidationCode(phone);
        } else {
            binding.phoneLayout.setError(getResources().getString(R.string.signup_empty_phone_text_error));
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private void subscribeUi() {
        viewModel.getResetPasswordValidateResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                navigator.intrinsicNavigate(
                        requireActivity(),
                        PasswordRecoveryFragmentDirections
                                .actionGoToValidation("کد تایید برای شماره موبایل " + phone + " ارسال گردید"),
                        true
                );
            }
            binding.btnSubmit.setEnabled(true);
            binding.swipe.setRefreshing(false);
        });
    }
}
