package com.ezstudio.pdftoolmodule.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.ezstudio.pdftoolmodule.model.FileModel;
import com.ezstudio.pdftoolmodule.utils.pdftool.Password;
import com.ezteam.baseproject.EzListener;
import com.ezteam.baseproject.listener.EzItemListener;
import com.ezteam.baseproject.utils.permisson.PermissionUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class FileUtils {
    public static ArrayList<FileModel> getPdfFileList(Context context) {
        if (context == null) {
            return new ArrayList();
        }

        String pdfExt = "_data LIKE '%.pdf'";
        Uri ducumentsUri = MediaStore.Files.getContentUri("external");
        String[] docsProjection = {MediaStore.Files.FileColumns.DATA, MediaStore.Images.Media.SIZE, MediaStore.Files.FileColumns.MIME_TYPE,};
        return queryFilesFromDevice(ducumentsUri, docsProjection, pdfExt, context);
    }

    public static ArrayList<FileModel> queryFilesFromDevice(Uri uri, String[] projection, String selection, final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                return new ArrayList<>();
            }
        } else if (!PermissionUtils.checkPermissonAccept(context,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return new ArrayList<>();
        }
        ArrayList arrFile = new ArrayList();
        Cursor c = context.getContentResolver().query(uri, projection,
                selection,
                null,
                null);
        if (c != null) {

            while (c.moveToNext()) {
                FileModel fileModel = new FileModel();
                fileModel.setPath(c.getString(0));

                String name = fileModel.getPath().substring(fileModel.getPath().lastIndexOf("/") + 1);
                fileModel.setName(name.substring(0, (name.length() - 4)));
                fileModel.setSize(c.getString(1));
//                fileModel.setHasPassword(Password.INSTANCE.isPDFEncrypted(fileModel.getPath()));
                File file = new File(fileModel.getPath());
                fileModel.setDate(file.lastModified());
                if (file.isFile()) {
                    arrFile.add(fileModel);
                }
            }
            c.close();
        }

        return arrFile;
    }

    private static String[] arrFolderSystem = {
            Environment.getExternalStorageDirectory().getPath(),
            Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS,
            Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOCUMENTS};


    // k dung ham nay ma dung ham ben kia

    public static void deleteNoMedia() {
        File file = new File("/storage/emulated/0/Telegram/Telegram Documents/.nomedia");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void scanAllFile(Context context, String Path) {
        deleteNoMedia();
        try {
            File file = new File(Path);
            FileFilter filter = f -> f.getName().endsWith(".pdf") || f.isDirectory();
            File[] files = file.listFiles(filter);
            for (File f : files) {
                if (f.isFile() && getFileExt(f.getPath()).endsWith(".pdf")) {

                    scanFile(context, f.getPath(), null);
                } else if (f.isDirectory()) {
                    if (checkPathDeep(f.getPath(), 3)) {
                        continue;
                    }
                    scanAllFile(context, f.getPath());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkFolderSystem(Context context) {
        File allFiles = new File(Environment.getExternalStorageDirectory().getPath());
        scanAllFile(context, allFiles.getPath());
        /*for (String path : arrFolderSystem) {
            scanAllFile(context, path);
        }*/
    }

    public static File searchFileInFolder(String fileName) {
        for (String path : arrFolderSystem) {
            try {
                File file = new File(path);
                File[] files = file.listFiles();
                for (File f : files) {
                    if (f.isFile()) {
                        if (getFileName(f.getPath()).equals(fileName)) {
                            return f;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String searchPathAllFile(Context context, String path, String fileName) {
        deleteNoMedia();
        String pathxxx = "";

        try {
            File file = new File(path);
            FileFilter filter = f -> f.getName().endsWith(".pdf") || f.isDirectory();
            File[] files = file.listFiles(filter);
            for (File f : files) {
                if (f.isFile() && getFileExt(f.getPath()).equals(".pdf")) {
                    if (getFileName(f.getPath()).equals(fileName)) {
                        return f.getPath();
                    }
                } else if (f.isDirectory()) {
                    if (checkPathDeep(f.getPath(), 3)) {
                        continue;
                    }
                    pathxxx = searchPathAllFile(context, f.getPath(), fileName);
                    if (!pathxxx.isEmpty()) {
                        return pathxxx;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pathxxx;
    }

    public static String searchPathInFolder(Context context, String fileName) {
        File allFiles = new File(Environment.getExternalStorageDirectory().getPath());
        return searchPathAllFile(context, allFiles.getPath(), fileName);
    }

    public static String getFileName(String path) {
        if (path == null) {
            return "not found";
        }
        int index = path.lastIndexOf('/');
        if (index == -1) {
            return "not found";
        } else {
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

    public static String getFileExt(String path) {
        return path.substring(path.lastIndexOf('.'), path.length());
    }

    public static void scanFile(final Context context, String pathFile, EzListener listener) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(pathFile));
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
        MediaScannerConnection.scanFile(context, new String[]{pathFile}, null, (path, uri) -> {
            if (listener != null) {
                listener.onListener();
            }
        });
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getFileExtension(String filePath) {
        String extension = "";
        try {
            extension = filePath.substring(filePath.lastIndexOf("."));
        } catch (Exception exception) {
            Log.e("Err", exception.toString() + "");
        }
        return extension;
    }

    public static String getFileName(Context context, Uri uri) {
        try {
            String result = null;
            if (uri.getScheme().equals("content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            return result;
        } catch (Exception e) {

        }
        return "";
    }

    public static boolean checkPathDeep(String path, int deep) {
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        if (path.contains(rootPath)) {
            path = path.replace(rootPath, "");
            return path.split("/").length - 1 > deep;
        }
        return false;
    }
}
