package com.poonehmedia.app.data.framework;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.poonehmedia.app.data.framework.service.LoggerDao;
import com.poonehmedia.app.data.model.LogItem;

@Database(entities = LogItem.class, version = 1, exportSchema = false)
public abstract class LoggerDatabase extends RoomDatabase {

    public abstract LoggerDao getLoggerDao();
}
