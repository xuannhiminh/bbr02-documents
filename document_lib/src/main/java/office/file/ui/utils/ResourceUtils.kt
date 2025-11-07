package office.file.ui.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object ResourceUtils {

    fun getFilePathFromUri(context: Context, uri: Uri, uniqueName: Boolean): String =
        if (uri.path?.contains("file://") == true) uri.path!!
        else getFileFromContentUri(context, uri, uniqueName).path

    private fun getFileFromContentUri(context: Context, contentUri: Uri, uniqueName: Boolean): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(context, contentUri) ?: ""
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = ("temp_file_" + if (uniqueName) timeStamp else "") + ".$fileExtension"
        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()
        // Initialize streams
        var oStream: FileOutputStream? = null
        var inputStream: InputStream? = null

        try {
            oStream = FileOutputStream(tempFile)
            inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let { copy(inputStream, oStream) }
            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Close streams
            inputStream?.close()
            oStream?.close()
        }

        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String? =
        if (uri.scheme == ContentResolver.SCHEME_CONTENT)
            MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
        else uri.path?.let { MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(it)).toString()) }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }


    fun loadData(context: Context, inFile: String): String {
        var tContents = ""

        try {
            val stream = context.assets.open(inFile);

            val size = stream.available();
            val buffer = ByteArray(size)
            stream.read(buffer);
            stream.close();
            tContents = String(buffer);
        } catch (e: IOException) {
            // Handle exceptions here
        }

        return tContents;
    }

    fun listAssetFiles(path: String, context: Context): List<String> {
        val result = ArrayList<String>()
        context.assets.list(path)?.forEach { file ->
            val innerFiles = listAssetFiles("$path/$file", context)
            if (!innerFiles.isEmpty()) {
                result.addAll(innerFiles)
            } else {
                // it can be an empty folder or file you don't like, you can check it here
                result.add("$path/$file")
            }
        }
        return result
    }

    fun getBitmapFromAsset(context: Context, path: String): Bitmap =
        context.assets.open(path).use { BitmapFactory.decodeStream(it) }

    fun getDrawableFromAsset(context: Context, path: String): Drawable? = Drawable.createFromStream(
        context.assets.open(
            path
        ), null
    )
}