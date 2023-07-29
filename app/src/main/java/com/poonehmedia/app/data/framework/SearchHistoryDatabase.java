package com.poonehmedia.app.data.framework;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.poonehmedia.app.data.framework.service.SearchDao;
import com.poonehmedia.app.data.model.SearchHistory;

@Database(entities = {SearchHistory.class}, version = 1, exportSchema = false)
public abstract class SearchHistoryDatabase extends RoomDatabase {

    public abstract SearchDao getSearchDao();

}
