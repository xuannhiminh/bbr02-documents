package com.ezteam.ezpdflib.model;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "file_data")
public class FileData {

    @ColumnInfo(name = "path")
    @PrimaryKey
    @NonNull
    private String path;
    @ColumnInfo(name = "currentPage")
    private int currentPage;
    @ColumnInfo(name = "total_page")
    private int totalPage;
    @ColumnInfo(name = "bookmark")
    @TypeConverters(BookmarkConverster.class)
    private List<Bookmark> lstbookmark;

    public FileData(@NonNull String path) {
        this.path = path;
    }

    public List<Bookmark> getLstbookmark() {
        return lstbookmark;
    }

    public void setLstbookmark(List<Bookmark> lstbookmark) {
        this.lstbookmark = lstbookmark;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
