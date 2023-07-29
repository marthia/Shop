package com.poonehmedia.app.ui.commentReport;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentReportInappropriateCommentBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

public class ReportFragment extends BaseFragment {

    private FragmentReportInappropriateCommentBinding binding;
    private ReportViewModel viewModel;
    private JsonObject obj;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentReportInappropriateCommentBinding.inflate(inflater, container, false);

            obj = JsonParser.parseString(ReportFragmentArgs.fromBundle(getArguments()).getAddress()).getAsJsonObject();
            init();
            subscribe();

        }
        return binding.getRoot();
    }

    private void subscribe() {
        viewModel.getIsFinished().observe(getViewLifecycleOwner(), aBoolean -> {
            binding.btnSave.setAlpha(1);
            if (aBoolean)
                Navigation.findNavController(binding.getRoot()).navigateUp();
        });

        viewModel.getSubmitMessage().observe(getViewLifecycleOwner(), s -> {
            UiComponents.showSnack(requireActivity(), s);
        });
    }

    private void init() {

        binding.btnSave.setOnClickListener(v -> {
            AndroidUtils.hideKeyboardFrom(binding.getRoot());
            if (validateInputs()) {
                binding.btnSave.setAlpha(.4f);
                viewModel.submitReport(
                        obj,
                        binding.name.getText().toString(),
                        binding.body.getText().toString()
                );
            }
        });
    }

    private boolean validateInputs() {

        boolean isNameEmpty = TextUtils.isEmpty(binding.name.getText());
        boolean isBodyEmpty = TextUtils.isEmpty(binding.body.getText());

        if (isNameEmpty)
            binding.nameLayout.setError(getString(R.string.input_text_connot_be_empty));

        if (isBodyEmpty)
            binding.bodyLayout.setError(getString(R.string.input_text_connot_be_empty));

        return !isBodyEmpty && !isNameEmpty;
    }
}
