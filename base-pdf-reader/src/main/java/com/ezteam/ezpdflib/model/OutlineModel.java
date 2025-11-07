package com.ezteam.ezpdflib.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class OutlineModel implements Parcelable {
    private int level;
    private String title;
    private int page;
    private ArrayList<OutlineModel> listChild = new ArrayList<>();

    public OutlineModel(int level, String title, int page) {
        this.level = level;
        this.title = title;
        this.page = page;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ArrayList<OutlineModel> getListChild() {
        return listChild;
    }

    public void setListChild(ArrayList<OutlineModel> listChild) {
        this.listChild.clear();
        this.listChild.addAll(listChild);
    }

    protected OutlineModel(Parcel in) {
        level = in.readInt();
        title = in.readString();
        page = in.readInt();
        listChild = in.createTypedArrayList(OutlineModel.CREATOR);
    }

    public static final Creator<OutlineModel> CREATOR = new Creator<OutlineModel>() {
        @Override
        public OutlineModel createFromParcel(Parcel in) {
            return new OutlineModel(in);
        }

        @Override
        public OutlineModel[] newArray(int size) {
            return new OutlineModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level);
        dest.writeString(title);
        dest.writeInt(page);
        dest.writeTypedList(listChild);
    }
}
