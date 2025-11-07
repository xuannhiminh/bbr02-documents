package com.google.android.gms.ads.ez.consent;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 */
public final class ConsentUtils {

    private String TAG = "ConsentUtils";
    private static ConsentUtils instance;
    private final ConsentInformation consentInformation;

    /**
     * Private constructor
     */
    private ConsentUtils(Context context) {
        Log.e(TAG, "ConsentUtils: "   );
        this.consentInformation = UserMessagingPlatform.getConsentInformation(context.getApplicationContext());
//        consentInformation.reset();
    }

    public static ConsentUtils getInstance(Context context) {

        if (instance == null) {
            instance = new ConsentUtils(context);
        }

        return instance;
    }


    /**
     * Helper variable to determine if the app can request ads.
     */
    public boolean canRequestAds() {
        return consentInformation.canRequestAds();
    }

    /**
     * Helper variable to determine if the privacy options form is required.
     */
    public boolean isPrivacyOptionsRequired() {
        return consentInformation.getPrivacyOptionsRequirementStatus()
                == PrivacyOptionsRequirementStatus.REQUIRED;
    }

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/present a
     * consent form if necessary.
     */
    public void gatherConsent(
            Activity activity, OnConsentListener listener) {
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                // Check your logcat output for the hashed device ID e.g.
                // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
                // the debug functionality.
//                .addTestDeviceHashedId("F364FD46691C20A961CB9987D6722B85")
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .build();
        // Requesting an update to consent information should be called on every app launch.
        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        Log.e(TAG, "onConsentInfoUpdateSuccess: ");
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                activity,
                                new OnConsentFormDismissedListener() {
                                    @Override
                                    public void onConsentFormDismissed(@Nullable FormError formError) {
                                        Log.e(TAG, "onConsentFormDismissed: " + formError);
                                        // Consent has been gathered.
                                        if (listener != null) {
                                            listener.onComplete(formError);
                                        }
                                    }
                                });
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(@NonNull FormError requestConsentError) {
                        Log.e(TAG, "onConsentInfoUpdateFailure: ");
                        if (listener != null) {
                            listener.onComplete(requestConsentError);
                        }
                    }
                }
        );
    }

    public void showConsent(
            Activity activity, OnConsentListener listener) {
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                // Check your logcat output for the hashed device ID e.g.
                // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
                // the debug functionality.
//                .addTestDeviceHashedId("F364FD46691C20A961CB9987D6722B85")
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .build();
        // Requesting an update to consent information should be called on every app launch.
        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        Log.e(TAG, "onConsentInfoUpdateSuccess: ");
                        UserMessagingPlatform.showPrivacyOptionsForm(
                                activity,
                                new OnConsentFormDismissedListener() {
                                    @Override
                                    public void onConsentFormDismissed(@Nullable FormError formError) {
                                        Log.e(TAG, "onConsentFormDismissed: " + formError);
                                        // Consent has been gathered.
                                        if (listener != null) {
                                            listener.onComplete(formError);
                                        }
                                    }
                                });
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(@NonNull FormError requestConsentError) {
                        Log.e(TAG, "onConsentInfoUpdateFailure: ");
                        if (listener != null) {
                            listener.onComplete(requestConsentError);
                        }
                    }
                }
        );
    }

    /**
     * Helper method to call the UMP SDK method to present the privacy options form.
     */
    public void showPrivacyOptionsForm(
            Activity activity,
            OnConsentFormDismissedListener onConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener);
    }
}
