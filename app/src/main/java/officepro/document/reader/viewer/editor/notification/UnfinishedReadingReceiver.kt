package officepro.document.reader.viewer.editor.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log

class UnfinishedReadingReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_UNFINISHED_READING = "com.readingpdf.UNFINISHED_READING"
        const val EXTRA_FILE_NAME = "fileName"
        const val EXTRA_FILE_PATH = "filePath"
        const val EXTRA_CURRENT_PAGE = "currentPage"
        const val EXTRA_TOTAL_PAGES = "totalPages"

        fun register(context: Context, receiver: UnfinishedReadingReceiver) {
            val filter = IntentFilter(ACTION_UNFINISHED_READING)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            }
            else {
                context.registerReceiver(receiver, filter)
            }
        }

        fun unregister(context: Context, receiver: UnfinishedReadingReceiver) {
            context.unregisterReceiver(receiver)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("UnfinishedReadingReceiver", "=== Bắt đầu xử lý broadcast ===")
        Log.d("UnfinishedReadingReceiver", "Nhận được intent: action=${intent.action}")

        if (intent.action != ACTION_UNFINISHED_READING) {
            Log.e("UnfinishedReadingReceiver", "Action không khớp: ${intent.action}")
            return
        }

        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH) ?: return
        val currentPage = intent.getIntExtra(EXTRA_CURRENT_PAGE, 0)
        val totalPages = intent.getIntExtra(EXTRA_TOTAL_PAGES, 0)

        Log.d("UnfinishedReadingReceiver", "Thông tin file nhận được:")
        Log.d("UnfinishedReadingReceiver", "- Tên file: $fileName")
        Log.d("UnfinishedReadingReceiver", "- Đường dẫn: $filePath")
        Log.d("UnfinishedReadingReceiver", "- Trang hiện tại: $currentPage")
        Log.d("UnfinishedReadingReceiver", "- Tổng số trang: $totalPages")

        val notificationManager = NotificationManager(context)
        notificationManager.showCallUseAppUnfinishedReadingNotification(fileName, filePath)
        Log.d("UnfinishedReadingReceiver", "Đã hiển thị thông báo thành công")
    }
} 