package com.brian.base_iap.utils

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.brian.base_iap.iapLib.BillingProcessor
import com.brian.base_iap.iapLib.PurchaseInfo
import com.brian.base_iap.iapLib.SkuDetails
import java.util.concurrent.CopyOnWriteArraySet


object IAPUtils {
    const val TAG = "IAPUtils"
    private val listeners = CopyOnWriteArraySet<BillingProcessor.IBillingHandler>()

    /** Internal handler that dispatches to *all* registered listeners. */
    private val internalHandler = object : BillingProcessor.IBillingHandler {
        override fun onBillingInitialized() {
            Log.i(TAG, "Billing initialized successfully")
            billingInitialized = true
            // updatePremiumState()
            object : CountDownTimer(0, 0){ // add to countdown 0s to avoid UI thread issues
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    val tempListener = ArrayList(listeners) // create a copy to avoid concurrent modification
                    for (listener in tempListener) {
                        listener.onBillingInitialized()
                    }
                }
            }.start()
        }
        override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
            Log.i(TAG, "Product purchased: $productId, Details: $details")
            object : CountDownTimer(0, 0){
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    val tempListener = ArrayList(listeners) // create a copy to avoid concurrent modification
                    for (listener in tempListener) {
                        listener.onProductPurchased(productId, details)
                    }
                }
            }.start()
        }
        override fun onBillingError(errorCode: Int, error: Throwable?) {
            Log.e(TAG, "Billing error code: $errorCode, Error: ${error?.message}")
            object : CountDownTimer(0, 0){
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    val tempListener = ArrayList(listeners) // create a copy to avoid concurrent modification
                    for (listener in tempListener) {
                        listener.onBillingError(errorCode, error)
                    }
                }
            }.start()
        }
        override fun onPurchaseHistoryRestored() {
            Log.i(TAG, "Purchase history restored")
            object : CountDownTimer(0, 0){
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    val tempListener = ArrayList(listeners) // create a copy to avoid concurrent modification
                    for( listener in tempListener) {
                        listener.onPurchaseHistoryRestored()
                    }
                }
            }.start()
        }
    }

    val KEY_PREMIUM = "release_premium_access"
    val KEY_PREMIUM_YEARLY_PLAN = "release-yearly-plan"
    val KEY_PREMIUM_MONTHLY_PLAN = "release-monthly-plan"

    private var bp: BillingProcessor? = null

    private var billingInitialized = false

    fun init(context: Context, licenseKey: String) {
        if (bp != null) return  // already initialized

        bp = BillingProcessor.newBillingProcessor(context, licenseKey, internalHandler)
        bp?.initialize()
    }

    fun initAndRegister(context: Context, licenseKey: String, handler: BillingProcessor.IBillingHandler) {
        registerListener(handler)
        init(context, licenseKey)
    }

    fun registerListener(handler: BillingProcessor.IBillingHandler) {
        if(!listeners.contains(handler)) {
            listeners.add(handler)
        }
        // if billing was already up, immediately notify them
        if (billingInitialized) {
            handler.onBillingInitialized()
            // also you might want to restore their view of purchases:
//            handler.onPurchaseHistoryRestored()
        }
    }

    fun unregisterListener(handler: BillingProcessor.IBillingHandler) {
        listeners.remove(handler)
    }

    fun callSubscription(activity: Activity, sku: String, baseIdPlan: String) {
        if (billingInitialized) {
            val isSubsUpdateSupported: Boolean = bp?.isSubscriptionUpdateSupported() == true
            Log.d(TAG, "isSubscriptionUpdateSupported: $isSubsUpdateSupported")
            if (isSubsUpdateSupported) {
                bp?.subscribeV7(activity, sku, baseIdPlan)
            }
        }
    }

    fun isPremium(): Boolean {
//        if (BuildConfig.DEBUG) return false; // for debug builds, always return true
        val isPremium  = bp?.isSubscribed(KEY_PREMIUM)  == true
        Log.i(TAG, "isPremium: $isPremium")
        return isPremium;
    }

    fun loadOwnedPurchasesFromGoogleAsync(result : (Boolean) -> Unit) {
        bp?.loadOwnedPurchasesFromGoogleAsync(object : BillingProcessor.IPurchasesResponseListener {
            override fun onPurchasesSuccess() {
                Log.i(TAG, "Owned purchases loaded successfully")
           //     updatePremiumState()
                result.invoke(true)
            }

            override fun onPurchasesError() {
                Log.e(TAG, "Failed to load owned purchases")
                // Handle error if needed
                result.invoke(false)
            }
        })
    }

    fun getPurchaseListingDetails(productId: String, skuDetails: (List<SkuDetails?>?) -> Unit) {
        if (billingInitialized) {
            bp?.getPurchaseListingDetailsAsync(productId, object : BillingProcessor.ISkuDetailsResponseListener {
                override fun onSkuDetailsResponse(products: List<SkuDetails?>?) {
                    Log.d(TAG, "Purchase listing details for ${productId}: $skuDetails")
                    // Assuming one SkuDetails object per product
                    skuDetails.invoke(products)
                }

                override fun onSkuDetailsError(error: String?) {
                    Log.e(TAG, "Error getting purchase listing details: $error")
                    skuDetails.invoke(null)
                }

            })
        } else {
            Log.w(TAG, "BillingProcessor is not initialized")
        }
    }

    fun isSubscribed(productId: String): Boolean {
      return bp?.isSubscribed(productId) == true
    }

    // Function to get listing details for a single subscription
    fun getSubscriptionListingDetails(subscriptionId: String, skuDetails: (List<ProductDetails?>?) -> Unit) {
        if (billingInitialized) {
            bp?.getSubscriptionListingDetailsAsyncV7(subscriptionId, object : BillingProcessor.ISkuDetailsResponseListenerV7 {
                override fun onSkuDetailsResponse(products: List<ProductDetails?>?) {
                    Log.d(TAG, "Subscription listing details for ${subscriptionId}: $skuDetails")
                    skuDetails.invoke(products)
                }

                override fun onSkuDetailsError(error: String?) {
                    Log.e(TAG, "Error getting subscription listing details: $error")
                    skuDetails.invoke(null)
                }

            })
        } else {
            Log.w(TAG, "BillingProcessor is not initialized")
        }
    }

    // Function to query listing details for multiple product IDs
    fun getPurchaseListingDetails(productIds: ArrayList<String>) {
        if (billingInitialized) {
            bp?.getPurchaseListingDetailsAsync(productIds, object : BillingProcessor.ISkuDetailsResponseListener {
                override fun onSkuDetailsResponse(products: List<SkuDetails?>?) {
                    Log.i(TAG, "Purchase listing details for products: \$skuDetails")
                }

                override fun onSkuDetailsError(error: String?) {
                    Log.e(TAG, "Error getting purchase listing details: $error")
                }

            })
        } else {
            Log.w(TAG, "BillingProcessor is not initialized")
        }
    }

    // Function to query listing details for multiple subscription IDs
    fun getSubscriptionListingDetails(subscriptionIds: ArrayList<String>) {
        if (billingInitialized) {
            bp?.getPurchaseListingDetailsAsync(subscriptionIds, object : BillingProcessor.ISkuDetailsResponseListener {
                override fun onSkuDetailsResponse(products: List<SkuDetails?>?) {
                    Log.i(TAG, "Subscription listing details for subscriptions: \$skuDetails")
                }

                override fun onSkuDetailsError(error: String?) {
                    Log.e(TAG, "Error getting subscription listing details: $error")
                }

            })
        } else {
            Log.w(TAG, "BillingProcessor is not initialized")
        }
    }

    // Function to get purchase info details for a product
    fun getPurchaseInfoDetails(productId: String): PurchaseInfo? {
        return bp?.getPurchaseInfo(productId)
    }

    // Function to get purchase info details for a subscription
    fun getSubscriptionPurchaseInfoDetails(subscriptionId: String): PurchaseInfo? {
        return bp?.getSubscriptionPurchaseInfo(subscriptionId)
    }

    // Additional functions such as handle canceled subscriptions can be implemented by checking the autoRenewing flag
    fun isSubscriptionCanceled(subscriptionId: String): Boolean {
        val purchaseInfo = getSubscriptionPurchaseInfoDetails(subscriptionId)
        if (purchaseInfo != null) {
            return purchaseInfo.purchaseData.autoRenewing.not()
        }
        Log.w(TAG, "No purchase info for subscriptionId: \$subscriptionId")
        return false
    }

    fun destroy() {
        bp?.release()
        bp = null
        billingInitialized = false
        listeners.clear()
        Log.i(TAG, "IAPUtils destroyed")
    }

}
