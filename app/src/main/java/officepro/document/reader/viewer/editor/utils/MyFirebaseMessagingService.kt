package officepro.document.reader.viewer.editor.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.BuildConfig
import officepro.document.reader.viewer.editor.dialog.UpdateDialog
import officepro.document.reader.viewer.editor.notification.NotificationManager
import officepro.document.reader.viewer.editor.service.NotificationForegroundService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "DEBUG DEVICE TOKEN = $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        try {
            startForegroundService(Intent(this.applicationContext, NotificationForegroundService::class.java))
        } catch (e: Exception) {
            Log.e("MyFirebaseMessagingService", "Error starting NotificationForegroundService: ${e.message}")
        }

        try {
            val data = remoteMessage.data
            val hasDataPayload = data.isNotEmpty()

            val title: String? = data["title"] ?: remoteMessage.notification?.title
            val body: String? = data["body"] ?: remoteMessage.notification?.body

            if (hasDataPayload && data.containsKey("versionCode")) {
                Log.i(TAG, "Received data payload with versionCode")
                // Extract update data
                val versionCode = data["versionCode"]?.toIntOrNull()
                val versionName = data["versionName"]?: "New"

                if (versionCode != null && versionCode > BuildConfig.VERSION_CODE) {
                    checkForUpdatesAndShowNotification(versionName)
                }
                return
            }
            Log.i(TAG, "Received data payload without versionCode")

            // Fallback: standard notification with title/body
            if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
                val priority = remoteMessage.notification?.notificationPriority
                val notificationManager = NotificationManager(this)
                notificationManager.showFcmNotification(title ?: "New message", body ?: "", priority)
            }
        } catch (e: Exception) {
            Log.e("MyFirebaseMessagingService", "Error starting NotificationForegroundService: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }


    private fun checkForUpdatesAndShowNotification(versionName : String) {
        AppUpdateManagerFactory.create(this).appUpdateInfo.addOnSuccessListener { info ->
            try {
                Log.i(TAG, "Init View OK to check for updates: ${info?.updateAvailability()}")
                if (info?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    val notificationManager = NotificationManager(this)
                    notificationManager.showUpdateAvailableNotification(versionName)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking update availability: ${e.message}")
            }

        }.addOnFailureListener { e ->
            Log.e(TAG, "Error checking for updates: ${e.message}")
        }
    }

}