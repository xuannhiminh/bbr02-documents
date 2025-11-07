package officepro.document.reader.viewer.editor.notification

import android.os.Build
import android.os.FileObserver
import android.util.Log
import java.io.File
import java.util.Locale

class FileObserverWrapper(
    private val dirPath: String,
    private val notificationManager: NotificationManager
) {
    private val fileObserver: FileObserver

    companion object {
        private const val TAG = "FileObserverWrapper"
        private val SUPPORTED_EXTENSIONS = listOf(".pdf"
            , ".doc", ".docx",".pptx", ".ppt", ".xls", ".xlsx", ".xlsm", ".txt"
        )
    }

    init {
        fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(File(dirPath), ALL_EVENTS) {
                override fun onEvent(event: Int, path: String?) {
                    if (path == null) return
                    val actualEvent = event and ALL_EVENTS
                    when (actualEvent) {
                        CREATE -> {
                            handleEvent(actualEvent, path)
                        }

                        MOVED_TO -> {
                            handleEvent(actualEvent, path)
                        }
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(dirPath, ALL_EVENTS) {
                override fun onEvent(event: Int, path: String?) {
                    if (path == null) return
                    val actualEvent = event and ALL_EVENTS
                    when (actualEvent) {
                        CREATE -> {
                            handleEvent(actualEvent, path)
                        }

                        MOVED_TO -> {
                            handleEvent(actualEvent, path)
                        }
                    }
                }
            }
        }
    }

    private fun handleEvent(event: Int, path: String?) {
        val file = File(dirPath, path)
        val extension = file.extension.lowercase(Locale.ROOT)

        Log.d(TAG, "New file created: ${file.name} with extension: $extension")

        if (SUPPORTED_EXTENSIONS.any { it == ".$extension" }) {
            Log.d(TAG, "Supported file type detected: ${file.absolutePath}")
            notificationManager.showNewFileNotification(file.name, file.absolutePath)
        } else {
            Log.d(TAG, "Unsupported file type: $extension")
        }
    }

    fun startWatching() {
        Log.d(TAG, "Starting file observer for directory: $dirPath")
        fileObserver.startWatching()
    }

    fun stopWatching() {
        Log.d(TAG, "Stopping file observer for directory: $dirPath")
        fileObserver.stopWatching()
    }
}
