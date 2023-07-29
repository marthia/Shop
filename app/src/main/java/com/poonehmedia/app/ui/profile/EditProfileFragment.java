package com.poonehmedia.app.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentProfileEditWithShimmerBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileFragment extends BaseFragment {

    private ProfileViewModel viewModel;
    private FragmentProfileEditWithShimmerBinding binding;
    private boolean hasError = false;
    private String saveUrl;
    private String id;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileEditWithShimmerBinding.inflate(inflater, container, false);

        handleShimmer(binding.shimmer, binding.main.rootContainer, true);

        viewModel.resolveData(true);

        viewModel.getEditData().observe(getViewLifecycleOwner(), obj -> {
            if (obj != null) {
                binding.main.name.setText(obj.get("name").getAsString());
                binding.main.username.setText(obj.get("username").getAsString());
                binding.main.email.setText(obj.get("email").getAsString());
                saveUrl = obj.get("saveurl").getAsString();
                id = obj.get("id").getAsString();
                handleShimmer(binding.shimmer, binding.main.rootContainer, false);

                bindMobileEditButton(obj);
            }
        });

        viewModel.getEditResponse().observe(getViewLifecycleOwner(), s -> {
            if (s != null && !s.isEmpty()) {
                UiComponents.showSnack(requireActivity(), s);
            }
        });

        viewModel.isEditSuccess().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                Navigation.findNavController(requireActivity(), R.id.main_nav_fragment).popBackStack();
            }
        });

        binding.main.btnSave.setOnClickListener(v -> {
            doEdit();
        });

        binding.main.password.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                doEdit();
                return true;
            } else return false;
        });

        return binding.getRoot();
    }

    private void bindMobileEditButton(JsonObject obj) {
        if (JsonHelper.has(obj, "editmobile")) {
            String url = obj.get("editmobile").getAsJsonObject().get("link").getAsString();
            binding.main.btnMobileEdit.setOnClickListener(v ->
                    navigator.navigate(requireActivity(), url, this::handleDefaultNavigationState)
            );
        } else {
            binding.main.btnMobileEdit.setVisibility(View.GONE);
        }
    }

    private void doEdit() {
        AndroidUtils.hideKeyboardFrom(binding.getRoot());
        checkIfAnyFieldIsEmpty();
        if (!hasError) {
            saveChanges();
        } else
            UiComponents.showSnack(requireActivity(), getString(R.string.make_sure_all_filled));
    }

    private void saveChanges() {
        String name = binding.main.name.getText().toString();
        String username = binding.main.username.getText().toString();
        String email = binding.main.email.getText().toString();
        String password = binding.main.password.getText().toString();

        viewModel.editProfile(saveUrl, id, name, username, email, password);
    }

    private void checkIfAnyFieldIsEmpty() {
        boolean isNameEmpty = TextUtils.isEmpty(binding.main.name.getText());

        boolean isUsernameEmpty = TextUtils.isEmpty(binding.main.username.getText());

        if (isNameEmpty) {
            binding.main.nameLayout.setError(getResources().getString(R.string.empty_edit_text));
            hasError = true;
        } else if (isUsernameEmpty) {
            binding.main.usernameLayout.setError(getResources().getString(R.string.empty_edit_text));
            hasError = true;
        }

    }

}
