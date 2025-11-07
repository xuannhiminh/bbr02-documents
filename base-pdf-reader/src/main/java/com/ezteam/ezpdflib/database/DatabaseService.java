package com.ezteam.ezpdflib.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ezteam.ezpdflib.database.dao.FileInfoDao;
import com.ezteam.ezpdflib.model.FileData;

@Database(
        entities = {FileData.class},
        version = 1, exportSchema = false
)
public abstract class DatabaseService extends RoomDatabase {

    private static DatabaseService instance;

    public abstract FileInfoDao fileDao();

    public static DatabaseService getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseService.class) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        DatabaseService.class,
                        "basepdfreader.db")
                        .addMigrations(new Migration(1, 2) {
                            @Override
                            public void migrate(SupportSQLiteDatabase database) {

                            }
                        })
                        .build();
            }
        }
        return instance;
    }
}