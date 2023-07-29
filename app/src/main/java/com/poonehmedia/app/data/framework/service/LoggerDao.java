package com.poonehmedia.app.data.framework.service;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.poonehmedia.app.data.model.LogItem;

import java.util.List;

@Dao
public interface LoggerDao {

    @Query("SELECT * FROM t_log ORDER BY _id DESC LIMIT 100")
    List<LogItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LogItem logItem);
}
