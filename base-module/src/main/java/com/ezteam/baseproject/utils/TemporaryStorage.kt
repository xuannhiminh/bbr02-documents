package com.ezteam.baseproject.utils

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd

object TemporaryStorage {
    /**
     * Check if in this time user has seen the dialog to request to set default reader or not
     */
    @JvmStatic var isObtainConsent: Boolean = false
    @JvmStatic @Volatile var isSavingFileNotNoti = false
    @JvmStatic var isShowedDefaultReaderRequestDialogInThisSession: Boolean = false
    @JvmStatic var isShowedDefaultReaderRequestPdfDialogInThisSession: Boolean = false
    @JvmStatic var isShowedDefaultReaderRequestDocDialogInThisSession: Boolean = false
    @JvmStatic var isShowedDefaultReaderRequestExcelDialogInThisSession: Boolean = false
    @JvmStatic var isShowedDefaultReaderRequestPptDialogInThisSession: Boolean = false
    @JvmStatic var isShowSatisfiedDialogInThisSession: Boolean = false
    @JvmStatic var isShowedAddToHoneDialog: Boolean = false
    @JvmStatic var timeEnterPdfDetail = 0;
    @JvmStatic var shouldLoadAdsLanguageScreen = true;

    @JvmStatic var interAdPreloaded : InterstitialAd? = null
    @JvmStatic var keepScreenOn: Boolean = false
    @JvmStatic var enableNotification: Boolean = true
    @JvmStatic var isShowedReloadGuideInThisSession: Boolean = false
    @JvmStatic var isNightMode: Boolean = false
    @JvmStatic var isLoadAds: Boolean = false
    @JvmStatic var isRateFullStar: Boolean = false
    @JvmStatic var allowShowAdsAt: Long = 0
    @JvmStatic var nativeAdPreload : NativeAd? = null
    @JvmStatic var isLoadingNativeAdsLanguage = false
    @JvmStatic var callbackNativeAdsLanguage: (NativeAd?) -> Unit = { _ -> }
    @JvmStatic
    fun reset() {
        isShowedDefaultReaderRequestDialogInThisSession = false
        isShowedDefaultReaderRequestPdfDialogInThisSession = false
        isShowedDefaultReaderRequestDocDialogInThisSession = false
        isShowedDefaultReaderRequestExcelDialogInThisSession = false
        isShowedDefaultReaderRequestPptDialogInThisSession = false
        isShowSatisfiedDialogInThisSession = false
        isShowedAddToHoneDialog = false
        timeEnterPdfDetail = 0
        allowShowAdsAt = 0
    }
}