package com.ezteam.baseproject.photopicker

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import com.ezteam.baseproject.R
import java.io.File
import java.io.InputStream
import java.util.*

object ImageStorageUtils {
    suspend fun getImage(context: Context): ArrayList<Photo> {
        val images: ArrayList<Photo> = arrayListOf()
        val cursor: Cursor? = context
            .contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf("_data", "bucket_display_name"),
                null, null, null
            )

        if (cursor != null) {
            val columnIndexData: Int = cursor.getColumnIndexOrThrow("_data")
            while (cursor.moveToNext()) {
                val pathFile = cursor.getString(columnIndexData)
                val file = File(pathFile)
                if (file.exists()) {
                    val image = Photo(0, pathFile)
                    images.add(image)
                }
            }
        }

        return images
    }

    fun getPhotoDirs(context: Context): MutableList<PhotoDirectory> {
        val directories: MutableList<PhotoDirectory> = mutableListOf()
        val photoDirectoryAll = PhotoDirectory()
        photoDirectoryAll.name = context.getString(R.string.all_image)
        photoDirectoryAll.id = "ALL"
        val cursor: Cursor? = context
            .contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    BaseColumns._ID,
                    MediaColumns.DATA,
                    MediaColumns.BUCKET_ID,
                    MediaColumns.BUCKET_DISPLAY_NAME,
                    MediaColumns.SIZE,
                    MediaColumns.DATE_ADDED
                ),
                null, null, null
            )
        cursor?.let { data ->
            if (data.moveToFirst()) {
                do {
                    val imageId: Int = data.getInt(data.getColumnIndexOrThrow(BaseColumns._ID))
                    val bucketId: String? =
                        data.getString(data.getColumnIndexOrThrow(MediaColumns.BUCKET_ID))
                    val name: String? =
                        data.getString(data.getColumnIndexOrThrow(MediaColumns.BUCKET_DISPLAY_NAME))
                    val path: String? =
                        data.getString(data.getColumnIndexOrThrow(MediaColumns.DATA))
                    val size: Long =
                        data.getInt(data.getColumnIndexOrThrow(MediaColumns.SIZE)).toLong()
                    val dateAdded: Long =
                        data.getLong(data.getColumnIndexOrThrow(MediaColumns.DATE_ADDED))
                    if (size < 1) continue
                    val photoDirectory = PhotoDirectory()
                    photoDirectory.id = bucketId ?: ""
                    photoDirectory.name = name ?: ""
                    if (!directories.contains(photoDirectory)) {
                        photoDirectory.coverPath = path
                        photoDirectory.addPhoto(imageId, path, dateAdded)
                        photoDirectory.dateAdded =
                            data.getLong(data.getColumnIndexOrThrow(MediaColumns.DATE_ADDED))
                        directories.add(photoDirectory)
                    } else {
                        directories[directories.indexOf(photoDirectory)].addPhoto(
                            imageId,
                            path,
                            dateAdded
                        )
                    }
                    path?.let {
                        photoDirectoryAll.addPhoto(imageId, path, dateAdded)
                    }
                } while (data.moveToNext())
            }

            if (photoDirectoryAll.photoPaths.size > 0) {
                photoDirectoryAll.coverPath = photoDirectoryAll.photoPaths[0]
            }

            directories.sort()

            directories.add(0, photoDirectoryAll)
        }
        return directories
    }

    fun getPhotoDirs(context: Context, photoListener: (Photo) -> Unit) {
        try {
            val cursor: Cursor? = context
                .contentResolver
                .query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        BaseColumns._ID,
                        MediaColumns.DATA,
                        MediaColumns.BUCKET_ID,
                        MediaColumns.BUCKET_DISPLAY_NAME,
                        MediaColumns.SIZE,
                        MediaColumns.DATE_ADDED
                    ),
                    null, null, null
                )
            cursor?.let { data ->
                if (data.moveToFirst()) {
                    do {
                        val imageId: Long =
                            data.getLong(data.getColumnIndexOrThrow(BaseColumns._ID))
                        val bucketId: String? =
                            data.getString(data.getColumnIndexOrThrow(MediaColumns.BUCKET_ID))
                        val name: String? =
                            data.getString(data.getColumnIndexOrThrow(MediaColumns.BUCKET_DISPLAY_NAME))
                        val path: String? =
                            data.getString(data.getColumnIndexOrThrow(MediaColumns.DATA))
                        val size: Long =
                            data.getInt(data.getColumnIndexOrThrow(MediaColumns.SIZE)).toLong()
                        val dateAdded: Long =
                            data.getLong(data.getColumnIndexOrThrow(MediaColumns.DATE_ADDED))
                        if (size < 1) continue

                        val photo =
                            Photo(imageId, path ?: "", bucketId ?: "", name ?: "", false, dateAdded)
                        photoListener(photo)
                    } while (data.moveToNext())
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun loadBitmapFromAssets(context: Context, path: String): Bitmap? {
        var stream: InputStream? = null
        try {
            stream = context.assets.open(path)
            return BitmapFactory.decodeStream(stream)
        } catch (e: Exception) {
        } finally {
            try {
                stream?.close()
            } catch (e: Exception) {
            }
        }
        return null
    }
}