package com.poonehmedia.app.ui.mobileEdit;

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
import com.poonehmedia.app.databinding.FragmentMobileEditBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditMobileFragment extends BaseFragment {

    private FragmentMobileEditBinding binding;
    private EditMobileViewModel viewModel;
    private String phoneNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditMobileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentMobileEditBinding.inflate(inflater, container, false);
            binding.setLifecycleOwner(getViewLifecycleOwner());

            init();
            subscribeUi();
        }
        return binding.getRoot();
    }

    private void init() {

        binding.btnSubmit.setOnClickListener(v -> send());

        binding.phone.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send();
                return true;

            } else return false;
        });
    }

    private void send() {
        AndroidUtils.hideKeyboardFrom(binding.getRoot());
        if (!TextUtils.isEmpty(binding.phone.getText())) {
            binding.btnSubmit.setEnabled(false);
            binding.swipe.setRefreshing(true);
            phoneNumber = binding.phone.getText().toString();

            viewModel.getValidationCode(phoneNumber);
        } else {
            binding.phoneLayout.setError(getResources().getString(R.string.signup_empty_phone_text_error));
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private void subscribeUi() {
        viewModel.getIsValidated().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                navigator.intrinsicNavigate(
                        requireActivity(),
                        EditMobileFragmentDirections
                                .actionGoToValidate("کد تایید برای شماره موبایل " + phoneNumber + " ارسال گردید"),
                        true
                );
            }
            binding.btnSubmit.setEnabled(true);
            binding.swipe.setRefreshing(false);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

}
