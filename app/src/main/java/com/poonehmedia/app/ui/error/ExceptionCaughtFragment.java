package com.poonehmedia.app.ui.error;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentExceptionCaughtBinding;
import com.poonehmedia.app.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExceptionCaughtFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.poonehmedia.app.databinding.FragmentExceptionCaughtBinding binding = FragmentExceptionCaughtBinding.inflate(inflater, container, false);

        binding.btnReturn.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.main_nav_fragment).navigateUp());

        View navView = requireActivity().findViewById(R.id.bottom_nav);
        if (navView != null)
            navView.setVisibility(View.GONE);

        return binding.getRoot();
    }
}
