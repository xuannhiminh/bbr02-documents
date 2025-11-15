package office.pdf.document.reader.viewer.editor.notification

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.brian.base_iap.utils.IAPUtils
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import java.util.Calendar
import java.util.concurrent.TimeUnit


/**
 * Decides when the app is allowed to show another notification.
 * Keeps all user engagement data locally (SharedPreferences).
 */
object NotificationDecider {

    const val TAG = "NotificationDecider"

    private const val PREF_NAME = "device_notification_prefs"
    const val KEY_LAST_NOTI_TIME = "last_noti_time"
    private const val KEY_TIMEOUT_DELTA = "timeout_delta" // store only adjustment

    // Default fallback values (if RemoteConfig not available yet)
    private const val DEFAULT_TIMEOUT_MINUTES = 180L // 3h
    private const val DEFAULT_STEP_INCREASE = 30L
    private const val DEFAULT_STEP_DECREASE = 30L
    private const val DEFAULT_STEP_ORGANIC = 15L
    private const val DEFAULT_MIN_TIMEOUT = 60L
    private const val DEFAULT_MAX_TIMEOUT = 24 * 60L

    fun getPrefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ===== Remote Config Fetchers =====
    private fun getRemoteConfigBase(): Long {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationFrequencyMinutes()
            .takeIf { it > 0 } ?: DEFAULT_TIMEOUT_MINUTES
    }

    private fun getStepIncrease(): Long {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationStepIncrease()
            .takeIf { it > 0 } ?: DEFAULT_STEP_INCREASE
    }

    private fun getStepDecrease(): Long {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationStepDecrease()
            .takeIf { it > 0 } ?: DEFAULT_STEP_DECREASE
    }

    private fun getStepOrganic(): Long {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationStepOrganic()
            .takeIf { it > 0 } ?: DEFAULT_STEP_ORGANIC
    }

    private fun getMinTimeout(): Long {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationMinTimeout()
            .takeIf { it > 0 } ?: DEFAULT_MIN_TIMEOUT
    }

    private fun getMaxTimeout(): Long {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationMaxTimeout()
            .takeIf { it > 0 } ?: DEFAULT_MAX_TIMEOUT
    }
    private fun getMaxHour(): Int {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationMaxHour()
    }

    private fun getMinHour(): Int {
        return FirebaseRemoteConfigUtil.getInstance()
            .getNotificationMinHour()
    }
    // ===== Public API =====
    fun getCurrentTimeoutMinutes(context: Context): Long {
        val prefs = getPrefs(context)
        val delta = prefs.getLong(KEY_TIMEOUT_DELTA, 0L)
        val base = getRemoteConfigBase()

        return (base + delta).coerceIn(getMinTimeout(), getMaxTimeout())
    }

    fun isInGoodWindow(hour: Int, start: Int, end: Int): Boolean {
        // bad window from badStart through midnight to badEnd
        return if (start <= end) {
            hour in start..end
        } else {
            hour >= start || hour <= end
        }
    }
    
    
    fun canShowNotification(context: Context): Boolean {

        if (IAPUtils.isPremium()) {
            Log.d(TAG, "canShowNotification: premium user, no noti")
            return false // No noti for premium users
        }

        val prefs = getPrefs(context)
        val lastTime = prefs.getLong(KEY_LAST_NOTI_TIME, 0L)

        if (lastTime == 0L) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    return true // Allow noti to ask for permission
                }
            } else {
                // For Android 6-11, if no permission, show noti
                if (context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    return true
                }
            }
        }

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val (minHour, maxHour) = Pair(getMinHour(),getMaxHour())
        Log.d("NotificationDecider", "canShowNotification: current hour $hour, good window $minHour-$maxHour")
        if (!isInGoodWindow(hour,minHour,maxHour)) {
            // non-good window → do not show
            Log.d("NotificationDecider", "canNOTShowNotification: $hour non-good window")
            return false
        }
        val timeout = getCurrentTimeoutMinutes(context)
        val now = System.currentTimeMillis()
        Log.d(TAG, "canShowNotification: last=$lastTime, now=$now, timeout=$timeout minutes")
        return (now - lastTime) >= TimeUnit.MINUTES.toMillis(timeout)


    }

    fun recordNotificationShown(context: Context) {
        Log.d(TAG, "recordNotificationShown at ${System.currentTimeMillis()}")
        getPrefs(context).edit()
            .putLong(KEY_LAST_NOTI_TIME, System.currentTimeMillis())
            .apply()
    }

    /** Call when user clicks your notification */
    fun onNotificationClicked(context: Context) {
        Log.d(TAG, "onNotificationClicked")
        adjustDelta(context, -getStepDecrease())
    }

    /** Call when user go to app from Uninstall*/
    fun onOpenAppFromUninstall(context: Context) {
        Log.d(TAG, "onOpenAppFromUninstall")
        adjustDelta(context, getStepIncrease())
    }



    /** Call when user swipes/dismisses your notification */
    fun onNotificationDismissed(context: Context) {
        Log.d(TAG, "onNotificationDismissed")
        adjustDelta(context, getStepIncrease())
    }

    /** Call when user opens the app directly (not by noti) */
    fun onOrganicAppOpen(context: Context) {
        Log.d(TAG, "onOrganicAppOpen")
        adjustDelta(context, -getStepOrganic())
    }

    // ===== Internal Logic =====
    private fun adjustDelta(context: Context, deltaChange: Long) {
        val prefs = getPrefs(context)
        val currentDelta = prefs.getLong(KEY_TIMEOUT_DELTA, 0L)
        val newDelta = currentDelta + deltaChange

        val base = getRemoteConfigBase()
        val finalTimeout = (base + newDelta).coerceIn(getMinTimeout(), getMaxTimeout())

        // Save only the delta portion
        val finalDelta = finalTimeout - base

        prefs.edit().putLong(KEY_TIMEOUT_DELTA, finalDelta).apply()
    }

    fun getLastTimeShowNotification(context: Context) : Long {
        val prefs = getPrefs(context)
        val lastTime = prefs.getLong(KEY_LAST_NOTI_TIME, 0L)
        Log.d(TAG, "getLastTimeShowNotification: lastTime = $lastTime")
        return lastTime
    }
}