package com.ezteam.ezpdflib.database.repository;

import com.ezteam.ezpdflib.database.DatabaseService_Impl;
import com.ezteam.ezpdflib.database.dao.FileInfoDao;
import com.ezteam.ezpdflib.model.FileData;

import io.reactivex.rxjava3.core.Single;

public class FileRepository {
    private FileInfoDao dao;

    public FileRepository(FileInfoDao dao) {
        this.dao = dao;
    }

    public Single<FileData> getFileStatus(String path) {
        return Single.create(emitter -> {
            FileData fileData = dao.getFileStatus(path);
            if (fileData != null)
                emitter.onSuccess(fileData);
        });
    }

    public void updateFileStatus(FileData fileData) {
        dao.updateFileStatus(fileData);
    }

}
