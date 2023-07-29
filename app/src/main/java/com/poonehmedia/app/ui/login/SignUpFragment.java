package com.poonehmedia.app.ui.login;

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
import com.poonehmedia.app.databinding.FragmentSignupBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends BaseFragment {

    private LoginViewModel viewModel;
    private FragmentSignupBinding binding;
    private boolean isConfirmMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);

        bind();

        viewModel.getSignUpResponse().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                handleVisibility(getVisibility(true));
                UiComponents.showSnack(requireActivity(), message);
                changeLayoutToConfirm(message);
                binding.swipe.setRefreshing(false);
            }
        });

        binding.password.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                doSignUp();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });

        viewModel.getConfirmResponse().observe(getViewLifecycleOwner(), message -> {

            binding.swipe.setRefreshing(false);

            if (message != null && !message.isEmpty())
                UiComponents.showSnack(requireActivity(), message);

//            if (message != null) {
//                navigator.clearBackStack(requireActivity());
//                ((MainActivity) requireActivity()).resolveDeepLinks();
//                ((MainActivity) requireActivity()).initializeApp();
//            }

        });

        viewModel.getIsError().observe(getViewLifecycleOwner(), isError -> {
            if (isError) {
                resetLayout();
            }
            binding.swipe.setRefreshing(false);
        });

        return binding.getRoot();
    }

    private void resetLayout() {
        // turn back everything
        handleVisibility(getVisibility(true));

        binding.phone.setText("");
        binding.password.setText("");
        binding.confirmCode.setText("");

        binding.title.setText(R.string.signup_title);
        binding.subtitle.setText(R.string.signup_subtitle);
        binding.confirmCode.setVisibility(View.GONE);
        binding.btnSignupContinue.setText(R.string.signup_continue);
        isConfirmMode = false;
    }

    private void bind() {
        binding.swipe.setDistanceToTriggerSync(1000);
        binding.swipe.setEnabled(false);

        binding.btnSignupContinue.setOnClickListener(v -> doSignUp());

    }

    private void doSignUp() {
        if (!isConfirmMode) {
            if (TextUtils.isEmpty(binding.phone.getText()))
                binding.phoneLayout.setError(getResources().getString(R.string.signup_empty_phone_text_error));
            else if (TextUtils.isEmpty(binding.password.getText()))
                binding.passwordLayout.setError(getResources().getString(R.string.signup_empty_pass_text_error));
            else {// sign up
                viewModel.signUp(binding.phone.getText().toString(), binding.password.getText().toString());
                binding.swipe.setRefreshing(true);

                // hide all
                handleVisibility(getVisibility(false));
            }
        } else {
            if (TextUtils.isEmpty(binding.confirmCode.getText()))
                binding.confirmCode.setError(getResources().getString(R.string.signup_empty_pass_text_error));
            else {
                binding.swipe.setRefreshing(true);
                viewModel.confirmUsername(binding.confirmCode.getText().toString());
            }
        }
    }

    private void handleVisibility(int visibility) {
        binding.passwordLayout.setVisibility(visibility);
        binding.phoneLayout.setVisibility(visibility);
        binding.title.setVisibility(visibility);
        binding.subtitle.setVisibility(visibility);
        binding.btnSignupContinue.setVisibility(visibility);
        binding.confirmCode.setVisibility(visibility);
    }

    private void changeLayoutToConfirm(String subtitle) {
        binding.title.setText(R.string.signup_confirm_code);
        binding.subtitle.setText(subtitle);
        binding.phone.setText("");
        binding.password.setText("");
        binding.confirmCode.setVisibility(View.VISIBLE);
        binding.phoneLayout.setVisibility(View.GONE);
        binding.passwordLayout.setVisibility(View.GONE);
        binding.btnSignupContinue.setText(R.string.signup_confirm_btn);
        isConfirmMode = true;

    }

    private int getVisibility(boolean visibility) {
        if (visibility) return View.VISIBLE;
        else return View.GONE;
    }

}
