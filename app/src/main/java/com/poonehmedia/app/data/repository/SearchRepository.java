package com.poonehmedia.app.data.repository;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.SearchHistoryDatabase;
import com.poonehmedia.app.data.framework.service.SearchApi;
import com.poonehmedia.app.data.model.SearchHistory;
import com.poonehmedia.app.util.base.DataController;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import retrofit2.Response;

public class SearchRepository {

    private final SearchApi searchApi;
    private final DataController dataController;
    private final SearchHistoryDatabase searchHistoryDatabase;

    @Inject
    public SearchRepository(SearchApi searchApi,
                            DataController dataController,
                            SearchHistoryDatabase searchHistoryDatabase) {
        this.searchApi = searchApi;
        this.dataController = dataController;
        this.searchHistoryDatabase = searchHistoryDatabase;
    }

    public Flowable<List<SearchHistory>> getSearchHistory() {
        return searchHistoryDatabase.getSearchDao().getAll();
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public Completable insert(String query, List<SearchHistory> currentList) {
        int updateIndex = -1;

        for (int i = 0; i < currentList.size(); i++) {
            SearchHistory item = currentList.get(i);
            if (item.getQuery().equalsIgnoreCase(query)) {
                item.setFrequency(item.getFrequency() + 1);
                updateIndex = i;
                break;
            }
        }

        SearchHistory history;

        if (updateIndex == -1) {
            history = new SearchHistory();
            history.setQuery(query);
            history.setFrequency(0);
        } else {
            history = currentList.get(updateIndex);
        }

        return searchHistoryDatabase.getSearchDao().insert(history);
    }

    public Completable delete(List<SearchHistory> historySuggestionsList) {
        return searchHistoryDatabase.getSearchDao().deleteAll(historySuggestionsList);
    }

    public Pager<Integer, JsonObject> fetchData(String path, String query, int pageSize, LoadingState loadingState) {
        return new Pager<>(new PagingConfig(pageSize, 5, true, pageSize),
                () ->
                        new SearchPagingSource(dataController, searchApi, path, pageSize, query, loadingState)
        );
    }

}
