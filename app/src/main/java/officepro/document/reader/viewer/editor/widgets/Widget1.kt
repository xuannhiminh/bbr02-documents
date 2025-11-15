package office.pdf.document.reader.viewer.editor.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.ezteam.baseproject.utils.DateUtils
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.database.repository.FileModelRepository
import office.pdf.document.reader.viewer.editor.screen.start.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin

class Widget1 : AppWidgetProvider() {
    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_option_1)

        // Handle clicks
        val intent = Intent(context, SplashActivity::class.java).apply {
            Log.d("Widget1", "isFromWidget = ${context.packageName}.isFromWidget")
            putExtra("${context.packageName}.isFromWidget", true)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.midGraphicContainer, pendingIntent)

        setClickAction(context, views, R.id.ivRecent, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivBookmarks, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivPdf, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivWord, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivExcel, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivPpt, SplashActivity::class.java)

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

fun setClickAction(context: Context, views: RemoteViews, viewId: Int, targetActivity: Class<*>) {
    val intent = Intent(context, targetActivity).apply {
        action = System.currentTimeMillis().toString() // Unique action to ensure it's recognized
        putExtra("${context.packageName}.isFromWidget", true)
        putExtra("${context.packageName}.whereToOpen", viewId)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        viewId, // Unique request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(viewId, pendingIntent)
}

class Widget3 : AppWidgetProvider() {
    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_option_3)

        // Handle clicks
        val intent = Intent(context, SplashActivity::class.java).apply {
            Log.d("Widget2", "isFromWidget = ${context.packageName}.isFromWidget")
            putExtra("${context.packageName}.isFromWidget", true)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget3Container, pendingIntent)

        setClickAction(context, views, R.id.searchBar, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivHome, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivRecent, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivBookmarks, SplashActivity::class.java)
        setClickAction(context, views, R.id.ivEdit, SplashActivity::class.java)

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}


class Widget2 : AppWidgetProvider() {
    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        val repository: FileModelRepository = getKoin().get() // Inject repository

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, repository)
        }
    }

    private fun getIconResource(path: String): Int {
        return when {
            path.lowercase().endsWith(".pdf") -> R.drawable.icon_main_pdf
            path.lowercase().endsWith(".ppt") || path.lowercase().endsWith(".pptx") -> R.drawable.icon_main_ppt
            path.lowercase().endsWith(".doc") || path.lowercase().endsWith(".docx") -> R.drawable.icon_main_word
            path.lowercase().endsWith(".xls") || path.lowercase().endsWith(".xlsx") || path.lowercase().endsWith(".xlsm")-> R.drawable.icon_main_excel
            path.lowercase().endsWith(".txt")-> R.drawable.icon_main_txt
            else -> R.drawable.icon_main_pdf
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, repository: FileModelRepository) {
        val views = RemoteViews(context.packageName, R.layout.widget_option_2)

        // Handle clicks
        val intent = Intent(context, SplashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.midGraphicContainer, pendingIntent)
        setClickAction(context, views, R.id.searchBar, SplashActivity::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val files = repository.getLatestFiles()

            if (files.isNotEmpty()) {
                val firstFile = files.getOrNull(0)
                val secondFile = files.getOrNull(1)

                // Update first file
                firstFile?.let {
                    views.setImageViewResource(R.id.file_icon_1, getIconResource(it.path))
                    views.setTextViewText(R.id.file_name_1, it.name)
                    views.setTextViewText(R.id.file_details_1,"${DateUtils.longToDateString(it.date, DateUtils.DATE_FORMAT_7)} - ${it.sizeString}")
                }

                // Update second file
                secondFile?.let {
                    views.setImageViewResource(R.id.file_icon_2, getIconResource(it.path))
                    views.setTextViewText(R.id.file_name_2, it.name)
                    views.setTextViewText(R.id.file_details_2,"${DateUtils.longToDateString(it.date, DateUtils.DATE_FORMAT_7)} - ${it.sizeString}")
                }

                // Apply the update
                withContext(Dispatchers.Main) {
                    setClickAction(context, views, R.id.latest_file_item_1, SplashActivity::class.java)
                    setClickAction(context, views, R.id.latest_file_item_2, SplashActivity::class.java)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}

