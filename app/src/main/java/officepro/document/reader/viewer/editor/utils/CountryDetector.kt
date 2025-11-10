package officepro.document.reader.viewer.editor.utils

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import officepro.document.reader.viewer.editor.BuildConfig
import officepro.document.reader.viewer.editor.screen.language.PreferencesHelper
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.io.use
import kotlin.text.isNullOrEmpty
import kotlin.text.uppercase

object CountryDetector {
    const val KEY_IS_NOT_VN = "is_not_vn"

    suspend fun checkIfNotVN(context: Context): Boolean {
        if (BuildConfig.DEBUG) {
            Log.d("CountryDetector", "checkIfNotVN: DEBUG build, returning true")
            PreferencesHelper.putString(KEY_IS_NOT_VN, "true")
            return true
        }
        Log.d("CountryDetector", "checkIfNotVN: called")
        val isNotVN = PreferencesHelper.getString(KEY_IS_NOT_VN, null)

        if (!isNotVN.isNullOrEmpty()) {
            Log.d("CountryDetector", "checkIfNotVN: cached value found: $isNotVN")
            return isNotVN == "true"
        }
        Log.d("CountryDetector", "checkIfNotVN: detecting country")
        val isNotVn = isNotVietnam(context)
        PreferencesHelper.putString(KEY_IS_NOT_VN, if(isNotVn) "true" else "false")
        Log.d("CountryDetector", "checkIfNotVN: detected country!")
        return isNotVn
    }

    suspend fun isNotVietnam(context: Context): Boolean {
        val ipCountry = getCountryFromIP()?.uppercase(Locale.US)
        val simCountry = getSimCountry(context)
        val localeCountry = Locale.getDefault().country.uppercase(Locale.US)
        val tzOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000

        // Step 1: if IP says VN → stop immediately
        if (ipCountry == "VN") return false

        // Step 2: Combine signals to detect strong Vietnam pattern
        val possibleVietnam = when {
            simCountry == "VN" -> true
            localeCountry == "VN" -> true
            tzOffset == 7
                    && (simCountry != "ID" )
                    && (simCountry != "LA")
                    && (simCountry != "KH" )
                    && (simCountry != "TH" )
                           -> true
            else -> false
        }

        return !possibleVietnam
    }

    private suspend fun getCountryFromIP(): String? {
        return try {
            val url = URL("https://ipinfo.io/country")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 1000
            conn.readTimeout = 1000
            conn.requestMethod = "GET"
            conn.inputStream.bufferedReader().use {
                it?.readText()?.trim()
            }
        } catch (e: Exception) {
            Log.e("CountryDetector", "getCountryFromIP: failed to get country from IP", e)
            null
        }
    }

    private fun getSimCountry(context: Context): String? {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.simCountryIso?.uppercase(Locale.US)
        } catch (e: Exception) {
            null
        }
    }
}
