package office.pdf.document.reader.viewer.editor.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import office.pdf.document.reader.viewer.editor.R
import com.ezteam.baseproject.EzListener
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import org.apache.commons.io.FilenameUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object FileSaveManager {


    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return if (cursor != null) {
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val name = cursor.getString(nameIndex)
            cursor.close()
            name
        } else {
            null
        }
    }


//    fun saveUriToDownloads(
//        context: Context,
//        sourceUri: Uri): Uri? {
//        val resolver = context.contentResolver
//
//        return try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                // For Android 10 and above
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.Downloads.DISPLAY_NAME, getFileName(context, sourceUri) ?: "downloaded_file")
//                    put(MediaStore.Downloads.MIME_TYPE, getMimeType(context, sourceUri) ?: "application/octet-stream")
//                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//                }
//
//                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
//                if (uri == null) {
//                    Log.e("saveUriToDownloads", "Failed to create new MediaStore record.")
//                    return null
//                }
//
//                resolver.openOutputStream(uri)?.use { outputStream ->
//                    resolver.openInputStream(sourceUri)?.use { inputStream ->
//                        inputStream.copyTo(outputStream)
//                    } ?: run {
//                        Log.e("saveUriToDownloads", "Failed to open input stream from sourceUri.")
//                        return null
//                    }
//                } ?: run {
//                    Log.e("saveUriToDownloads", "Failed to open output stream to destinationUri.")
//                    return null
//                }
//
//                uri
//            } else {
//                // For Android 9 and below
//                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                if (!downloadsDir.exists()) {
//                    if (!downloadsDir.mkdirs()) {
//                        Log.e("saveUriToDownloads", "Failed to create Downloads directory.")
//                        return null
//                    }
//                }
//                val file = File(downloadsDir, getFileName(context, sourceUri) ?: "downloaded_file")
//                resolver.openInputStream(sourceUri)?.use { inputStream ->
//                    FileOutputStream(file).use { outputStream ->
//                        inputStream.copyTo(outputStream)
//                    }
//                } ?: run {
//                    Log.e("saveUriToDownloads", "Failed to open input stream from sourceUri.")
//                    return null
//                }
//
//                Uri.fromFile(file)
//            }
//        } catch (e: IOException) {
//            Log.e("saveUriToDownloads", "IOException occurred: ${e.message}", e)
//            null
//        } catch (e: SecurityException) {
//            Log.e("saveUriToDownloads", "SecurityException occurred: ${e.message}", e)
//            null
//        } catch (e: Exception) {
//            Log.e("saveUriToDownloads", "Unexpected exception occurred: ${e.message}", e)
//            null
//        }
//    }

    private fun sanitizeFileName(name: String, timestamp: String = ""): String {
        var finalName = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (timestamp.isNotEmpty()) {
            finalName = finalName.substringBeforeLast(".") + "_" + timestamp + "." + finalName.substringAfterLast(".")
        }
        return finalName
    }

    fun saveUriToDownloads(
        context: Context,
        sourceUri: Uri
    ): File? {
        val resolver = context.contentResolver

        return try {
            val timestampName = if (FirebaseRemoteConfigUtil.getInstance().getPDFDetailType() == AppUtils.PDF_DETAIL_EZLIB)
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            else ""

            val timestampForder = if (FirebaseRemoteConfigUtil.getInstance().getPDFDetailType() != AppUtils.PDF_DETAIL_EZLIB)
                ("/${AppUtils.FOLDER_EXTERNAL_IN_DOWNLOADS}/" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + "/")
            else ""

            val relativePath = Environment.DIRECTORY_DOWNLOADS + timestampForder

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {


                // For Android 10 and above
                val fileName = getFileName(context, sourceUri)?.let { sanitizeFileName(it, timestampName) } ?: ("downloaded_file")
                val mimeType = getMimeType(context, sourceUri) ?: "application/octet-stream"


                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
//                    put(MediaStore.Downloads.IS_PENDING, 1) // mark as pending while writing
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri == null) {
                    Log.e("saveUriToDownloads", "Failed to create new MediaStore record.")
                    return null
                }

                resolver.openOutputStream(uri)?.use { outputStream ->
                    resolver.openInputStream(sourceUri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    } ?: run {
                        Log.e("saveUriToDownloads", "Failed to open input stream from sourceUri.")
                        return null
                    }
                } ?: run {
                    Log.e("saveUriToDownloads", "Failed to open output stream to destinationUri.")
                    return null
                }

                // Resolve the file path from the MediaStore URI
                File(Environment.getExternalStoragePublicDirectory(relativePath), fileName)
            } else {
                // For Android 9 and below
                val downloadsDir = Environment.getExternalStoragePublicDirectory(relativePath)
                if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                    Log.e("saveUriToDownloads", "Failed to create MyApp directory.")
                    return null
                }

                val file = File(downloadsDir, getFileName(context, sourceUri) ?: "downloaded_file")
                resolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: run {
                    Log.e("saveUriToDownloads", "Failed to open input stream from sourceUri.")
                    return null
                }

                file
            }
        } catch (e: IOException) {
            Log.e("saveUriToDownloads", "IOException occurred: ${e.message}", e)
            null
        } catch (e: SecurityException) {
            Log.e("saveUriToDownloads", "SecurityException occurred: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("saveUriToDownloads", "Unexpected exception occurred: ${e.message}", e)
            null
        }
    }


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
            filename = FileUtils.getFileName(fileInput)
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
                e.printStackTrace()
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
                throw e
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
            || context.applicationInfo.targetSdkVersion < 30
        ) {
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q
            || context.applicationInfo.targetSdkVersion < 30
        ) {
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
                        Log.e(
                            "XXX",
                            cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH))
                        )
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return pathOut
        }
        return filePath
    }

    fun deleteFile(context: Context, filePath: String, listener: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
            || context.applicationInfo.targetSdkVersion < 30
        ) {
            listener(File(filePath).delete())
            FileUtils.scanFile(context, filePath, object : EzListener {
                override fun onListener() {

                }
            })
        } else {
            MediaScannerConnection.scanFile(
                context, arrayOf(filePath), arrayOf("application/pdf")
            ) { _, uri ->
                if (context.contentResolver.delete(uri, null, null) != -1) {
                    listener(true)
                } else {
                    listener(false)
                }
            }
        }
    }
}