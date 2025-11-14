package com.brian.base_iap.utils

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
         const val FOLDER_EXTERNAL_IN_DOWNLOADS = "AllDocumentsGB"
         const val PUBLIC_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkIORS/nQkfA915WYTCxDGsOB/XT/72ZPLYl+3CJJ+PwrcR6MiBGDbiM/HdEFyAIKoxrANjiXoiUjkmdOFgEtNoYBWeMMZ+EMv+eGQFicbZHm5x543o+ZN7sros4/BBeMmdxbcofVDLPm77urSPrElhHBFmAH8TYLnS/3Vd4Vju/j1yNspyYzWeO2CBybPjq4GpJltUT00wA5OiBRMXyfmOhwfvkQCB4SH65+kf0xSzpm95J88Sgo+32w9L7parajO9mG/z68TC2FhRX4bO9P52tdp+KqD2M/DgnwFc5EcvXm5SGNCqKxzOrmrlN2jN5aGxT/dviucnfzBrS9EmQteQIDAQAB"
     }
 }