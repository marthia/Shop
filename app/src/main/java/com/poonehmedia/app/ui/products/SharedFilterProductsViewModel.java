package com.poonehmedia.app.ui.products;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Filter;
import com.poonehmedia.app.data.model.FilterCategories;
import com.poonehmedia.app.data.model.FilterData;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;


@HiltViewModel
public class SharedFilterProductsViewModel extends BaseViewModel {

    private final String TAG = getClass().getSimpleName();
    private final MutableLiveData<JsonArray> filterData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> sortData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> currentlySelectedFilters = new MutableLiveData<>();
    private final MutableLiveData<Map<String, FilterData>> filterValues = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<JsonObject> filterParams = new MutableLiveData<>();
    private final MutableLiveData<String> totalCount = new MutableLiveData<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Boolean> totalCountVisibility = new MutableLiveData<>(true);
    private final Context context;
    private final DataController dataController;
    private Runnable applyFilterTask = null;

    @Inject
    public SharedFilterProductsViewModel(@ApplicationContext Context context,
                                         SavedStateHandle savedStateHandle,
                                         DataController dataController,
                                         RoutePersistence routePersistence
    ) {
        super(routePersistence, savedStateHandle);
        this.context = context;
        this.dataController = dataController;
    }

    public LiveData<JsonArray> getFilterData() {
        return filterData;
    }

    public void setFilterData(JsonArray filterData) {
        this.filterData.setValue(filterData);
    }

    public LiveData<JsonArray> getSortData() {
        return sortData;
    }

    public void setSortData(JsonArray array) {
        this.sortData.postValue(array);
    }

    public LiveData<Map<String, FilterData>> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(Filter filter) {
        try {
            Map<String, FilterData> result = filterValues.getValue();

            FilterData filterData = new FilterData();
            filterData.setType(filter.getType());

            String filterId = filter.getMetadata().get("filter_id").getAsString();

            if (filter.isChecked()) {
                filterData.setParentId(filter.getMetadata().get("filter_id").getAsString());
                filterData.setParentPosition(filter.getParentPos());


                switch (filter.getType()) {
                    case "price":
                        filterData.getSelectedValues().put(filter.getOptionKey(), "1");
                        filterData.getSelectedValues().put(filter.getOptionKey(), filter.getValue());
                        break;
                    case "checkBox":

                        if (result.get(filterId) != null && result.get(filterId).getSelectedValues().containsKey(filter.getOptionKey())) {

                            StringBuilder builder = new StringBuilder();
                            String preValue = result.get(filterId).getSelectedValues().get(filter.getOptionKey());
                            builder.append(preValue);
                            builder.append("::");
                            builder.append(filter.getValue());
                            filterData.getSelectedValues().put(filter.getOptionKey(), builder.toString());

                        } else
                            filterData.getSelectedValues().put(filter.getOptionKey(), filter.getValue());
                        break;
                    case "radio":
                    case "sort":
                    case "text":
                        filterData.getSelectedValues().put(filter.getOptionKey(), filter.getValue());
                        break;
                }

                // set titles
                if (filter.getType().equals("checkBox") && result.get(filterId) != null && result.get(filterId).getSelectedValuesLabels().containsKey(filter.getOptionKey())
                ) {
                    StringBuilder builder = new StringBuilder();
                    String preLabel = result.get(filterId).getSelectedValuesLabels().get(filter.getOptionKey());
                    builder.append(preLabel);
                    builder.append(", ");
                    builder.append(filter.getSubtitle());
                    filterData.getSelectedValuesLabels().put(filter.getOptionKey(), builder.toString());
                } else
                    filterData.getSelectedValuesLabels().put(filter.getOptionKey(), filter.getSubtitle());

                result.put(filterId, filterData);

            } else {
                result.get(filterId).getSelectedValues().remove(filter.getOptionKey());
                result.get(filterId).getSelectedValuesLabels().remove(filter.getOptionKey());
            }

            filterValues.postValue(result);
        } catch (Exception e) {
            Log.e(TAG, "Could not complete setFilterValues in (SharedFilterProductsViewModel); " + e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not complete setFilterValues in (SharedFilterProductsViewModel).", e));
        }
    }

    public void applyFilter() {

        if (applyFilterTask != null) handler.removeCallbacks(applyFilterTask);

        applyFilterTask = () -> {
            Map<String, FilterData> filterParams = filterValues.getValue();
            if (filterParams.size() > 0) {
                JsonObject param = new JsonObject();

                ArrayList<FilterData> filterData = dataController.convertMapToList(filterParams);

                for (int i = 0; i < filterData.size(); i++) {
                    FilterData data = filterData.get(i);
                    for (String key : data.getSelectedValues().keySet()) {
                        String s = data.getSelectedValues().get(key);
                        param.addProperty(key, s);
                    }
                }
                this.filterParams.postValue(param);
                Log.i("filter", "FILTER APPLIED");
            }

        };

        handler.postDelayed(applyFilterTask, 500);

    }

    public void clearFilterValues() {
        // make sure our observers get notified by this change
        filterValues.postValue(new HashMap<>());
        filterParams.setValue(new JsonObject());
    }

    public LiveData<JsonObject> getCurrentFilter() {
        return currentlySelectedFilters;
    }

    public void setCurrentFilter(JsonObject obj) {
        currentlySelectedFilters.postValue(obj);
    }

    public void clearFilterFor(String filterId) {
        // make sure our observers get notified by this change
        Map<String, FilterData> value = filterValues.getValue();
        value.remove(filterId);
        filterValues.postValue(value);
    }

    public void clearSortFilter(String filterId) {

        try {
            HashMap<String, String> selectedValues = filterValues.getValue().get(filterId).getSelectedValues();

            String paramKey = "";

            for (String key :
                    selectedValues.keySet()) {
                paramKey = key;
            }

            // make sure our observers get notified by this change
            Map<String, FilterData> filterValuesMap = filterValues.getValue();
            filterValuesMap.remove(filterId);
            filterValues.postValue(filterValuesMap);

            JsonObject value = filterParams.getValue();
            value.remove(paramKey);

            filterParams.postValue(value);
        } catch (Exception e) {
            Log.e("clearSort", "Could not Clear sort");
        }

    }

    public LiveData<JsonObject> getFilterParams() {
        return filterParams;
    }

    public LiveData<String> getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        String first = context.getResources().getString(R.string.total_count_see);
        String last = context.getResources().getString(R.string.total_count_products);
        this.totalCount.postValue(first + " " + totalCount + " " + last);
    }

    public LiveData<Boolean> getTotalCountVisibility() {
        return totalCountVisibility;
    }

    public void updateTotalCountVisibility(Boolean visibility) {
        totalCountVisibility.setValue(visibility);
    }

    public int extractValueAndSetToAdapterList(Map<String, FilterData> stringFilterDataMap, ArrayList<FilterCategories> list) {

        if (stringFilterDataMap.size() > 0) {
            for (String a : stringFilterDataMap.keySet()) {

                FilterData filterData = stringFilterDataMap.get(a);

                if (filterData.getType().equals("sort")) continue;

                else if (filterData.getParentPosition() == -1)
                    continue;

                JsonObject changedItem = list.get(filterData.getParentPosition()).getMetadata();

                if (changedItem.get("filter_id").getAsString().equals(a)) {
                    if (filterData.getSelectedValuesLabels().size() > 0)
                        list.get(filterData.getParentPosition()).setValues(filterData.getSelectedValuesLabels().values().toString());

                    return filterData.getParentPosition();
                }
            }
        }
        return -1;
    }
}
