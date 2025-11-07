package officepro.document.reader.viewer.editor.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import java.util.Currency
import java.util.Locale

class AppUtils {

     companion object {
         fun getCurrencySymbol(currencyCode: String, locale: Locale = Locale.getDefault()): String {
             return Currency.getInstance(currencyCode)
                 .getSymbol(locale)
         }
        fun isWidgetNotAdded(context: Context, widgetClass: Class<*>): Boolean {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, widgetClass))
            return appWidgetIds.isEmpty()
        }
         const val PDF_DETAIL_EZLIB = 0L // 0 mean use EZ lib pdf detail, 1 mean use SoLib PDF detail
         const val FOLDER_EXTERNAL_IN_DOWNLOADS = "AllDocumentsBR"
         const val PUBLIC_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtadIeo+LHCATmZgsCJFKaAk5P0g8YMYFkelgJpBavlK0tSvM3Y+rKOs6wxFGXDmxeDX8Q0rGsSvPmq90Y75KnlB80Bevb9lfWGB+U6L+vLVp2tTREhJGTzq8HCwA3IBicqgUzIYJ5rSC7BDQI9br2CNSBbiZtrVH3CkJ+//kSw45Dc3z9JgXk4eEfjzigpIHHYiVw0koWKsciA4Ns7TOyX061xKsuIS2/1T8zRQuUXv85d1917pEA0daWKDkPbl5ucZhWDdY4FckSDRKSx58QmiHSNhKr6Bu8o9BSvnYDUqyu2DyS0qB7KNHRkigMu2q9cXZaxB7w4gmMyBWWshKJwIDAQAB"
     }
 }