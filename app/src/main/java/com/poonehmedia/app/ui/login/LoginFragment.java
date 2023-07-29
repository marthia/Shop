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
import com.poonehmedia.app.databinding.FragmentLoginBinding;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends BaseFragment {

    @Inject
    public RoutePersistence routePersistence;
    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        binding.btnLogin.setOnClickListener(v -> {
            AndroidUtils.hideKeyboardFrom(binding.getRoot());
            doLogin();
        });

        binding.password.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                doLogin();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            navigator.navigate(
                    requireActivity(),
                    "index.php?option=com_users&view=registration",
                    this::handleDefaultNavigationState);
        });

        binding.forgotPassword.setOnClickListener(v -> {
            navigator.intrinsicNavigate(requireActivity(), LoginFragmentDirections.actionGoToPasswordRecovery(), true);
        });

        viewModel.onLoginResponse().observe(getViewLifecycleOwner(), response -> {
            binding.getRoot().setAlpha(1);
            if (!response.isError()) {
//                UiComponents.showSnack(requireActivity(), getString(R.string.login_success));
//                navigator.clearBackStack(requireActivity());
//                ((MainActivity) requireActivity()).resolveDeepLinks();
//                ((MainActivity) requireActivity()).initializeApp();
            } else {
                UiComponents.showSnack(requireActivity(), getString(R.string.login_error));
            }
        });

        return binding.getRoot();
    }

    private void doLogin() {
        if (TextUtils.isEmpty(binding.username.getText()))
            binding.usernameLayout.setError(getResources().getString(R.string.login_empty_user_text_error));
        else if (TextUtils.isEmpty(binding.password.getText()))
            binding.passwordLayout.setError(getResources().getString(R.string.login_empty_pass_text_error));
        else {
            binding.getRoot().setAlpha(.4f);
            viewModel.login(binding.username.getText().toString(), binding.password.getText().toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
