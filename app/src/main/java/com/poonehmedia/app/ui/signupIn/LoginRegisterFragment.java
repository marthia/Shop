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
import com.poonehmedia.app.databinding.FragmentLoginRegisterBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginRegisterFragment extends BaseFragment {


    private LoginRegisterViewModel viewModel;
    private FragmentLoginRegisterBinding binding;
    private String submitPath = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginRegisterBinding.inflate(inflater, container, false);

        init();
        subscribe();

        return binding.getRoot();
    }

    private void subscribe() {
        viewModel.resolveData();

        viewModel.getSubmitAction().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {
                    binding.btnSubmit.setText(jsonObject.get("text").getAsString());
                    submitPath = jsonObject.get("link").getAsString();
                }
            } catch (Exception e) {
                Log.e("SignUpInFragment", e.getMessage());
            }
        });

        viewModel.getResetAction().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {
                    binding.forgotPassword.setText(jsonObject.get("text").getAsString());
                    binding.forgotPassword.setTag(jsonObject.get("link").getAsString());
                }
            } catch (Exception e) {
                Log.e("SignUpInFragment", e.getMessage());
            }
        });

        viewModel.getSubtitle().observe(getViewLifecycleOwner(), subtitle -> {
            if (!subtitle.isEmpty()) {
                binding.subtitle.setText(subtitle);
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

        binding.btnSubmit.setOnClickListener(v -> submit());

        binding.forgotPassword.setOnClickListener(v -> {
            navigator.navigate(binding.getRoot(),
                    (String) binding.forgotPassword.getTag(),
                    this::handleDefaultNavigationState
            );
        });

        binding.password.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submit();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });

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

    private void submit() {

        if (TextUtils.isEmpty(binding.password.getText()))
            binding.password.setError(getResources().getString(R.string.signup_empty_pass_text_error));
        else {
            navigator.navigate(binding.getRoot(),
                    submitPath,
                    viewModel.getPostData(binding.password.getText().toString()),
                    this::handleDefaultNavigationState
            );
        }
    }

}
