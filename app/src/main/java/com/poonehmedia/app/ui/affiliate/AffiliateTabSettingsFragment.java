package com.poonehmedia.app.ui.affiliate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.TabAffiliateSettingsBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.MyTextWatcher;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AffiliateTabSettingsFragment extends BaseFragment {

    private TabAffiliateSettingsBinding binding;
    private AffiliateViewModel viewModel;
    private JsonObject saveAction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavBackStackEntry backStackEntry = getGraphScope(R.id.affiliate_graph);
        viewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(AffiliateViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TabAffiliateSettingsBinding.inflate(inflater, container, false);

        parseArgument();

        return binding.getRoot();
    }

    private void parseArgument() {
        String args = (String) getArguments().get("args");
        JsonObject data = JsonParser.parseString(args).getAsJsonObject();
        binding.setItem(data);

        saveAction = data.get("saveAction").getAsJsonObject();
        JsonObject lastLevel = saveAction
                .get("params").getAsJsonObject()
                .get("data").getAsJsonObject()
                .get("user").getAsJsonObject();

        binding.enableMarketing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                lastLevel.remove("user_partner_activated");
                lastLevel.addProperty("user_partner_activated", "1");
            } else {
                lastLevel.remove("user_partner_activated");
                lastLevel.addProperty("user_partner_activated", "0");
            }
        });

        binding.editText.addTextChangedListener(new MyTextWatcher(text -> {
            lastLevel.remove("user_partner_email");
            lastLevel.addProperty("user_partner_email", text);
        }));

        binding.btnSave.setOnClickListener(v -> {
            viewModel.saveSettings(
                    saveAction.get("link").getAsString(),
                    saveAction.get("params").getAsJsonObject()
            );
        });
    }
}
