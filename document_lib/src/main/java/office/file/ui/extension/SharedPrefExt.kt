package office.file.ui.extension

import android.content.Context
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE


private const val mySharedPref = "OfficeSubSharedPreferences"
private const val isRemoveAds = "isRemoveAds"
private const val isYearlyTrialPurchased = "isYearlyTrialPurchased"
private const val isYearlyPurchased = "isYearlyPurchased"
private const val isMonthlyPurchased = "isMonthlyPurchased"

private const val hourNotify = "HourNotify"
private const val minuteNotify = "MinuteNotify"
private const val openNotify = "OpenNotify"
private const val sizeListContentNotify = "SizeListContentNotify"
private const val contentTitleNotify = "ContentTitleNotify"
private const val contentBodyNotify = "ContentBodyNotify"

private const val showInterRatio = "ShowInterRatio"
private const val willDrawerOpen = "WillDrawerOpen"



fun Context.getHourNotify(): Int {
    return getSharedPreferences(mySharedPref, MODE_PRIVATE).getInt(hourNotify, 8)
}

fun Context.setHourNotify(ratio: Int){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putInt(hourNotify, ratio)
        .apply()
}

fun Context.getMinuteNotify(): Int {
    return getSharedPreferences(mySharedPref, MODE_PRIVATE).getInt(minuteNotify, 0)
}

fun Context.setMinuteNotify(ratio: Int){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putInt(minuteNotify, ratio)
        .apply()
}

fun Context.isOpenNotify(): Boolean {
    return getSharedPreferences(mySharedPref, MODE_PRIVATE).getBoolean(openNotify, false)
}

fun Context.setOpenNotify(ratio: Boolean){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putBoolean(openNotify, ratio)
        .apply()
}

fun Context.getContentTitleNotify(): String {
    val value = getSharedPreferences(mySharedPref, MODE_PRIVATE).getString(contentTitleNotify, "")
    return value ?: ""
}

fun Context.setContentTitleNotify(ratio: String){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putString(contentTitleNotify, ratio)
        .apply()
}

fun Context.getContentBodyNotify(): String {
    val value = getSharedPreferences(mySharedPref, MODE_PRIVATE).getString(contentBodyNotify, "")
    return value ?: ""
}

fun Context.setContentBodyNotify(ratio: String){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putString(contentBodyNotify, ratio)
        .apply()
}

fun Context.getSizeListContentNotify(): Int {
    return getSharedPreferences(mySharedPref, MODE_PRIVATE).getInt(sizeListContentNotify, 0)
}

fun Context.setSizeListContentNotify(size: Int) {
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putInt(sizeListContentNotify, size)
        .apply()
}

fun Context.setInterAdsRatio(ratio: Int){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putInt(showInterRatio, ratio)
        .apply()
}

fun Context.getInterAdsRatio(): Int{
    return getSharedPreferences(mySharedPref, MODE_PRIVATE).getInt(showInterRatio, 2)
}

fun Context.setDrawerWillOpen(ratio: Boolean){
    getSharedPreferences(mySharedPref, MODE_PRIVATE).edit().putBoolean(willDrawerOpen, ratio)
        .apply()
}
fun Context.willDrawerOpen(): Boolean{
    return getSharedPreferences(mySharedPref, MODE_PRIVATE).getBoolean(willDrawerOpen, true)
}