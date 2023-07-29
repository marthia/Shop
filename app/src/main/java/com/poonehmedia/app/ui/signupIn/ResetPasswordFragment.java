package com.poonehmedia.app.ui.signupIn;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentResetPasswordNewBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.MyTextWatcher;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordFragment extends BaseFragment {

    private FragmentResetPasswordNewBinding binding;
    private ResetPasswordViewModel viewModel;
    private String link = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentResetPasswordNewBinding.inflate(inflater, container, false);

        init();

        subscribe();

        return binding.getRoot();
    }

    private void subscribe() {

        viewModel.resolveData();

        viewModel.getSubmitAction().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {
                    link = jsonObject.get("link").getAsString();
                }
            } catch (Exception e) {
                Log.e("ResetFragment", e.getMessage());
            }
        });
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


        setUpTextChangedListeners();

        binding.btnResetPassword.setOnClickListener(v -> submit());

        binding.passwordRepeat.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND && binding.btnResetPassword.isEnabled()) {
                submit();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });
    }

    private void submit() {
        binding.btnResetPassword.setEnabled(false);
        navigator.navigate(binding.getRoot(),
                link,
                viewModel.getPostData(binding.password.getText().toString()),
                this::handleDefaultNavigationState
        );
    }

    private void setUpTextChangedListeners() {
        binding.password.addTextChangedListener(new MyTextWatcher(s -> {
            if (!s.equals(binding.passwordRepeat.getText().toString())) {
                binding.passwordLayout.setError(getResources().getString(R.string.passwords_not_match));
                binding.btnResetPassword.setEnabled(false);
            } else {
                binding.passwordLayout.setErrorEnabled(false);
            }
        }));

        binding.passwordRepeat.addTextChangedListener(new MyTextWatcher(s -> {
            if (TextUtils.isEmpty(binding.password.getText()) || !s.equals(binding.password.getText().toString())) {
                binding.passwordRepeatLayout.setError(getResources().getString(R.string.passwords_not_match));
                binding.btnResetPassword.setEnabled(false);
            } else {
                binding.passwordRepeatLayout.setErrorEnabled(false);
                binding.passwordLayout.setErrorEnabled(false);
                binding.btnResetPassword.setEnabled(true);
            }
        }));

        // manually disable the submit button at first run
        binding.password.setText("");
        binding.passwordRepeat.setText("");
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
