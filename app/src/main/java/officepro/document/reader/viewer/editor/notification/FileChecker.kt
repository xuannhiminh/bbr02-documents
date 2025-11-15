package office.pdf.document.reader.viewer.editor.notification

import android.content.Context
import android.util.Log
import office.pdf.document.reader.viewer.editor.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.get

class FileChecker(context: Context) {
    companion object {
        private const val DAYS_TO_FILTER_FORGOTTEN_FILE = 3
        private const val DAYS_FILTER_FORGOTTEN_FILE_IN_MILLIS = DAYS_TO_FILTER_FORGOTTEN_FILE * 24 * 60 * 60 * 1000L
        private const val DAYS_TO_FILTER_UNFINISHED_FILE = 2
        private const val DAYS_FILTER_UNFINISHED_FILE_IN_MILLIS = DAYS_TO_FILTER_UNFINISHED_FILE * 24 * 60 * 60 * 1000L
    }

    private val database = AppDatabase.getInstance(context)
    private val fileDao = database.serverDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val notificationManager = NotificationManager(context)


    fun checkAndNotifyOldestForgottenFile() {
        coroutineScope.launch {
            val currentTime = System.currentTimeMillis()
            Log.d("FileChecker", "Current time: ${Date(currentTime)}")
            val oldestFile = fileDao.getOldestForgottenFile(currentTime, DAYS_FILTER_FORGOTTEN_FILE_IN_MILLIS)
            oldestFile?.let { file ->
                notificationManager.showForgottenFileNotification(
                    file.path
                )

            } ?:
            Log.d("FileChecker", "No unread files found.")
        }
    }

    fun checkAndNotifyUnfinishedFile(): Boolean {
        var result = false
        coroutineScope.launch {
            val currentTime = System.currentTimeMillis()
            Log.d("FileChecker", "Current time: ${Date(currentTime)}")
            val unfinishedFile = fileDao.getLatestUnFinishedFile(currentTime, DAYS_FILTER_UNFINISHED_FILE_IN_MILLIS)
            if (unfinishedFile != null) {
                notificationManager.showCallUseAppUnfinishedReadingNotification(
                    null,
                    unfinishedFile.path
                )
                result = true
            } else {
                Log.d("FileChecker", "No unfinished files found.")
            }
        }
        return result
    }

    fun checkAndNotifyLatestFile(): Boolean  {
        var result = false
        coroutineScope.launch {
            val currentTime = System.currentTimeMillis()
            Log.d("FileChecker", "Current time: ${Date(currentTime)}")
            val latestFiles = fileDao.getLatestFiles()
            if( latestFiles.isEmpty()) {
                Log.d("FileChecker", "No files found.")
                result = false
                return@launch
            }
            notificationManager.showCallUseAppLatestFileNotification(
                latestFiles[0].path
            )
            result = true
        }
        return result
    }

} 