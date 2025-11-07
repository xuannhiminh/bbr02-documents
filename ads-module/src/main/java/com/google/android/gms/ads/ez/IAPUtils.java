package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.observer.MySubject;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IAPUtils {
    private List<SkuDetails> skuDetailsList = new ArrayList<>();
    private Application application;
    private static IAPUtils INSTANCE;
    public static final String KEY_PURCHASE_SUCCESS = "purchase_success";


    private BillingClient billingClient;
    final HashMap<String, SkuDetails> skuDetailsHashMap = new HashMap<>();
    final List<String> arrPurchased = new ArrayList<>();

    public static final String KEY_PREMIUM_1 = "premium_1";
    public static final String KEY_PREMIUM_2 = "premium_2";
    public static final String KEY_PREMIUM_3 = "premium_3";

    public boolean isPremium() {
        if (isSubscriptions(KEY_PREMIUM_1) || isSubscriptions(KEY_PREMIUM_2) || isSubscriptions(KEY_PREMIUM_3) ||
                isSubscriptions("sub_month") || isSubscriptions("sub_trial_3day_year") || isSubscriptions("sub_one_time")) {
            return true;
        }

        return false;
    }


    public static IAPUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IAPUtils();
        }
        return INSTANCE;
    }


    public void init(Application application) {
        this.application = application;
        billingClient = BillingClient.newBuilder(application)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    getAllSubcriptions();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


    }


    // get all subcription
    public void getAllSubcriptions() {
        if (AdUnit.isTEST()) {
            try {
                skuDetailsHashMap.put("one_month", new SkuDetails("{\"productId\":\"one_month\",\"type\":\"subs\",\"title\":\"Monthly (PDF Reader App: Read All PDF)\",\"name\":\"Monthly\",\"price\":\"117.000 ₫\",\"price_amount_micros\":117000000000,\"price_currency_code\":\"VND\",\"subscriptionPeriod\":\"P1M\",\"skuDetailsToken\":\"AEuhp4K8aoNOgluSIa0UBX0r_x6-cxMXALx_X78TwtHcK12OP_7fuxfPbi2I0L0wHAJK\"}"));
                skuDetailsHashMap.put("one_year", new SkuDetails("{\"productId\":\"one_year\",\"type\":\"subs\",\"title\":\"Yearly (PDF Reader App: Read All PDF)\",\"name\":\"Yearly\",\"price\":\"700.000 ₫\",\"price_amount_micros\":700000000000,\"price_currency_code\":\"VND\",\"subscriptionPeriod\":\"P1Y\",\"skuDetailsToken\":\"AEuhp4KVXBCeVSRtIS8vIL_5HdPSIqYp6vUNcuzNTWBtRGpezsUHj4PELiCFR9b9_aNE\"}"));
                skuDetailsHashMap.put("free_trial", new SkuDetails("{\"productId\":\"free_trial\",\"type\":\"subs\",\"title\":\"Yearly (PDF Reader App: Read All PDF)\",\"name\":\"Yearly\",\"price\":\"700.000 ₫\",\"price_amount_micros\":700000000000,\"price_currency_code\":\"VND\",\"subscriptionPeriod\":\"P1Y\",\"skuDetailsToken\":\"AEuhp4KVXBCeVSRtIS8vIL_5HdPSIqYp6vUNcuzNTWBtRGpezsUHj4PELiCFR9b9_aNE\"}"));
            } catch (JSONException e) {
                Log.e("IAPUtils", "onSkuDetailsResponse: ", e);
                e.printStackTrace();
            }
        }

        List<String> skuList = new ArrayList<>();
        skuList.add(KEY_PREMIUM_1);
        skuList.add(KEY_PREMIUM_2);
        skuList.add(KEY_PREMIUM_3);


        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);

        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null && !list.isEmpty()) {
                    Log.v("TAG_INAPP", "skuDetailsList 3");
                    for (SkuDetails skuDetails : list) {
                        skuDetailsHashMap.put(skuDetails.getSku(), skuDetails);
                    }
                }
            }
        });


        try {
            billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    if (list != null) {
                        for (Purchase purchase : list) {
                            arrPurchased.addAll(purchase.getSkus());
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                handlePurchase(purchase);
                            }
                        }
                    }
                }
            });
            billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    if (list != null) {
                        for (Purchase purchase : list) {
                            arrPurchased.addAll(purchase.getSkus());
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                handlePurchase(purchase);
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public SkuDetails getSubcriptionById(String id) {

        return skuDetailsHashMap.get(id);

//        getSku = productId
//        getPrice = price
    }

    public boolean isPurchase(String id) {
        if (billingClient == null || !billingClient.isReady()) {
            return false;
        }


        return false;
    }

    public boolean isSubscriptions(String id) {
        if (billingClient == null || !billingClient.isReady()) {
            return false;
        }
        return arrPurchased.contains(id);
    }


    public void callSubcriptions(Activity activity, String id) {

        SkuDetails skuDetails = getSubcriptionById(id);
        if (skuDetails != null && billingClient.isReady()) {
            BillingFlowParams mBillingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            billingClient.launchBillingFlow(activity, mBillingFlowParams);
        }


    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
            if (purchases == null) {
                return;
            }

            logResponse(billingResult.getResponseCode());

            for (Purchase purchase : purchases) {
                // mua hang thanh cong
                handlePurchase(purchase);
                MySubject.getInstance().notifyChange(KEY_PURCHASE_SUCCESS);

            }

        }
    };

    public void handlePurchase(Purchase purchase) {
        handleConsumableProduct(purchase);
        handleNonConsumableProduct(purchase);
    }

    public void handleConsumableProduct(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == OK) {
                // Handle the success of the consume operation.
            }
        });
    }

    public void handleNonConsumableProduct(Purchase purchase) {
        if (purchase.getPurchaseState() == purchase.getPurchaseState()) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    //Handle acknowledge result
                });
            }
        }
    }


    private void logResponse(int responseCode) {
        switch (responseCode) {
            case SERVICE_TIMEOUT:
                break;
            case FEATURE_NOT_SUPPORTED:
                break;
            case SERVICE_DISCONNECTED:
                break;
            case OK:
                break;
            case USER_CANCELED:
                break;
            case SERVICE_UNAVAILABLE:
                break;
            case BILLING_UNAVAILABLE:
                break;
            case ITEM_UNAVAILABLE:
                break;
            case DEVELOPER_ERROR:
                break;
            case ERROR:
                break;
            case ITEM_ALREADY_OWNED:
                break;
            case ITEM_NOT_OWNED:
                break;
        }
    }

    final int SERVICE_TIMEOUT = -3;
    final int FEATURE_NOT_SUPPORTED = -2;
    final int SERVICE_DISCONNECTED = -1;
    final int OK = 0;
    final int USER_CANCELED = 1;
    final int SERVICE_UNAVAILABLE = 2;
    final int BILLING_UNAVAILABLE = 3;
    final int ITEM_UNAVAILABLE = 4;
    final int DEVELOPER_ERROR = 5;
    final int ERROR = 6;
    final int ITEM_ALREADY_OWNED = 7;
    final int ITEM_NOT_OWNED = 8;
}