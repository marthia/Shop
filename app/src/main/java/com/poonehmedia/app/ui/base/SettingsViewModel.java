package com.poonehmedia.app.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.poonehmedia.app.data.model.AppParams;
import com.poonehmedia.app.data.repository.SettingsRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends BaseViewModel {

    private final SettingsRepository repository;
    private final MutableLiveData<Integer> loadingProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> errorObserver = new MutableLiveData<>();
    private final MutableLiveData<AppParams> forceUpdate = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(SettingsRepository repository,
                             SavedStateHandle savedStateHandle,
                             RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
    }

    public LiveData<Integer> getLoadingProgress() {
        return loadingProgress;
    }

    public LiveData<Boolean> getHasError() {
        return errorObserver;
    }

    public void init() {
        repository.init();

        repository.getIsError()
                .doOnSuccess(errorObserver::postValue)
                .subscribe();

        repository.getLevel()
                .doOnNext(loadingProgress::postValue)
                .subscribe();

        repository.getForceUpdate()
                .doOnSuccess(forceUpdate::postValue)
                .subscribe();
    }

    public LiveData<AppParams> getForceUpdate() {
        return forceUpdate;
    }

    public void setHasHandled(boolean b) {
        repository.setHasForceUpdateHandled(b);
    }
}
