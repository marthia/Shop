package com.poonehmedia.app.ui.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.poonehmedia.app.R;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.databinding.FragmentAddEditCommentBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommentFragment extends BaseFragment {

    private final String TAG = getClass().getSimpleName();
    @Inject
    public PreferenceManager preferenceManager;
    private FragmentAddEditCommentBinding binding;
    private CommentViewModel viewModel;
    private String payload;
    private boolean isLogin = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditCommentBinding.inflate(inflater, container, false);

        payload = CommentFragmentArgs.fromBundle(getArguments()).getAddress();

        init();
        subscribe();

        return binding.getRoot();
    }

    private void subscribe() {
        viewModel.getIsFinished().observe(getViewLifecycleOwner(), aBoolean -> {
            binding.btnSave.setAlpha(1f);
            if (aBoolean)
                Navigation.findNavController(binding.getRoot()).navigateUp();
        });

        viewModel.getSubmitMessage().observe(getViewLifecycleOwner(), s -> {
            UiComponents.showSnack(requireActivity(), s);
        });
    }

    private void init() {
        bindDefaultParams();

        binding.btnSave.setOnClickListener(v -> {

            AndroidUtils.hideKeyboardFrom(binding.getRoot());
            if (validateInputs()) {
                binding.btnSave.setAlpha(.4f);
                viewModel.submitComment(
                        binding.name.getText().toString(),
                        binding.email.getText().toString(),
                        binding.body.getText().toString()
                );
            }
        });
    }

    private void bindDefaultParams() {
        if (!payload.isEmpty())
            binding.body.setText(payload);

        if (!preferenceManager.getUser().isEmpty() || !preferenceManager.getFullName().isEmpty()) {
            binding.emailSection.setVisibility(View.GONE);
            binding.nameSection.setVisibility(View.GONE);
            isLogin = true;
        }
    }

    private boolean validateInputs() {

        boolean isNameEmpty = TextUtils.isEmpty(binding.name.getText());
        boolean isEmailEmpty = TextUtils.isEmpty(binding.email.getText());
        boolean isBodyEmpty = TextUtils.isEmpty(binding.body.getText());

        if (!isLogin) {
            if (isNameEmpty)
                binding.nameLayout.setError(getString(R.string.input_text_connot_be_empty));

            if (isEmailEmpty)
                binding.emailLayout.setError(getString(R.string.input_text_connot_be_empty));
        }

        if (isBodyEmpty)
            binding.bodyLayout.setError(getString(R.string.input_text_connot_be_empty));

        return !isBodyEmpty && (isLogin || (!isNameEmpty && !isEmailEmpty));

    }

}
