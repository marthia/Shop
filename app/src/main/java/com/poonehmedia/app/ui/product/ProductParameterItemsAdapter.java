package com.poonehmedia.app.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.CharacteristicsItem;
import com.poonehmedia.app.data.model.ParameterItem;
import com.poonehmedia.app.databinding.ListItemParameterItemsBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductParameterItemsAdapter extends RecyclerView.Adapter<ProductParameterItemsAdapter.ViewHolder> {

    private final ProductViewModel viewModel;
    private final boolean shouldUseCached;
    private final Map<Integer, CharacteristicsItem> selectedVariantController = new HashMap<>();
    private ArrayList<ParameterItem> list = new ArrayList<>();
    private RecyclerView recyclerView;

    public ProductParameterItemsAdapter(ProductViewModel productViewModel, boolean shouldUseCached) {
        this.viewModel = productViewModel;
        this.shouldUseCached = shouldUseCached;
    }

    public ArrayList<ParameterItem> getList() {
        return list;
    }

    public void submitList(ArrayList<ParameterItem> list) {
        this.list = list;
        recyclerView.post(this::notifyDataSetChanged);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemParameterItemsBinding binding = ListItemParameterItemsBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParameterItem object = list.get(position);
        holder.bind(object);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return getList() == null ? 0 : getList().size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemParameterItemsBinding binding;

        public ViewHolder(@NonNull ListItemParameterItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            ItemSpaceDecoration colorsItemSpaceDecoration = new ItemSpaceDecoration(16);
            binding.rvParameterItem.addItemDecoration(colorsItemSpaceDecoration);

            // item parameters adapter
            GenericListAdapterImp<JsonElement> paramsAdapter = new GenericListAdapterImp<>();
            paramsAdapter.setLayoutRes(R.layout.list_item_parameter);

            paramsAdapter.subscribeCallbacks((finalValues, innerPos) -> {

                viewModel.getParameterItemSelectionHolder().put(getAbsoluteAdapterPosition(), innerPos);

                // update selected variant and titles
                selectedVariantController.put(getAbsoluteAdapterPosition(),
                        new CharacteristicsItem(
                                ((JsonObject) finalValues).get("text").getAsString(),
                                ((JsonObject) finalValues).get("value").getAsString()
                        )
                );

                binding.setValue(selectedVariantController.get(getAbsoluteAdapterPosition()).getTitle());

                viewModel.setTitle(selectedVariantController);

                // get new data
                ArrayList<ParameterItem> variantsData = viewModel.getUpdatedVariantsData(
                        list.get(getAbsoluteAdapterPosition())
                                .getElement()
                                .getAsJsonArray()
                                .get(innerPos)
                                .getAsJsonObject()
                );

                // user selected one item from upper row : make the next row visible and
                // register for an assisted selection of the current row because once the first row selection
                // changes the second row loses its selection (although it has stroke as if it has) so we
                // must keep track of it and select one item from the second row manually in the next submitList round.
                if (variantsData.size() > getAbsoluteAdapterPosition() + 1) {
                    variantsData.get(getAbsoluteAdapterPosition() + 1).setVisible(true);
                    variantsData.get(getAbsoluteAdapterPosition() + 1).setAssistedSelection(true);
                }

                // update with new data
                submitList(variantsData);

                // handle selection background color
                binding.rvParameterItem.post(paramsAdapter::notifyDataSetChanged);

                // update viewModels : ui elements for price and title and its dependants
                if (selectedVariantController.size() == list.get(0).getCharacteristicsSize()) {
                    viewModel.setSelectedVariant(selectedVariantController);
                    viewModel.setPriceData();
                }
            });

            // enable selection
            paramsAdapter.hasSelection(true);

            binding.rvParameterItem.setAdapter(paramsAdapter);
        }

        public void bind(ParameterItem item) {

            JsonArray variants = item.getElement().getAsJsonArray();

            if (item.isVisible()) {
                binding.setTitle(item.getTitle());

                GenericListAdapterImp<JsonElement> adapter = (GenericListAdapterImp) binding.rvParameterItem.getAdapter();
                adapter.submitList(variants);

                int selectedPosition = -1;

                // select default item only at first run
                if (adapter.isFresh()) {
                    if (shouldUseCached)
                        selectedPosition = viewModel.getParameterItemSelectionHolder().get(getAbsoluteAdapterPosition());
                    else
                        selectedPosition = getSelectedPosition(variants, item.getDefaultSelectedItem());
                    adapter.setIsFresh(false);
                } else if (item.isAssistedSelection())
                    selectedPosition = 0;

                if (selectedPosition != -1)
                    ((GenericListAdapterImp) binding.rvParameterItem.getAdapter())
                            .submitChange(
                                    variants.get(selectedPosition).getAsJsonObject(),
                                    selectedPosition
                            );

                binding.setSize(variants.size() + "");

            }

            binding.executePendingBindings();
        }

        // server gives us information about default selection by id, but we need to find its position in adapter
        private int getSelectedPosition(JsonArray variants, String defaultSelectedId) {
            if (defaultSelectedId != null && !defaultSelectedId.equals("-1")) {

                for (int position = 0; position < variants.size(); position++) {
                    JsonObject item = variants.get(position).getAsJsonObject();
                    if (item.get("value").getAsString().equals(defaultSelectedId)) {
                        return position;
                    }
                }
            }
            return 0;
        }

    }

}
