package com.brian.base_iap.utils

import android.content.Context
import android.icu.util.TimeZone
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object FCMTopicHandler {
    var isDebug = false
    private const val TAG = "FCMTopicHandler"

    fun resetFCMTopic(context: Context) {
        GlobalScope.launch {
            Log.d(TAG, "resetFCMTopic: called")
            val newTopic = generateTopic(context)
            val currentTopic = PreferencesHelper.getString(PreferencesHelper.GLOBAL_FIREBASE_TOPIC, null)

            if (!currentTopic.isNullOrEmpty() && currentTopic == newTopic) {
                FirebaseMessaging.getInstance().subscribeToTopic(newTopic)
                Log.d(TAG, "resetFCMTopic: topic is the same, no need to reset")
                Log.d(TAG, "resetFCMTopic: subscribed to $newTopic")
                return@launch
            } else {
                if (!currentTopic.isNullOrEmpty()) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(currentTopic).addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentTopic)
                            Log.d(TAG, "resetFCMTopic: failed to unsubscribe from $currentTopic, retrying")
                        }
                    }
                }

                FirebaseMessaging.getInstance().subscribeToTopic(newTopic).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        PreferencesHelper.putString(PreferencesHelper.GLOBAL_FIREBASE_TOPIC, newTopic)
                        Log.d(TAG, "resetFCMTopic: subscribed to $newTopic")
                    } else {
                        FirebaseMessaging.getInstance().subscribeToTopic(newTopic).addOnCompleteListener {
                            if (it.isSuccessful) {
                                PreferencesHelper.putString(PreferencesHelper.GLOBAL_FIREBASE_TOPIC, newTopic)
                                Log.d(TAG, "resetFCMTopic: subscribed to $newTopic on retry")
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun generateTopic(context: Context): String {
        val premium = if (IAPUtils.isPremium()) "prem" else "free"
        val isNotVn = CountryDetector.checkIfNotVN(context)

        val offsetHours = (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings) / (1000 * 60 * 60)

        // Lấy version app từ context
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode

        val lastEngageMillis = PreferencesHelper.getLong(PreferencesHelper.KEY_LAST_ENGAGE, -1L)
        Log.d(TAG, "generateTopic: lastEngageMillis = $lastEngageMillis")
        val daysSinceEngage = if (lastEngageMillis == -1L) 0L else {
            val zone = ZoneId.systemDefault()
            val lastDate = Instant.ofEpochMilli(lastEngageMillis).atZone(zone).toLocalDate()
            val nowDate = Instant.now().atZone(zone).toLocalDate()
            ChronoUnit.DAYS.between(lastDate, nowDate)
        }

        val engageBucket = when {
            daysSinceEngage == 0L -> "d0"
            daysSinceEngage < 3L -> "d1"
            daysSinceEngage < 7L -> "d3"
            else -> "d7"
        }

        val firstOpen = PreferencesHelper.getLong(PreferencesHelper.KEY_FIRST_OPEN, 0L)
        Log.d(TAG, "generateTopic: firstOpen = $firstOpen")
        val daysSinceFirstOpen = if (firstOpen == 0L) 0L else {
            val zone = ZoneId.systemDefault()
            val lastDate = Instant.ofEpochMilli(firstOpen).atZone(zone).toLocalDate()
            val nowDate = Instant.now().atZone(zone).toLocalDate()
            ChronoUnit.DAYS.between(lastDate, nowDate)
        }

        val engageFo = if (daysSinceFirstOpen == 0L) "true" else "false"

        val topic = "${premium}_vn${!isNotVn}_v${versionName}_fo${engageFo}_db${isDebug}"
        Log.d(TAG, "generateTopic: $topic")
        return topic
    }
}
