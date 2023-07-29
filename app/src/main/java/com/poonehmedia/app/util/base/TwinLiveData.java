package com.poonehmedia.app.util.base;

import androidx.core.util.Pair;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

public class TwinLiveData<K, T> extends MediatorLiveData<Pair<K, T>> {
    public TwinLiveData(MutableLiveData<K> firstLiveData, MutableLiveData<T> secondLiveData) {

        addSource(firstLiveData, first -> setValue(Pair.create(first, secondLiveData.getValue())));

        addSource(secondLiveData, second -> setValue(Pair.create(firstLiveData.getValue(), second)));
    }
}