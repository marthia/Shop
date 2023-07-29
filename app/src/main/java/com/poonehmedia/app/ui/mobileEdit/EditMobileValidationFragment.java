package com.poonehmedia.app.ui.mobileEdit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentValidationBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.ui.signupIn.ValidationFragmentArgs;
import com.poonehmedia.app.util.ui.MyTextWatcher;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class EditMobileValidationFragment extends BaseFragment {

    private EditMobileViewModel viewModel;
    private FragmentValidationBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditMobileViewModel.class);
    }

    @SuppressLint("FragmentLiveDataObserve")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentValidationBinding.inflate(inflater, container, false);

            String subtitle = ValidationFragmentArgs.fromBundle(getArguments()).getSubtitle();
            if (subtitle != null) binding.subtitle.setText(subtitle);

            binding.btnSubmit.setOnClickListener(v -> {
                if (TextUtils.isEmpty(binding.confirmCode.getText()))
                    binding.confirmCode.setError(getResources().getString(R.string.empty_confirm_code_error));
                else {
                    submit();
                }
            });

            binding.confirmCode.addTextChangedListener(new MyTextWatcher(s -> {
                if (s.length() == 6) submit();
            }));

            viewModel.isConfirmed().observe(getViewLifecycleOwner(), aBoolean -> {

                if (aBoolean) {
                    navigator.intrinsicNavigate(
                            requireActivity(),
                            EditMobileValidationFragmentDirections.actionReturnToProfile(),
                            true
                    );
                }
                binding.btnSubmit.setEnabled(true);
                binding.swipe.setRefreshing(false);
            });
        }

        return binding.getRoot();
    }

    private void submit() {
        binding.swipe.setRefreshing(true);
        binding.btnSubmit.setEnabled(false);
        viewModel.validate(binding.confirmCode.getText().toString());
    }
}
