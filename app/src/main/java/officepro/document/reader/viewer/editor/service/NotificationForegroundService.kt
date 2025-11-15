package office.pdf.document.reader.viewer.editor.service

import android.annotation.SuppressLint
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.brian.base_iap.iapLib.BillingProcessor
import com.brian.base_iap.iapLib.PurchaseInfo
import com.brian.base_iap.utils.IAPUtils
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import office.pdf.document.reader.viewer.editor.notification.FileObserverWrapper
import office.pdf.document.reader.viewer.editor.notification.NotificationManager
import office.pdf.document.reader.viewer.editor.receiver.HomeButtonReceiver
import office.pdf.document.reader.viewer.editor.receiver.UnlockReceiver
import com.brian.base_iap.utils.AppUtils
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import office.pdf.document.reader.viewer.editor.notification.NotificationManager.Companion.CHANNEL_DESCRIPTION_FOREGROUND
import office.pdf.document.reader.viewer.editor.notification.NotificationManager.Companion.CHANNEL_ID_FOREGROUND
import office.pdf.document.reader.viewer.editor.notification.NotificationManager.Companion.CHANNEL_NAME_SERVICE
import com.brian.base_iap.utils.FCMTopicHandler

class NotificationForegroundService: Service() {

    companion object {
        val TAG = "NotificationForegroundService"
    }
    private lateinit var appUpdateManager : AppUpdateManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var fileDownloadObserver: FileObserverWrapper
    private lateinit var fileDocumentObserver: FileObserverWrapper
    private val unlockReceiver =  UnlockReceiver()
    private val homeButtonReceiver =  HomeButtonReceiver()

    private fun showForegroundNotification() {
        // Create a notification for the foreground service
        val notification = notificationManager.createForegroundNotification()

        // Start the service in the foreground with the notification
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    startForeground(NotificationManager.WIDGETS_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                }catch (ex: Exception) {
                    Log.e(TAG, "Foreground service start not allowed: ${ex.message}")
                    // Handle the case where starting a foreground service is not allowed
                    // This might happen if the app is in the background or if the device policy prevents it
                    stopSelf()
                    return
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        startForeground(NotificationManager.WIDGETS_NOTIFICATION_ID, notification)
                    }catch (ex: Exception) {
                        Log.e(TAG, "Foreground service start not allowed: ${ex.message}")
                        // Handle the case where starting a foreground service is not allowed
                        // This might happen if the app is in the background or if the device policy prevents it
                        stopSelf()
                        return
                    }
                } else {
                    startForeground(NotificationManager.WIDGETS_NOTIFICATION_ID, notification)
                }
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}")
            stopSelf()
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null as this service is not meant to be bound
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the service start command
        Log.d(TAG, "Service started")
        createForegroundNotificationChannel()
        showForegroundNotification()

        IAPUtils.initAndRegister(this, AppUtils.PUBLIC_LICENSE_KEY, iapHandler)


        when (intent?.action) {
            "${packageName}.WAIT_UPDATE_DOWNLOADED" -> {
                Log.d(TAG, "Waiting for update to be downloaded")
                startListeningUpdate()
            }
            "${packageName}.STOP_WAIT_UPDATE_DOWNLOADED" -> {
                Log.d(TAG, "Stop Waiting for update to be downloaded")
                stopListeningUpdate()
            }
        }

        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        unregisterReceiver(unlockReceiver)
        unregisterReceiver(homeButtonReceiver)
        fileDownloadObserver.stopWatching()
        fileDocumentObserver.stopWatching()
        observers.forEach { it.stopWatching() }
        observers.clear()
        contentResolver.unregisterContentObserver(screenShotObserver)
        IAPUtils.unregisterListener(iapHandler)
//        IAPUtils.destroy()
//        wakeLock?.let {
//            if (it.isHeld) {
//                it.release()
//            }
//        }
    }
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        // Handle re-binding to the service
    }
    override fun onUnbind(intent: Intent?): Boolean {
        // Handle unbinding from the service
        return super.onUnbind(intent)
    }
    private val downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    private val documentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath

    private val directoriesToMonitor = listOf(
        "${downloadPath}/Zalo",
        "${downloadPath}/${AppUtils.FOLDER_EXTERNAL_IN_DOWNLOADS}",
        "${downloadPath}/Telegram",
        "${downloadPath}/WhatsApp",
        "${downloadPath}/Messenger",
        "${downloadPath}/Viber",
        "${downloadPath}/Line",
        "${downloadPath}/Discord",
        "${downloadPath}/Gapo",
        "${downloadPath}/MicrosoftTeams/",
        "${downloadPath}/Tencent/",
        "${documentPath}/Zalo",
        "${documentPath}/Telegram",
        "${documentPath}/WhatsApp",
        "${documentPath}/Messenger",
        "${documentPath}/Viber",
        "${documentPath}/Line",
        "${documentPath}/Discord",
        "${documentPath}/Gapo",
        "${documentPath}/MicrosoftTeams/",
        "${documentPath}/Tencent/"
    )
    private val observers = mutableListOf<FileObserverWrapper>()

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        notificationManager = NotificationManager(this)

        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(homeButtonReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(homeButtonReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }
        } catch (e: SecurityException) {
            Log.w("HomeReceiver", "RegisterReceiver failed: " + e.message)
        }
        startObservers()
        registerScreenCaptureDetection()

