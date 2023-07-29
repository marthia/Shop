package com.poonehmedia.app.ui.search;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.paging.Pager;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.SearchHistory;
import com.poonehmedia.app.data.repository.LoadingState;
import com.poonehmedia.app.data.repository.SearchRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SearchViewModel extends BaseViewModel {
    // TODO MIGHT NEED TO GET PAGE SIZE FROM SERVER.
    private final int PAGE_SIZE = 15;
    private final SearchRepository repository;
    private final MutableLiveData<List<SearchHistory>> searchHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> errorResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyResponse = new MutableLiveData<>();

    @Inject
    public SearchViewModel(SearchRepository repository,
                           SavedStateHandle savedStateHandle,
                           RoutePersistence routePersistence) {

        super(routePersistence, savedStateHandle);
        this.repository = repository;
    }

    public Pager<Integer, JsonObject> fetchData(String query) {
        return repository.fetchData("search", query, PAGE_SIZE, states -> {
            if (states.equals(LoadingState.States.SUCCESS)) {
                loadingResponse.postValue(false);
                errorResponse.postValue(false);
                emptyResponse.postValue(false);
            } else {
                loadingResponse.postValue(states.equals(LoadingState.States.LOADING));
                errorResponse.postValue(states.equals(LoadingState.States.ERROR));
                emptyResponse.postValue(states.equals(LoadingState.States.EMPTY));
            }
        });
    }

    public void saveRecentQuery(String query, List<SearchHistory> historySuggestionsList) {
        repository.insert(query, historySuggestionsList)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void fetchSearchHistory() {
        requestData(repository.getSearchHistory(),
                searchHistory::postValue,
                throwable -> Log.e("searchHistory", "COULD NOT FETCH SEARCH HISTORY : " + throwable)
        );
    }

    public LiveData<Boolean> getLoadingResponse() {
        return loadingResponse;
    }

    public LiveData<Boolean> getEmptyResponse() {
        return emptyResponse;
    }

    public LiveData<Boolean> getErrorResponse() {
        return errorResponse;
    }

    public LiveData<List<SearchHistory>> getSearchHistory() {
        return searchHistory;
    }

    public void deleteSearchHistory(List<SearchHistory> historySuggestionsList) {
        repository.delete(historySuggestionsList)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void addToDispose(Disposable disposable) {
        getDisposables().add(disposable);
    }
}
