package com.poonehmedia.app.ui.editProfile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.FormData;
import com.poonehmedia.app.databinding.FragmentNewEditProfileBinding;
import com.poonehmedia.app.ui.adapter.FormBuilderAdapter;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NewEditProfileFragment extends BaseFragment {

    @Inject
    public FormBuilderAdapter adapter;
    private NewEditProfileViewModel viewModel;
    private FragmentNewEditProfileBinding binding;
    private String submitPath = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NewEditProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewEditProfileBinding.inflate(inflater, container, false);

        viewModel.resolveData();

        init();
        subscribe();


        return binding.getRoot();
    }

    private void subscribe() {

        viewModel.resolveData();

        viewModel.getEditData().observe(getViewLifecycleOwner(), array -> {
            if (JsonHelper.isNotEmptyNorNull(array)) {
                adapter.submitList(array);
            }
        });

        viewModel.getSpinnerUpdate().observe(getViewLifecycleOwner(), jsonObject -> {
            if (jsonObject != null) {
                if (jsonObject.has("tag")) {
                    String tag = jsonObject.get("tag").getAsString();
                    jsonObject.remove("tag");
                    adapter.updateSpinner(tag, jsonObject);
                }
            }
        });

        viewModel.getSubmitAction().observe(getViewLifecycleOwner(), jsonObject -> {
            try {
                if (JsonHelper.isNotEmptyNorNull(jsonObject)) {
                    submitPath = jsonObject.get("link").getAsString();
                    binding.btnSave.setTag(jsonObject.get("text").getAsString());
                }
            } catch (Exception e) {
                Log.e("SignUpInFragment", e.getMessage());
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

        adapter.subscribeCallbacks((item, key) -> {
            viewModel.updateDependantSpinners(((JsonObject) item), key);

        });

        adapter.subscribeOnSubmit(this::submit);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setAdapter(adapter);

        binding.btnSave.setOnClickListener(v -> submit());

    }

    private void submit() {
        AndroidUtils.hideKeyboardFrom(binding.getRoot());
        if (!adapter.hasErrors()) {
            Map<Integer, FormData> values = adapter.getValues();
            Log.i("values", values.toString());
            navigator.navigate(binding.getRoot(),
                    submitPath,
                    viewModel.getPostData(values),
                    this::handleDefaultNavigationState
            );

        } else {
            String message = getString(R.string.make_sure_all_filled);
            UiComponents.showSnack(requireActivity(), message);
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
