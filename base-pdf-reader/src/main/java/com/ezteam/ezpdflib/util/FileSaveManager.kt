package com.ezteam.ezpdflib.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.ezteam.ezpdflib.R
import org.apache.commons.io.FilenameUtils
import java.io.*


object FileSaveManager {
    private const val BACKUP_FILE_DICT = "PDF Reader/Backup"

    fun saveFileStorage(
        context: Context,
        fileInput: String?,
        parentPath: String? = null,
        fileNameDisplay: String? = null
    ): Uri? {
        var parentFolder = parentPath
        parentPath ?: let {
            parentFolder = context.getString(R.string.app_name)
        }
        var filename = "$fileNameDisplay.pdf"
        fileNameDisplay ?: let {
            filename = FilenameUtils.getName(fileInput)
        }
        var outputStream: OutputStream? = null
        var fileOutputUri: Uri? = null
        val newFile = File(
            (parentPath?.let { parentFolder } ?: getFolderParent(parentFolder!!))
                    + File.separator
                    + filename
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
            || context.applicationInfo.targetSdkVersion < 30
        ) {
            try {
                outputStream = FileOutputStream(newFile)
                val inputStream = FileInputStream(fileInput)
                val buffer = ByteArray(1024 * 4)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                inputStream.close()
                outputStream.flush()
                outputStream.close()
                fileOutputUri = Uri.fromFile(newFile)
                context.sendBroadcast(
                    Intent(
                        "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                        fileOutputUri
                    )
                )
            } catch (e: Exception) {

            }

        } else {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    getRealtivePath(context, newFile.path)
                )
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            }
            val resolver = context.contentResolver
            return try {
                val inputStream = FileInputStream(fileInput)
                val contentUri = MediaStore.Files.getContentUri("external")
                fileOutputUri = resolver.insert(contentUri, contentValues)
                val pfd: ParcelFileDescriptor?
                try {
                    fileOutputUri?.let { uri ->
                        pfd = resolver.openFileDescriptor(uri, "w")
                        pfd?.let {
                            val out = FileOutputStream(pfd.fileDescriptor)
                            val buf = ByteArray(4 * 1024)
                            var len: Int
                            while (inputStream.read(buf).also { len = it } > 0) {
                                out.write(buf, 0, len)
                            }
                            out.close()
                            inputStream.close()
                            pfd.close()
                        }
                        contentValues.apply {
                            clear()
                            put(MediaStore.Video.Media.IS_PENDING, 0)
                        }
                        resolver.update(uri, contentValues, null, null)
                        outputStream = resolver.openOutputStream(uri)
                        outputStream ?: let {
                            throw IOException("Failed to get output stream.")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                fileOutputUri
            } catch (e: IOException) {
                // Don't leave an orphan entry in the MediaStore
                fileOutputUri?.let {
                    resolver.delete(it, null, null)
                }
                return fileOutputUri
            } finally {
                outputStream?.close()
            }
        }
        return fileOutputUri
    }

    private fun getFolderParent(name: String = ""): String? {
        val mediaStorageDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + Environment.DIRECTORY_DOCUMENTS, name
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return ""
            }
        }
        return mediaStorageDir.absolutePath
    }

    fun checkFileExist(context: Context, fileName: String, folderName: String = ""): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return File(getFolderParent(folderName) + File.separator + fileName + ".pdf").exists()
        } else {
            val projection = arrayOf(
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            val path = Environment.DIRECTORY_DOCUMENTS + File.separator + folderName
            val selection =
                MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " +
                        MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?"
            val selectionargs = arrayOf("%$path%", "%$fileName%")
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionargs,
                null
            )

            if (cursor!!.count > 0) {
                return true
            }
        }
        return false
    }

    private fun getRealtivePath(context: Context, filePath: String?): String? {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            val path = FilenameUtils.getBaseName(File(filePath).parent)
            val selection =
                MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " +
                        MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?"
            val selectionargs = arrayOf("%$path%", "%${FilenameUtils.getBaseName(filePath)}%")
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionargs,
                null
            )
            var pathOut: String? = null
            if (cursor != null) {
                val isDataPresent = cursor.moveToFirst()
                if (isDataPresent) {
                    do {
                        pathOut =
                            cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return pathOut
        }
        return filePath
    }

    fun deleteFile(context: Context, filePath: String, listener: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            listener(File(filePath).delete())
            Utils.scanFile(context, filePath) { }
        } else {
            MediaScannerConnection.scanFile(
                context, arrayOf(filePath), arrayOf("application/pdf")
            ) { _, uri ->
                try {
                    if (context.contentResolver.delete(uri, null, null) != -1) {
                        listener(true)
                    } else {
                        listener(false)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    listener(false)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun copyFile(pathFrom: String?, pathTo: String?, onSuccess: (() -> Unit)? = null) {
        try {
            val src = File(pathFrom)
            val dst = File(pathTo + "/" + src.name)
            val inStream = FileInputStream(src)
            if (!dst.exists()) {
                File(pathTo).mkdirs()
            }
            val outStream = FileOutputStream(dst)
            val inChannel = inStream.channel
            val outChannel = outStream.channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
            inStream.close()
            outStream.close()
            onSuccess?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
            onSuccess?.invoke()
        }

    }

    fun copyFileToBackup(context: Context, pathFrom: String?) {
        copyFile(pathFrom, context.filesDir.path + "/" + BACKUP_FILE_DICT)
    }

    fun checkFileOnBackup(context: Context, path: String?): String? {
        val backupDst = context.filesDir.path + "/" + BACKUP_FILE_DICT
        val backupFolder = File(backupDst)
        val fileName = FilenameUtils.getBaseName(path)
        backupFolder.listFiles()?.let {
            it.forEach { file ->
                if (fileName.equals(FilenameUtils.getBaseName(file.path))) {
                    return file.path
                }
            }
        }
        return null
    }
}