//        wakeLock =
//            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
//                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
//                    acquire()
//                }
//            }
    }
    private val iapHandler = object : BillingProcessor.IBillingHandler {
        override fun onProductPurchased(productId: String, details: PurchaseInfo?) { /*--*/ }
        override fun onPurchaseHistoryRestored() { /*--*/ }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
        }

        override fun onBillingInitialized() {
            IAPUtils.loadOwnedPurchasesFromGoogleAsync { success ->
                Log.i(TAG, "loadOwnedPurchasesFromGoogleAsync: $success")
                FCMTopicHandler.resetFCMTopic(this@NotificationForegroundService)
                if (IAPUtils.isPremium()) {
                    Log.d(TAG, "IAP initialized and user is premium")
                    if (FirebaseRemoteConfigUtil.getInstance().isTurnOffNotiServiceIfPremium()) {
                        Log.d(
                            TAG,
                            "User is premium and turn off noti service if premium is true, so stop service"
                        )
                        stopSelf()
                    }
                } else {
                    Log.d(TAG, "IAP initialized and user is not premium")
                }
            }
        }
    }
    private lateinit var installStateUpdatedListener : InstallStateUpdatedListener


    private fun createForegroundNotificationChannel() {
        // Create a foreground service notification channel
        val foregroundChannel = NotificationChannel(
            CHANNEL_ID_FOREGROUND,
            CHANNEL_NAME_SERVICE,
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION_FOREGROUND
            setSound(null, null) // Disable sound for foreground service notifications
        }
        (getSystemService(NOTIFICATION_SERVICE) as AndroidNotificationManager).createNotificationChannel(
            foregroundChannel
        )
    }


    private fun startListeningUpdate() {
        Log.d(TAG, "Start listening update")
        appUpdateManager = AppUpdateManagerFactory.create(this)
        try {
            installStateUpdatedListener = InstallStateUpdatedListener { result ->
                Log.d(TAG, "Install state updated: ${result.installStatus()}")
                if (result.installStatus() == InstallStatus.DOWNLOADED) {
                    notificationManager.showUpdateDownloadedNotification()
                    appUpdateManager.unregisterListener(installStateUpdatedListener)
                }
            }
            appUpdateManager.registerListener(installStateUpdatedListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering update listener: ${e.message}")
        }
    }

    private fun stopListeningUpdate() {
        Log.d(TAG, "Stop listening update")
        if (::appUpdateManager.isInitialized) {
            try {
                if (::installStateUpdatedListener.isInitialized)
                    appUpdateManager.unregisterListener(installStateUpdatedListener)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering update listener: ${e.message}")
            }
        }
    }

    private fun startObservers() {
        fileDownloadObserver = FileObserverWrapper(downloadPath, notificationManager)
        fileDownloadObserver.startWatching()


        directoriesToMonitor.forEach { directory ->
            val fileObserver = FileObserverWrapper(directory, notificationManager)
            fileObserver.startWatching()
            observers.add(fileObserver)
        }


        fileDocumentObserver = FileObserverWrapper(documentPath, notificationManager)
        fileDocumentObserver.startWatching()
    }

    var getted = false;
    private var screenShotObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            uri?.let {
                // Query the media store for the new image
                try {
                    val cursor = contentResolver.query(
                        it,
                        arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA),
                        null,
                        null,
                        null
                    )
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            if (!getted && !path.contains(".pending") && path.contains("screenshots", ignoreCase = true) && (path.substringAfterLast(".").equals("png", ignoreCase = true)
                                        || path.substringAfterLast(".").equals("jpg", ignoreCase = true)
                                        || path.substringAfterLast(".").equals("jpeg", ignoreCase = true))) {
                                // Screenshot detected
                                getted = true;
                                Log.d(TAG, "Screenshot detected: $path")
                                notificationManager.showScreenShotNotification(path)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    getted = false
                                }, 1000) // Reset after 1 second
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error querying media store: ${e.message}")
                }
            }
        }
    }

    private fun registerScreenCaptureDetection() {
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            screenShotObserver
        )
    }


//    private var wakeLock: PowerManager.WakeLock? = null


}