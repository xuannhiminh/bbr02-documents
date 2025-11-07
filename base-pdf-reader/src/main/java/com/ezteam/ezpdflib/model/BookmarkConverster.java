package com.ezteam.ezpdflib.model;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class BookmarkConverster {

    @TypeConverter
    public String frombookmarkToJson(List<Bookmark> datas) {
        return new Gson().toJson(datas);
    }

    @TypeConverter
    public List<Bookmark> jsonToListbookmark(String jsonData) {
        return new Gson().fromJson(jsonData, new TypeToken<List<Bookmark>>() {
        }.getType());
    }

}
