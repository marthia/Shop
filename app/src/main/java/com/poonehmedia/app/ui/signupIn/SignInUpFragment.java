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

import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentSignUpInBinding;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignInUpFragment extends BaseFragment {

    @Inject
    public RoutePersistence routePersistence;
    private FragmentSignUpInBinding binding;
    private SignUpInViewModel viewModel;
    private String submitPath = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SignUpInViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpInBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        init();

        subscribe();

        return binding.getRoot();
    }

    private void subscribe() {

        viewModel.resolveData();

        viewModel.getSubmitAction().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {
                    binding.btnLogin.setText(jsonObject.get("text").getAsString());
                    submitPath = jsonObject.get("link").getAsString();
                }
            } catch (Exception e) {
                Log.e("SignUpInFragment", e.getMessage());
            }
        });

//        viewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), isLoggedIn -> {
//            if (isLoggedIn) {
//                UiComponents.showSnack(requireActivity(), getString(R.string.login_success));
//                navigator.clearBackStack(requireActivity());
//                ((MainActivity) requireActivity()).resolveDeepLinks();
//                ((MainActivity) requireActivity()).initializeApp();
//                ((MainActivity) requireActivity()).resetOverrideBackPress();
//            }
//        });
    }

    private void init() {

        binding.btnLogin.setOnClickListener(v -> {
            AndroidUtils.hideKeyboardFrom(binding.getRoot());
            submit();
        });

        binding.username.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submit();
                AndroidUtils.hideKeyboardFrom(binding.getRoot());
                return true;

            } else return false;
        });
    }

    private void submit() {
        if (TextUtils.isEmpty(binding.username.getText()))
            binding.usernameLayout.setError(getResources().getString(R.string.login_empty_user_text_error));
        else {
            navigator.navigate(binding.getRoot(),
                    submitPath,
                    viewModel.getPostData(binding.username.getText().toString()),
                    this::handleDefaultNavigationState
            );
        }
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
