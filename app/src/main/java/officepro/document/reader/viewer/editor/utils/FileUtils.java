package officepro.document.reader.viewer.editor.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.content.ContextCompat;

import officepro.document.reader.viewer.editor.model.FileModel;
import com.ezteam.baseproject.EzListener;
import com.ezteam.baseproject.listener.EzItemListener;
import com.ezteam.baseproject.utils.permisson.PermissionUtils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.'); // Find the last dot in the filename
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex); // Return the name without the extension and dot
        }
        return fileName; // If no extension, return the full filename
    }


    public static ArrayList<FileModel> queryFilesFromInInternalStorage(Context context) {
        File rootDir = new File(context.getFilesDir(), "defaultFiles");
        if (!rootDir.exists()) {
            rootDir.mkdirs(); // Create the directory if it doesn't exist
        }
        List<File> allFiles = new ArrayList<>();

        String[] allowedExtensions = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".xlsm", ".ppt", ".pptx", ".txt"};

        listFilesRecursively(rootDir, allFiles, allowedExtensions);

        ArrayList<FileModel> fileModels = new ArrayList<>();
        for (File file : allFiles) {
            FileModel fileModel = new FileModel();
            fileModel.setPath(file.getAbsolutePath());
            fileModel.setName(getFileNameWithoutExtension(file));
            fileModel.setSize(String.valueOf(file.length()/1024));
            fileModel.setDate(file.lastModified());
            fileModels.add(fileModel);
        }

        return fileModels;
    }

    private static void listFilesRecursively(File dir, List<File> allFiles, String[] allowedExtensions) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                listFilesRecursively(file, allFiles, allowedExtensions);
            } else {
                String name = file.getName().toLowerCase();
                for (String ext : allowedExtensions) {
                    if (name.endsWith(ext)) {
                        allFiles.add(file);
                        break;
                    }
                }
            }
        }
    }

    public static ArrayList<FileModel> getPdfFileList(Context context) {
        if (context == null) {
            return new ArrayList();
        }

        String pdfExt = "_data LIKE '%.pdf' or _data LIKE '%.doc' or _data LIKE '%.docx' or _data LIKE '%.xls' or _data LIKE '%.xlsx' or _data LIKE '%.xlsm' or _data LIKE '%.ppt' or _data LIKE '%.pptx' or _data LIKE '%.txt'";
        Uri ducumentsUri = MediaStore.Files.getContentUri("external");
        String[] docsProjection = {MediaStore.Files.FileColumns.DATA, MediaStore.Images.Media.SIZE, MediaStore.Files.FileColumns.MIME_TYPE,};

        ArrayList<FileModel> rs = queryFilesFromDevice(ducumentsUri, docsProjection, pdfExt, context);
        if(!checkStoragePermission(context)){
            rs.addAll(queryFilesFromInInternalStorage(context));
        }
        return rs;
    }


    private static boolean checkStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static ArrayList<FileModel> getFileListByExtensions(Context context) {
        if (context == null) {
            return new ArrayList<>();
        }

        // Danh sách các phần mở rộng tệp cần tìm
        String selection = MediaStore.Files.FileColumns.DATA + " LIKE '%.pdf' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.doc' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.docx' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.xls' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.xlsx' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.xlsm' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.txt' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.ppt' OR " +
                MediaStore.Files.FileColumns.DATA + " LIKE '%.pptx'";

        // Lấy URI của tệp trong bộ nhớ ngoài
        Uri documentsUri = MediaStore.Files.getContentUri("external");

        // Các cột cần lấy từ MediaStore
        String[] docsProjection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE
        };

        // Thực hiện truy vấn và trả về danh sách tệp
        ArrayList<FileModel> rs = queryFilesFromDevice(documentsUri, docsProjection, selection, context);
        if(!checkStoragePermission(context)){
            rs.addAll(queryFilesFromInInternalStorage(context));
        }
        return rs;
    }
    public static ArrayList<FileModel> queryFilesFromDevice(Uri uri, String[] projection, String selection, final Context context) {
        if (!permissionReadFile(context)) {
            return new ArrayList<>();
        }
        ArrayList arrFile = new ArrayList();
        Cursor c = context.getContentResolver().query(uri, projection,
                selection,
                null,
                null);
        if (c != null) {

            while (c.moveToNext()) {
                Log.d("getFile", "queryFilesFromDevice: " + c.getString(0));
                FileModel fileModel = new FileModel();
                fileModel.setPath(c.getString(0));

                String fileName = fileModel.getPath().substring(fileModel.getPath().lastIndexOf("/") + 1);
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    fileName =  fileName.substring(0, lastDotIndex);
                }
                fileModel.setName(fileName);
                fileModel.setSize(c.getString(1));

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
            Log.d("scanAllFile1", "scanAllFile: co ton tai");
            file.delete();
        }
    }

    public static void scanAllFile(Context context, String Path) {
        deleteNoMedia();
        Log.d("scanAllFile1", "scanAllFile: " + Path);
        try {
            File[] files = getFiles(Path);
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && (
                            getFileExt(f.getPath()).endsWith(".pdf") ||
                            getFileExt(f.getPath()).endsWith(".xls") ||
                            getFileExt(f.getPath()).endsWith(".xlsx") ||
                            getFileExt(f.getPath()).endsWith(".ppt") ||
                            getFileExt(f.getPath()).endsWith(".pptx") ||
                            getFileExt(f.getPath()).endsWith(".xlsm") ||
                            getFileExt(f.getPath()).endsWith(".txt") ||
                            getFileExt(f.getPath()).endsWith(".doc") ||
                            getFileExt(f.getPath()).endsWith(".docx"))
                    ) {

                        Log.d("scanAllFile2", "scanAllFile: " + f.getPath());
                        scanFile(context, f.getPath(), null);
                    } else if (f.isDirectory()) {
                        if (checkPathDeep(f.getPath(), 3)) {
                            continue;
                        }
                        scanAllFile(context, f.getPath());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File [] getFiles(String Path) {
        File file = new File(Path);
        FileFilter filter = f -> {
            if (f.isDirectory()) return true;

            String name = f.getName().toLowerCase();
            return name.endsWith(".pdf")
                    || name.endsWith(".doc")
                    || name.endsWith(".docx")
                    || name.endsWith(".xls")
                    || name.endsWith(".xlsx")
                    || name.endsWith(".xlsm")
                    || name.endsWith(".txt")
                    || name.endsWith(".ppt")
                    || name.endsWith(".pptx");
        };


        File[] files = file.listFiles(filter);
        return files;
    }
    public static void checkFolderSystem(Context context) {
        File allFiles = new File(Environment.getExternalStorageDirectory().getPath());
        scanAllFile(context, allFiles.getPath());
        Log.e("scanAllFile", "Success");
        /*for (String path : arrFolderSystem) {
            scanAllFile(context, path);
        }*/
    }

    public static File searchFileInFolder(String fileName) {
        for (String path : arrFolderSystem) {
            Log.e("XXX", "searchFileInFolder: " + path);
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
        Log.d("scanAllFile1", "scanAllFile: " + path);

        try {
            File file = new File(path);
            FileFilter filter = f -> f.getName().endsWith(".pdf")
                    ||f.getName().endsWith(".xls")
                    ||f.getName().endsWith(".xlsx")
                    ||f.getName().endsWith(".xlsm")
                    ||f.getName().endsWith(".txt")
                    ||f.getName().endsWith(".ppt")
                    ||f.getName().endsWith(".pptx")
                    ||f.getName().endsWith(".doc")
                    ||f.getName().endsWith(".docx")
                    || f.isDirectory();
            File[] files = file.listFiles(filter);
            for (File f : files) {
                if (f.isFile() && (
                        getFileExt(f.getPath()).equals(".pdf") ||
                        getFileExt(f.getPath()).equals(".xls") ||
                        getFileExt(f.getPath()).equals(".xlsx") ||
                        getFileExt(f.getPath()).equals(".xlsm") ||
                        getFileExt(f.getPath()).equals(".txt") ||
                        getFileExt(f.getPath()).equals(".ppt") ||
                        getFileExt(f.getPath()).equals(".pptx") ||
                        getFileExt(f.getPath()).equals(".doc") ||
                        getFileExt(f.getPath()).equals(".docx")
                        )
                ) {
                    String nameOne = FilenameUtils.getBaseName(f.getPath());
                    String nameTwo = FilenameUtils.getBaseName(fileName);
                    Log.i("searchAllFile2", "searchAllFile: " + f.getPath() + "/" + nameOne + "/" + nameTwo + "/" + nameOne.equals(nameTwo));
                    if (nameOne.equals(nameTwo)) {
                        return f.getPath();
                    }
                } else if (f.isDirectory()) {
                    if (checkPathDeep(f.getPath(), 3)) {
                        continue;
                    }
                    pathxxx = searchPathAllFile(context, f.getPath(), fileName);
                    if(!pathxxx.isEmpty()){
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

    public static void rename(Context context, File file, String newName, EzItemListener<RenameStatus> listener, EzItemListener<String> pathNewListener) {
        String name = file.getPath().substring(file.getPath().lastIndexOf("/") + 1);
        File toFile = new File(file.getPath().replace(name, newName));
        if (toFile.exists()) {
            listener.onListener(RenameStatus.EXISTS);
        } else {
            boolean success = file.renameTo(toFile);
            if (success) {
                scanFile(context, file.getPath(), () -> {
                    scanFile(context, toFile.getPath(), () -> {
                        pathNewListener.onListener(toFile.getPath());
                        listener.onListener(RenameStatus.SUCCESS);
                    });
                });
            } else {
                listener.onListener(RenameStatus.FAIL);
            }
        }
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
            Log.d("PathDeep", path + "/" + (path.split("/").length - 1));
            return path.split("/").length - 1 > deep;
        }
        return false;
    }

    private static boolean permissionReadFile(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return PermissionUtils.checkPermissonAccept(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
        }
    }
}
