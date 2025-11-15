package office.pdf.document.reader.viewer.editor.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.brian.base_iap.utils.IAPUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.RemoteMessage
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.screen.start.SplashActivity
import java.io.File
import android.app.NotificationManager as AndroidNotificationManager


class NotificationManager(private val context: Context) {
    private val TAG = "NotificationManager"
    companion object {
        const val HIGH_CHANNEL_ID = "all_pdf_reader_channel"
        const val CALL_USE_APP_CHANNEL_ID = "all_pdf_reader_call_use_app_channel"
        const val CALL_USE_APP_OUT_CHANNEL_ID = "all_pdf_reader_call_use_app_channel"
        const val HIGH_CHANNEL_NAME = "All Document Notifications"
        const val CALL_USE_APP_CHANNEL_NAME = "All Document Call Notifications"
        const val CHANNEL_NAME_SERVICE = "All Document Service Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for All Document app"
        const val CHANNEL_DESCRIPTION_FOREGROUND = "Notifications for All Document service"
        const val CHANNEL_ID_FOREGROUND = "all_pdf_reader_channel_foreground"
        const val GROUP_ID_FOREGROUND = "all_pdf_reader_group_foreground"

        // Channel thông báo cập nhật ứng dụng
        const val CHANNEL_ID_UPDATE = "all_pdf_reader_channel_update"
        const val GROUP_ID_UPDATE = "all_pdf_reader_group_update"
        const val CHANNEL_NAME_UPDATE = "All Document Update Notifications"
        const val CHANNEL_DESCRIPTION_UPDATE = "Notifications for All Document app updates and new features"

        // Notification IDs
        const val NEW_FILE_NOTIFICATION_ID = 1001
        const val UNFINISHED_READING_NOTIFICATION_ID = 1002
        const val FORGOTTEN_FILE_NOTIFICATION_ID = 1003
        const val DAILY_CALL_OPEN_APP_NOTIFICATION_ID = 1004
        const val WIDGETS_NOTIFICATION_ID = 1005
        const val CALL_USE_APP_NOTIFICATION_ID = 1006
        const val CALL_USE_APP_NOTIFICATION_OUT_ID = 1010
        const val SCREENSHOT_NOTIFICATION_ID = 1007
        const val UPDATE_NOTIFICATION_ID = 1008
        const val FCM_NOTIFICATION_ID = 1009
    }
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    private fun logEvent(event: String) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            firebaseAnalytics.logEvent(event, Bundle())
            PreferencesUtils.putInteger("time_notification_show", PreferencesUtils.getInteger("time_notification_show", 0) + 1)
        }
    }

    fun cancelNotification(notificationID: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.cancel(notificationID)
    }

    fun createForegroundNotification() : Notification {
        val remoteCustomView = createWidgetNotificationView()
        val remoteCustomBigView = createWidgetNotificationBigView()
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_FOREGROUND)
                .setSmallIcon(R.drawable.icon_notification)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setCustomContentView(remoteCustomView)
                .setDeleteIntent(createDismissPendingIntent(WIDGETS_NOTIFICATION_ID))
                .setCustomBigContentView(remoteCustomBigView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(0)
                .build()
            return notification
        }  catch (e : Exception) {
            Log.e(TAG, "createForegroundNotification: Error creating notification", e)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_FOREGROUND)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setCustomContentView(remoteCustomView)
                .setDeleteIntent(createDismissPendingIntent(WIDGETS_NOTIFICATION_ID))
                .setCustomBigContentView(remoteCustomBigView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(0)
                .build()
            return notification
        }
    }

    fun createNotificationChannel() {
        val highChanel = NotificationChannel(
            HIGH_CHANNEL_ID,
            HIGH_CHANNEL_NAME,
            AndroidNotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.createNotificationChannel(highChanel)

        val callUseAppChannel = NotificationChannel(
            CALL_USE_APP_CHANNEL_ID,
            CALL_USE_APP_CHANNEL_NAME,
            AndroidNotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            setSound(null, null) // Disable sound
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200)
        }

        notificationManager.createNotificationChannel(callUseAppChannel)

        // Create a update notification channel
        val updateChannel = NotificationChannel(
            CHANNEL_ID_UPDATE,
            CHANNEL_NAME_UPDATE,
            AndroidNotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION_UPDATE
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500) // Customize as needed
        }
        notificationManager.createNotificationChannel(updateChannel)

    }

    private val FILE_EXTENSION_TO_ICON = mapOf(
        "pdf" to R.drawable.icon_pdf,
        "doc" to R.drawable.icon_word,
        "docx" to R.drawable.icon_word,
        "xls" to R.drawable.icon_excel,
        "xlsx" to R.drawable.icon_excel,
        "xlsm" to R.drawable.icon_excel,
        "ppt" to R.drawable.icon_ppt,
        "pptx" to R.drawable.icon_ppt
    )

    private val FILE_EXTENSION_TO_BACKGROUND = mapOf(
        "pdf" to R.drawable.btn_background_red,
        "doc" to R.drawable.btn_background_blue,
        "docx" to R.drawable.btn_background_blue,
        "xls" to R.drawable.btn_background_green,
        "xlsx" to R.drawable.btn_background_green,
        "xlsm" to R.drawable.btn_background_green,
        "ppt" to R.drawable.btn_background_orange,
        "pptx" to R.drawable.btn_background_orange
    )

    private fun createCustomNotificationView(
        title: String,
        content: String,
        filePath: String? = null,
        notificationID: Int? = null,
        buttonTitle: String? = null
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_collapsed_custom)
        remoteViews.setTextViewText(R.id.text_title, title)
        remoteViews.setTextViewText(R.id.text_content, content)
        buttonTitle?.let { remoteViews.setTextViewText(R.id.button_open, it) }

        val fileExtension = filePath?.substringAfterLast(".")
        val backgroundResource = FILE_EXTENSION_TO_BACKGROUND.getOrDefault(fileExtension, R.drawable.btn_background_red)
        remoteViews.setInt(R.id.button_open, "setBackgroundResource", backgroundResource)

        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("${context.packageName}.isFromNotification", true)
            notificationID?.let { putExtra("${context.packageName}.notificationID", it) }
            filePath?.let { putExtra("${context.packageName}.filePath", it) }
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.container, openAppPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.button_open, openAppPendingIntent)

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }

        return remoteViews
    }
    private fun createCustomNotificationViewNew(
        title: String,
        content: String,
        filePath: String? = null,
        notificationID: Int? = null,
        buttonTitle: String? = null
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_collapsed_custom_4)
        remoteViews.setTextViewText(R.id.text_title, title)
       // remoteViews.setTextViewText(R.id.text_content, content)
        buttonTitle?.let { remoteViews.setTextViewText(R.id.button_open, it) }

        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("${context.packageName}.isFromNotification", true)
            notificationID?.let { putExtra("${context.packageName}.notificationID", it) }
            filePath?.let { putExtra("${context.packageName}.filePath", it) }
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.container, openAppPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.button_open, openAppPendingIntent)

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }

        return remoteViews
    }
    private fun createCustomNotificationViewRecent(
        title: String,
        content: String,
        filePath: String? = null,
        notificationID: Int? = null,
        buttonTitle: String? = null
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_collapsed_custom_3)
        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }
        setClickAction(context, remoteViews, R.id.button_open, SplashActivity::class.java)
        return remoteViews
    }
    private fun createCustomNotificationBigViewNew(
        title: String,
        content: String,
        filePath: String? = null,
        notificationID: Int? = null,
        buttonTitle: String? = null
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_expand_custom_2)

        val safeTitle = title.orEmpty()
        val spaceIndex = safeTitle.indexOfFirst { it == ' ' }

        // Guard case: no space found (-1)
        if (spaceIndex > 0 && spaceIndex < safeTitle.length) {
            remoteViews.setTextViewText(R.id.text_title, safeTitle.substring(spaceIndex)) // after space
            remoteViews.setTextViewText(R.id.icon_text_title, safeTitle.substring(0, spaceIndex)) // before space
        } else {
            // Fallback: show full title in one place or empty in the other
            remoteViews.setTextViewText(R.id.text_title, safeTitle)
            remoteViews.setTextViewText(R.id.icon_text_title, "\uD83D\uDCC1")
        }

        remoteViews.setTextViewText(R.id.text_content, content)
        buttonTitle?.let { remoteViews.setTextViewText(R.id.button_open, buttonTitle) }

        val fileExtension = filePath?.substringAfterLast(".")
        val iconResource = FILE_EXTENSION_TO_ICON.getOrDefault(fileExtension, R.drawable.icon_pdf)
        remoteViews.setImageViewResource(R.id.image_file_icon, iconResource)

        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("${context.packageName}.isFromNotification", true)
            filePath?.let { putExtra("${context.packageName}.filePath", it) }
            notificationID?.let { putExtra("${context.packageName}.notificationID", it) }
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.container, openAppPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.button_open, openAppPendingIntent)

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }

        return remoteViews
    }
    private fun createCustomNotificationBigView(
        title: String,
        content: String,
        filePath: String? = null,
        notificationID: Int? = null,
        buttonTitle: String? = null
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_expand_custom)
        remoteViews.setTextViewText(R.id.text_title, title)
        remoteViews.setTextViewText(R.id.text_content, content)
        buttonTitle?.let { remoteViews.setTextViewText(R.id.button_open, buttonTitle) }

        val fileExtension = filePath?.substringAfterLast(".")
        val iconResource = FILE_EXTENSION_TO_ICON.getOrDefault(fileExtension, R.drawable.icon_pdf)
        remoteViews.setImageViewResource(R.id.image_file_icon, iconResource)
        val backgroundResource = FILE_EXTENSION_TO_BACKGROUND.getOrDefault(fileExtension, R.drawable.btn_background_red)
        remoteViews.setInt(R.id.button_open, "setBackgroundResource", backgroundResource)

        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("${context.packageName}.isFromNotification", true)
            filePath?.let { putExtra("${context.packageName}.filePath", it) }
            notificationID?.let { putExtra("${context.packageName}.notificationID", it) }
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.container, openAppPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.button_open, openAppPendingIntent)

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }

        return remoteViews
    }

    private fun createCustomNotificationViewScreenShot(
        filePath: String? = null,
        notificationID: Int? = null,
    ): RemoteViews {
        val remoteViews =  RemoteViews(context.packageName, R.layout.notification_collapsed_custom_screenshot)

        remoteViews.setTextViewText(R.id.text_title,  context.getString(R.string.notification_title_11, filePath?.substringAfterLast("/") ?: "Screenshot"))
        remoteViews.setTextViewText(R.id.text_content, context.getString(R.string.notification_description_11))

        // Set the image preview using FileProvider
        val imageFile = filePath?.let { File(it) }

        if (imageFile != null) {
            if (imageFile.exists()) {
                val authority = "${context.packageName}.provider" //Or your defined authority
                val contentUri: Uri? = try {
                    FileProvider.getUriForFile(
                        context.applicationContext,
                        authority,
                        imageFile
                    )
                } catch (e: IllegalArgumentException) {
                    // Handle the case where the file path is not valid or not configured in file_paths.xml
                    Log.e("FileProvider", "Error getting URI for file: ${imageFile.path}", e)
                    null
                }

                if (contentUri != null) {
                    // Now use this contentUri with RemoteViews.setUri()
                    remoteViews.setUri(R.id.image_preview, "setImageURI", contentUri) // Example for an ImageView
                } else {

                    // Handle the error, e.g., show a message to the user
                }
            } else {
                // Handle file not found
                Log.e("FileAccess", "File not found: ${imageFile.path}")
            }
        }

        // Tạo intent để mở app khi bấm vào nút button_open
        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("${context.packageName}.isFromNotification", true)
            notificationID?.let { putExtra("${context.packageName}.notificationID", it) }
            filePath?.let { putExtra("${context.packageName}.filePath", it) }
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.container, openAppPendingIntent)

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }

        return remoteViews
    }

    private fun createCustomNotificationBigViewScreenShot(
        filePath: String? = null,
        notificationID: Int? = null,
    ): RemoteViews {
        val remoteViews =  RemoteViews(context.packageName, R.layout.notification_expand_custom_screenshot)

        remoteViews.setTextViewText(R.id.text_title,  context.getString(R.string.notification_title_11, filePath?.substringAfterLast("/") ?: "Screenshot"))
        remoteViews.setTextViewText(R.id.text_content, context.getString(R.string.notification_description_11))
        remoteViews.setTextViewText(R.id.button_open, context.getString(R.string.open_now))

        // Set the image preview using FileProvider
        val imageFile = filePath?.let { File(it) }

        if (imageFile != null) {
            if (imageFile.exists()) {
                val authority = "${context.packageName}.provider" //Or your defined authority
                val contentUri: Uri? = try {
                    FileProvider.getUriForFile(
                        context.applicationContext,
                        authority,
                        imageFile
                    )
                } catch (e: IllegalArgumentException) {
                    // Handle the case where the file path is not valid or not configured in file_paths.xml
                    Log.e("FileProvider", "Error getting URI for file: ${imageFile.path}", e)
                    null
                }

                if (contentUri != null) {
                    // Now use this contentUri with RemoteViews.setUri()
                    remoteViews.setUri(R.id.image_preview, "setImageURI", contentUri) // Example for an ImageView
                } else {

                    // Handle the error, e.g., show a message to the user
                }
            } else {
                // Handle file not found
                Log.e("FileAccess", "File not found: ${imageFile.path}")
            }
        }

        // Tạo intent để mở app khi bấm vào nút button_open
        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("${context.packageName}.isFromNotification", true)
            notificationID?.let { putExtra("${context.packageName}.notificationID", it) }
            filePath?.let { putExtra("${context.packageName}.filePath", it) }
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.container, openAppPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.button_open, openAppPendingIntent)

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }

        return remoteViews
    }

    private fun setClickAction(context: Context, views: RemoteViews, viewId: Int, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity).apply {
            action = System.currentTimeMillis().toString() // Unique action to ensure it's recognized
            putExtra("${context.packageName}.isFromWidget", true)
            putExtra("${context.packageName}.isFromNotification", true)
            putExtra("${context.packageName}.whereToOpen", viewId)
            putExtra("${context.packageName}.notificationID", WIDGETS_NOTIFICATION_ID)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            viewId, // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(viewId, pendingIntent)
    }

    private fun createWidgetNotificationView(): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_collapsed_widget)
        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }
        setClickAction(context, remoteViews, R.id.ivHome, SplashActivity::class.java)
        setClickAction(context, remoteViews, R.id.ivRecent, SplashActivity::class.java)
        setClickAction(context, remoteViews, R.id.ivBookmarks, SplashActivity::class.java)
        setClickAction(context, remoteViews, R.id.ivEdit, SplashActivity::class.java)
        return remoteViews
    }
    private fun createWidgetNotificationBigView(): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_expand_widget)
        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            val paddingInPx = context.resources.getDimensionPixelSize(R.dimen._12sdp)
            remoteViews.setViewPadding(R.id.container, paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        }
        setClickAction(context, remoteViews, R.id.ivHome, SplashActivity::class.java)
        setClickAction(context, remoteViews, R.id.ivRecent, SplashActivity::class.java)
        setClickAction(context, remoteViews, R.id.ivBookmarks, SplashActivity::class.java)
        setClickAction(context, remoteViews, R.id.ivEdit, SplashActivity::class.java)
        return remoteViews
    }

    fun showNewFileNotification(fileName: String, filePath: String? = null) {
        val customView = createCustomNotificationView(
            fileName,
            context.getString(R.string.new_file_content_notification),
            filePath,
            NEW_FILE_NOTIFICATION_ID
        )

        val customBigView = createCustomNotificationBigView(
            fileName,
            context.getString(R.string.new_file_content_notification),
            filePath,
            NEW_FILE_NOTIFICATION_ID
        )

        val notification = NotificationCompat.Builder(context, HIGH_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(NEW_FILE_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${NEW_FILE_NOTIFICATION_ID}")
    }


    fun showCallUseAppNotification() {
//        var lastTimeShowNotification = PreferencesUtils.getLong("last_time_show_notification", 0)
//        if (System.currentTimeMillis() - lastTimeShowNotification < FirebaseRemoteConfigUtil.getInstance().getNotificationFrequencyMinutes() * 60L * 1000L) {
//            Log.d(TAG, "Notification not shown, last time: $lastTimeShowNotification")
//            return
//        }
//
//        lastTimeShowNotification = System.currentTimeMillis()
//        PreferencesUtils.putLong("last_time_show_notification", lastTimeShowNotification)
        val canShowNotification = NotificationDecider.canShowNotification(context);
        if (!canShowNotification) {
            Log.d(TAG, "Notification not shown, not enough time passed since last notification")
            return
        }

        val notificationInfo = getNextNotificationInfo() ?: return

        val customView = createCustomNotificationViewNew(
            notificationInfo.first,
            notificationInfo.second,
            null,
            CALL_USE_APP_NOTIFICATION_ID,
            notificationInfo.third,
        )

        val customBigView = createCustomNotificationBigViewNew(
            notificationInfo.first,
            notificationInfo.second,
            null,
            CALL_USE_APP_NOTIFICATION_ID,
            notificationInfo.third,
        )


        val notification = NotificationCompat.Builder(context, CALL_USE_APP_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setDeleteIntent(createDismissPendingIntent(CALL_USE_APP_NOTIFICATION_ID))
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(CALL_USE_APP_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${CALL_USE_APP_NOTIFICATION_ID}")
        NotificationDecider.recordNotificationShown(context)
        Log.d(TAG, "showCallUseAppNotification: Notification shown at ${System.currentTimeMillis()}")

    }


    fun showCallUseAppNotificationWhenOutApp() {

        if (TemporaryStorage.temporaryTurnOffNotificationOutApp) {
            Log.d(TAG,"Notification out app not shown temporary turn off" )
            return
        }

        if (!FirebaseRemoteConfigUtil.getInstance().isShowNotificationOutApp()) {
            Log.d(TAG,"Notification out app not shown remote config" )
            return
        }
        var lastTimeShowNotification = NotificationDecider.getLastTimeShowNotification(context)
        if (System.currentTimeMillis() - lastTimeShowNotification < FirebaseRemoteConfigUtil.getInstance().getNotificationOutAppIntervalSecond() * 1000L) {
            Log.d(TAG, "Notification out app not shown, last time: $lastTimeShowNotification")
            return
        }

        if (IAPUtils.isPremium()) {
            Log.d(TAG,"Notification out app not shown premium" )
            return
        }

        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("${context.packageName}.notificationID", CALL_USE_APP_NOTIFICATION_OUT_ID)
            putExtra("${context.packageName}.isFromNotification", true)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, CALL_USE_APP_OUT_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setDeleteIntent(createDismissPendingIntent(CALL_USE_APP_NOTIFICATION_OUT_ID))
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.you_have_unfinished_file))
            .setContentText(context.getString(R.string.click_to_see_more))
            .setContentIntent(openAppPendingIntent)
            .setSound(null)
            .setVibrate(null)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(CALL_USE_APP_NOTIFICATION_OUT_ID, notification)
        logEvent("notification_shown_${CALL_USE_APP_NOTIFICATION_OUT_ID}")
        NotificationDecider.recordNotificationShown(context)
        Log.d(TAG, "showCallUseAppNotificationWhenOut: Notification shown at ${System.currentTimeMillis()}")

    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getNextNotificationInfo(): Triple<String, String, String>? {
        val index = PreferencesUtils.getInteger("last_shown_index_noti", 0)

        if (isStoragePermissionGranted()) {
            when (index) {
                0 -> if (FileChecker(context).checkAndNotifyUnfinishedFile()) {
                    PreferencesUtils.putInteger("last_shown_index_noti", 1)
                    return null
                }
                1 -> if (FileChecker(context).checkAndNotifyLatestFile()) {
                    PreferencesUtils.putInteger("last_shown_index_noti", 2)
                    return null
                }
            }
        }

        val (titles, messages, buttons) = if (!isStoragePermissionGranted()) {
            Triple(
                context.resources.getStringArray(R.array.notification_title1),
                context.resources.getStringArray(R.array.notification_message1),
                context.resources.getStringArray(R.array.notification_button1)
            )
        } else {
            Triple(
                context.resources.getStringArray(R.array.notification_title2),
                context.resources.getStringArray(R.array.notification_message2),
                context.resources.getStringArray(R.array.notification_button2)
            )
        }

        val titleToShow = titles[index]
        val messageToShow = if (index == 0) {
            String.format(messages[index], context.getString(R.string.app_name))
        } else {
            messages[index]
        }
        val buttonToShow = buttons[index]

        PreferencesUtils.putInteger("last_shown_index_noti", (index + 1) % messages.size)

        return Triple(titleToShow, messageToShow, buttonToShow)
    }


    fun showCallUseAppUnfinishedReadingNotification(fileName: String? = null, filePath: String? = null) {

        val title = context.resources.getStringArray(R.array.notification_title2)[0]
        val content = String.format(
            context.resources.getStringArray(R.array.notification_message2)[0],
            filePath?.substringAfterLast("/") ?: fileName ?: "File"
        )
        val buttonText = context.resources.getStringArray(R.array.notification_button2)[0]

        val customView = createCustomNotificationViewNew(title, content, filePath, CALL_USE_APP_NOTIFICATION_ID, buttonText)
        val customBigView = createCustomNotificationBigViewNew(title, content, filePath, CALL_USE_APP_NOTIFICATION_ID, buttonText)

        val notification = NotificationCompat.Builder(context, CALL_USE_APP_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setDeleteIntent(createDismissPendingIntent(CALL_USE_APP_NOTIFICATION_ID))
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager)
            .notify(CALL_USE_APP_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${CALL_USE_APP_NOTIFICATION_ID}")
        NotificationDecider.recordNotificationShown(context)
        Log.d(TAG, "showUnfinishedReadingNotification: Notification shown at ${System.currentTimeMillis()}")
    }


    fun showCallUseAppLatestFileNotification(filePath: String) {
        val title = context.resources.getStringArray(R.array.notification_title2)[1]
        val content = context.resources.getStringArray(R.array.notification_message2)[1]
        val buttonText = context.resources.getStringArray(R.array.notification_button2)[1]

        val customView = createCustomNotificationViewNew(title, content, filePath, CALL_USE_APP_NOTIFICATION_ID, buttonText)
        val customBigView = createCustomNotificationBigViewNew(title, content, filePath, CALL_USE_APP_NOTIFICATION_ID, buttonText)

        val notification = NotificationCompat.Builder(context, CALL_USE_APP_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setDeleteIntent(createDismissPendingIntent(CALL_USE_APP_NOTIFICATION_ID))
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager)
            .notify(CALL_USE_APP_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${CALL_USE_APP_NOTIFICATION_ID}")
        NotificationDecider.recordNotificationShown(context)
        Log.d(TAG, "showLatestFileNotification: Notification shown at ${System.currentTimeMillis()}")
    }

    fun showScreenShotNotification(filePath: String? = null) {

        if (IAPUtils.isPremium()) return // No noti for premium users


        val customView = createCustomNotificationViewScreenShot(
            filePath,
            SCREENSHOT_NOTIFICATION_ID,
        )

        val customBigView = createCustomNotificationBigViewScreenShot(
            filePath,
            SCREENSHOT_NOTIFICATION_ID,
        )

        val notification = NotificationCompat.Builder(context, HIGH_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()


        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(SCREENSHOT_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${SCREENSHOT_NOTIFICATION_ID}")
        Log.d(TAG, "showForgottenFileNotification: Notification shown at ${System.currentTimeMillis()}")
    }

    fun showDailyCallOpenAppNotification() {

        val openAppIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // no need isFromNotification because this notification doesn't open any file
            putExtra("${context.packageName}.isFromNotification", true)
            // putExtra("${context.packageName}.notificationID", DAILY_CALL_OPEN_APP_NOTIFICATION_ID)
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HIGH_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.daily_call_open_app_title))
            .setContentText(context.getString(R.string.daily_call_open_app_content))
            .setSmallIcon(R.drawable.icon_notification)
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(DAILY_CALL_OPEN_APP_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${DAILY_CALL_OPEN_APP_NOTIFICATION_ID}")
        Log.d(TAG, "showDailyCallOpenAppNotification: Notification shown at ${System.currentTimeMillis()}")
    }

    fun showForgottenFileNotification(fileName: String, filePath: String? = null) {

        val customView = createCustomNotificationView(
            fileName,
            context.getString(R.string.forgotten_file_content_notification),
            filePath,
            FORGOTTEN_FILE_NOTIFICATION_ID
        )

        val customBigView = createCustomNotificationBigView(
            fileName,
            context.getString(R.string.forgotten_file_content_notification),
            filePath,
            FORGOTTEN_FILE_NOTIFICATION_ID
        )

        val notification = NotificationCompat.Builder(context, HIGH_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()


        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(FORGOTTEN_FILE_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${FORGOTTEN_FILE_NOTIFICATION_ID}")
        Log.d(TAG, "showForgottenFileNotification: Notification shown at ${System.currentTimeMillis()}")
    }

    fun showUpdateAvailableNotification(versionName: String) {
        val customView = createCustomNotificationView(
            context.getString(R.string.update_available_title, versionName),
            context.getString(R.string.update_available_description, versionName),
            null,
            UPDATE_NOTIFICATION_ID,
            context.getString(R.string.update)
        )

        val customBigView = createCustomNotificationBigView(
            context.getString(R.string.update_available_title, versionName),
            context.getString(R.string.update_available_description, versionName),
            null,
            UPDATE_NOTIFICATION_ID,
            context.getString(R.string.update)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATE)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${UPDATE_NOTIFICATION_ID}")
        Log.d(TAG, "showUpdateNotification: Notification shown at ${System.currentTimeMillis()}")
    }

    fun showUpdateDownloadedNotification() {
        Log.d(TAG, "showUpdateDownloadedNotification: Showing update downloaded notification")
        val customView = createCustomNotificationView(
            context.getString(R.string.app_update_downloaded),
            context.getString(R.string.app_update_install),
            null,
            UPDATE_NOTIFICATION_ID,
            context.getString(R.string.install)
        )

        val customBigView = createCustomNotificationBigView(
            context.getString(R.string.app_update_downloaded),
            context.getString(R.string.app_update_install),
            null,
            UPDATE_NOTIFICATION_ID,
            context.getString(R.string.install)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATE)
            .setSmallIcon(R.drawable.icon_notification)
            .setCustomContentView(customView)
            .setCustomBigContentView(customBigView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
        logEvent("notification_shown_${UPDATE_NOTIFICATION_ID}_2")
        Log.d(TAG, "showUpdateNotification: Notification shown at ${System.currentTimeMillis()}")
    }

    fun showFcmNotification(title: String, body: String, priority: Int? = RemoteMessage.PRIORITY_NORMAL) {

        if (IAPUtils.isPremium()) return // No noti for premium users

        val channelId = if (priority == RemoteMessage.PRIORITY_HIGH) "fcm_high" else "fcm_normal"
        val channelName = "FCM Notifications"

        val importance = if (priority == RemoteMessage.PRIORITY_HIGH) {
            AndroidNotificationManager.IMPORTANCE_HIGH
        } else {
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        }

        // Create NotificationChannel for Android 8+
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "FCM notifications"
            setSound(
                if (priority == RemoteMessage.PRIORITY_HIGH)
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else null,
                null
            )
            vibrationPattern = if (priority == RemoteMessage.PRIORITY_HIGH) longArrayOf(0, 500, 250, 500)
            else longArrayOf(0, 200)
        }
        val notificationManager = context.getSystemService(AndroidNotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)

        // Intent to open the app
        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("${context.packageName}.notificationID", FCM_NOTIFICATION_ID)
            putExtra("${context.packageName}.isFromNotification", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (priority == RemoteMessage.PRIORITY_HIGH)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setDeleteIntent(createDismissPendingIntent(FCM_NOTIFICATION_ID))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if (priority == RemoteMessage.PRIORITY_HIGH) {
            notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        notificationManager.notify(FCM_NOTIFICATION_ID, notification.build())
        logEvent("notification_shown_${FCM_NOTIFICATION_ID}")
        NotificationDecider.recordNotificationShown(context)
        PreferencesUtils.putLong("last_time_show_notification", System.currentTimeMillis())
    }

    fun cancelUpdateDownloadedNotification() {
        Log.i(TAG, "cancelUpdateDownloadedNotification: Cancelling update downloaded notification")
        val notificationManager = context.getSystemService(AndroidNotificationManager::class.java)
        notificationManager?.cancel(UPDATE_NOTIFICATION_ID)
    }

    fun createDismissPendingIntent(
        notificationId: Int
    ): PendingIntent {
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = "${context.packageName}.NOTIFICATION_DISMISSED"
            putExtra("${context.packageName}.notificationID", notificationId)
        }

        return PendingIntent.getBroadcast(
            context,
            notificationId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}

