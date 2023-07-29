package com.poonehmedia.app.ui.compare;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.model.CompareItem;
import com.poonehmedia.app.data.model.CompareResult;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.ui.AndroidUtils;

import org.acra.ACRA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class CompareViewModel extends BaseViewModel {

    private final Context context;
    private final BaseApi baseApi;
    private final SavedStateHandle savedStateHandle;
    private final DataController dataController;
    private final MutableLiveData<CompareResult> compareList = new MutableLiveData<>();
    private String path;

    @Inject
    public CompareViewModel(@ApplicationContext Context context,
                            BaseApi baseApi,
                            SavedStateHandle savedStateHandle,
                            DataController dataController,
                            RoutePersistence routePersistence) {

        super(routePersistence, savedStateHandle);
        this.context = context;
        this.baseApi = baseApi;
        this.savedStateHandle = savedStateHandle;
        this.dataController = dataController;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            extractResult(((JsonElement) navigationArgs.getData()));
        }
    }

    public void fetchData() {

        requestData(baseApi.get(path), response -> {

            dataController.onSuccess(response);
            if (response.isSuccessful()) {
                extractResult(response.body());
            }
        }, dataController::onFailure);

    }

    private void extractResult(JsonElement element) {
        try {
//            JsonArray array = dataController.extractDataItemsAsJsonArray(element);
            JsonArray array = JsonParser.parseString(getRawResource(R.raw.compare_data)).getAsJsonArray();

            Map<Integer, Map<Integer, CompareItem>> map = new HashMap<>();
            Map<Integer, Integer> rowHeightController = new HashMap<>();

            for (int index = 0; index < array.size(); index++) {

                Map<Integer, CompareItem> data = new HashMap<>();

                JsonObject obj = array.get(index).getAsJsonObject();
                String title = obj.get("title").getAsString();
                int titleHeight = getHeight(title, 8.0f);

                // add title as a column
                CompareItem item = new CompareItem();
                item.setTitle(title);
                data.put(0, item);

                JsonArray values = obj.get("values").getAsJsonArray();

                for (int i = 0; i < values.size(); i++) {
                    CompareItem item1 = new CompareItem();
                    String value = values.get(i).getAsString();
                    data.put(i + 1, item1); // zero index is already filled by the title column
                    item1.setValue(value);
                    if (index != 0) { // calculate line height skipping images

                        int h = getHeight(value, 26.0f);
                        int maxHeight = Math.max(titleHeight, h);

                        Integer prevMaxHeight = rowHeightController.get(index);
                        if (prevMaxHeight != null)
                            rowHeightController.put(index, Math.max(maxHeight, prevMaxHeight));
                        else rowHeightController.put(index, maxHeight);

                    }

                }
                map.put(index, data);
            }

            CompareResult result = new CompareResult(map, rowHeightController);
            compareList.postValue(result);


        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("extract result", e));
        }
    }

    private String getRawResource(int resource) {
        String res = null;
        InputStream is = context.getResources().openRawResource(resource);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1];
        try {
            while (is.read(b) != -1) {
                baos.write(b);
            }
            res = baos.toString();
            is.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }


    private int getHeight(String value, float maxCharsInOneLine) {

        int length = value.length();
        float lines = length / maxCharsInOneLine;
        Log.i("compare", "lines :" + lines);
        int maxLines = (int) Math.ceil(lines);
        Log.i("compare", "rounded lines :" + maxLines);
        int height = maxLines * (int) AndroidUtils.getPixels(28, context);// min height of a line in dp.
        Log.i("compare", "height : " + height);

        return height;
    }

    public LiveData<CompareResult> getCompareList() {
        return compareList;
    }
}
