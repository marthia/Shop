package com.poonehmedia.app.data.framework.service;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.poonehmedia.app.data.model.SearchHistory;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface SearchDao {

    @Query("SELECT * FROM search_history ORDER BY history_frequency DESC LIMIT 10")
    Flowable<List<SearchHistory>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(SearchHistory searchHistories);

    @Delete
    Completable deleteAll(List<SearchHistory> history);

}
