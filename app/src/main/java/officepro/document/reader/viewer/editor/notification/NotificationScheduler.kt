package office.pdf.document.reader.viewer.editor.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar


class NotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val TAG = "NotificationScheduler"
    private val calendarDailyCallOpenApp = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DATE, 1)
        }
    }

    private val calenderUnfinishedReading = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DATE, 1)
        }
    }

    private val calendarForgotten = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DATE, 1)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun scheduleUnfinishedReadingNotification() {
        // Check if the user has granted notification permission
//        if (!checkNotificationPermission()) {
//            Log.d(TAG, "scheduleUnfinishedReadingNotification: Notification permission not granted")
//            return
//        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("request_code", NotificationReceiver.DAILY_CHECK_UNFINISHED_FILES_REQUEST_CODE)
        }
        // Only check if the PendingIntent exists if you're sure it would be still valid
        val existingIntent = PendingIntent.getBroadcast(
            context,
            NotificationManager.UNFINISHED_READING_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existingIntent != null) {
            // Alarm already scheduled; nothing to do
            Log.d(TAG, "scheduleUnfinishedReadingNotification: Alarm already scheduled; nothing to do")
            return
        }
        // Set the alarm time. Optionally include logic to adjust time for today or tomorrow.
        val calendar = calenderUnfinishedReading
        val newPendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationManager.UNFINISHED_READING_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            newPendingIntent
        )
        Log.d(TAG, "scheduleUnfinishedReadingNotification: Alarm scheduled for ${calendar.timeInMillis}")
    }

    fun scheduleForgottenFileNotification() {
        // Check if the user has granted notification permission
//        if (!checkNotificationPermission()) {
//            Log.d(TAG, "scheduleForgottenFileNotification: Notification permission not granted")
//            return
//        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("request_code", NotificationReceiver.DAILY_CHECK_FORGOTTEN_FILES_REQUEST_CODE)
        }

        // Only check if the PendingIntent exists if you're sure it would be still valid
        val existingIntent = PendingIntent.getBroadcast(
            context,
            NotificationManager.FORGOTTEN_FILE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existingIntent != null) {
            // Alarm already scheduled; nothing to do
            Log.d(TAG, "scheduleForgottenFileNotification: Alarm already scheduled; nothing to do")
            return
        }
        // Set the alarm time. Optionally include logic to adjust time for today or tomorrow.

        val calendar = calendarForgotten

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationManager.FORGOTTEN_FILE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d(TAG, "scheduleForgottenFileNotification: Alarm scheduled for ${calendar.timeInMillis}")
    }

    fun scheduleDailyCallOpenAppNotification() {
        return
        // temporary do not use this function
        // Check if the user has granted notification permission
//        if (!checkNotificationPermission()) {
//            Log.d(TAG, "scheduleDailyCallOpenAppNotification: Notification permission not granted")
//            return
//        }
        val calendar = calendarDailyCallOpenApp

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("request_code", NotificationReceiver.DAILY_CALL_OPEN_APP_REQUEST_CODE)
        }

        // Only check if the PendingIntent exists if you're sure it would be still valid
        val existingIntent = PendingIntent.getBroadcast(
            context,
            NotificationManager.DAILY_CALL_OPEN_APP_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existingIntent != null) {
            // Alarm already scheduled; nothing to do
            Log.d(TAG, "scheduleDailyCallOpenAppNotification: Alarm already scheduled; nothing to do")
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationManager.DAILY_CALL_OPEN_APP_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d(TAG, "scheduleDailyCallOpenAppNotification: Alarm scheduled for ${calendar.timeInMillis}")

    }
} 