package com.poonehmedia.app.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentResetPasswordBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.MyTextWatcher;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetFragment extends BaseFragment {

    private static final int DEFAULT_PASSWORD_LENGTH = 6;
    private FragmentResetPasswordBinding binding;
    private LoginViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);

        setUpTextChangedListeners();

        binding.btnResetPassword.setOnClickListener(v -> submit());

        binding.passwordRepeat.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND && binding.btnResetPassword.isEnabled()) {
                submit();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });

        viewModel.isApproved().observe(getViewLifecycleOwner(), aBoolean -> {

                    if (aBoolean) {
                        navigator.intrinsicNavigate(
                                requireActivity(),
                                ResetFragmentDirections.actionReturnToLogin(),
                                false
                        );
                    }
                    binding.btnResetPassword.setEnabled(true);
                    binding.swipe.setRefreshing(false);
                }
        );

        return binding.getRoot();
    }

    private void submit() {
        binding.btnResetPassword.setEnabled(false);
        binding.swipe.setRefreshing(true);
        viewModel.finalizeChangePassword(binding.password.getText().toString());
    }

    private void setUpTextChangedListeners() {
        binding.password.addTextChangedListener(new MyTextWatcher(s -> {
            if (s.length() < DEFAULT_PASSWORD_LENGTH) {
                binding.passwordLayout.setError(getResources().getString(R.string.password_limit_not_satisfied));
                binding.btnResetPassword.setEnabled(false);
            } else {
                binding.passwordLayout.setErrorEnabled(false);
                binding.btnResetPassword.setEnabled(true);
            }
        }));

        binding.passwordRepeat.addTextChangedListener(new MyTextWatcher(s -> {
            if (s.length() < DEFAULT_PASSWORD_LENGTH && !s.equals(binding.password.getText().toString())) {
                binding.passwordRepeatLayout.setError(getResources().getString(R.string.passwords_not_match));
                binding.btnResetPassword.setEnabled(false);
            } else {
                binding.passwordRepeatLayout.setErrorEnabled(false);
                binding.btnResetPassword.setEnabled(true);
            }
        }));

        // mimic action call to disable the button at first run
        binding.password.setText("");
        binding.passwordRepeat.setText("");
    }

}
