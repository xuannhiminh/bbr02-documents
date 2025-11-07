package com.ezteam.ezpdflib.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ezteam.ezpdflib.model.FileData;

@Dao
public interface FileInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateFileStatus(FileData fileData);

    @Query("SELECT * FROM file_data WHERE path =:path")
    FileData getFileStatus(String path);

}
