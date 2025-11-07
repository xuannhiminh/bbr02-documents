package officepro.document.reader.viewer.editor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import officepro.document.reader.viewer.editor.notification.NotificationManager

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_USER_PRESENT === intent.action) {
            // Handle the unlock event here
            Log.d("UnlockReceiver", "Device unlocked")
            try {
                NotificationManager(context).showCallUseAppNotification()
            } catch (e: Exception) {
                Log.e("UnlockReceiver", "Error handling unlock event", e)
            }
        }
    }
}
