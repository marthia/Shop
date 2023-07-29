package com.poonehmedia.app.ui.affiliate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.databinding.TabAffiliateStatsBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AffiliateTabStatsFragment extends BaseFragment {

    private TabAffiliateStatsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TabAffiliateStatsBinding.inflate(inflater, container, false);

        parseArgument();

        return binding.getRoot();
    }

    private void parseArgument() {
        String args = (String) getArguments().get("args");
        JsonObject data = JsonParser.parseString(args).getAsJsonObject();

        // TODO SERVER RESPONSE MUST MATCH WITH THE REQUIRED TYPE
        JsonArray stats = convertMapJsonObjectToJsonArray(data);

        AffiliateStatsAdapter adapter = new AffiliateStatsAdapter((view, item) -> {
            navigator.navigate(requireActivity(),
                    ((JsonObject) item).get("history").getAsJsonObject().get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });
        binding.recycler.setAdapter(adapter);
        binding.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));
        adapter.submitList(stats);
    }

    private JsonArray convertMapJsonObjectToJsonArray(JsonObject data) {
        JsonArray result = new JsonArray();
        for (String s : data.keySet()) {
            JsonElement element = data.get(s);
            if (element instanceof JsonObject)
                result.add(element.getAsJsonObject());
        }
        return result;
    }


}
