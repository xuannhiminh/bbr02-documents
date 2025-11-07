package officepro.document.reader.viewer.editor.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

class NotificationDismissReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "NotificationDismissReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val notificationId = intent.getIntExtra("${context.packageName}.notificationID", -1)
            Log.d(TAG, "Notification dismissed: id=$notificationId at ${System.currentTimeMillis()}")
            FirebaseAnalytics.getInstance(context).logEvent("notification_dismiss_${if (notificationId == -1) "_1" else notificationId}",
                Bundle()
            )
            if (notificationId == NotificationManager.CALL_USE_APP_NOTIFICATION_ID) {
                NotificationDecider.onNotificationDismissed(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing intent: ${e.message}")
            return
        }
    }
}