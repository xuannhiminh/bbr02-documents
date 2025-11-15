package office.pdf.document.reader.viewer.editor.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    private val TAG = "NotificationReceiver"

    companion object {
        const val DAILY_CHECK_UNFINISHED_FILES_REQUEST_CODE = 1003
        const val DAILY_CHECK_FORGOTTEN_FILES_REQUEST_CODE = 1004
        const val DAILY_CALL_OPEN_APP_REQUEST_CODE = 1005
    }

    override fun onReceive(context: Context, intent: Intent) {
//
//        val notificationManager = NotificationManager(context)
//        val notificationScheduler = NotificationScheduler(context)
//
//        val type = intent.getIntExtra("request_code", -1)
//
//        Log.d(TAG, "Receive intent: type=${type} at ${System.currentTimeMillis()}")
//
//        when (type) {
//            DAILY_CALL_OPEN_APP_REQUEST_CODE -> {
//                notificationManager.showDailyCallOpenAppNotification()
//            }
//            DAILY_CHECK_UNFINISHED_FILES_REQUEST_CODE-> {
//                FileChecker(context).checkAndNotifyUnfinishedFile()
//                notificationScheduler.scheduleUnfinishedReadingNotification()
//            }
//            DAILY_CHECK_FORGOTTEN_FILES_REQUEST_CODE -> {
//                FileChecker(context).checkAndNotifyOldestForgottenFile()
//                notificationScheduler.scheduleForgottenFileNotification()
//            }
//        }
    }
} 