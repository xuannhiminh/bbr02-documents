package com.ezteam.ezpdflib.model;

public class Bookmark {

    private int page;
    private long timeSave;

    public Bookmark(int page, long timeSave) {
        this.page = page;
        this.timeSave = timeSave;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getTimeSave() {
        return timeSave;
    }

    public void setTimeSave(long timeSave) {
        this.timeSave = timeSave;
    }
}
