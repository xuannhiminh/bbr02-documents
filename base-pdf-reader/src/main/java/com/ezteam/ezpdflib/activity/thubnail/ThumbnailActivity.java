package com.ezteam.ezpdflib.activity.thubnail;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil;
import com.ezteam.baseproject.utils.IAPUtils;
import com.ezteam.baseproject.utils.SystemUtils;
import com.ezteam.ezpdflib.R;
import com.ezteam.ezpdflib.activity.BasePdfViewerActivity;
import com.ezteam.ezpdflib.adapter.ThumbnailAdapter;
import com.ezteam.ezpdflib.databinding.LibActivityThumbnailBinding;
import com.ezteam.ezpdflib.util.Config;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.util.Admob;
//import com.google.android.gms.ads.ez.EzAdControl;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ThumbnailActivity extends BasePdfViewerActivity {

    protected CompositeDisposable disposable = new CompositeDisposable();
    private ThumbnailAdapter ThumbnailAdapter;
    private LibActivityThumbnailBinding binding;
    private HashMap<Integer, Uri> mapUriPage = new HashMap<>();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LibActivityThumbnailBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
//        EzAdControl.getInstance(this).showAds();
        initView2();// change name to 2 to avoid conflict with init view of BasePdfViewerActivity
        initListener2();
        loadNativeNomedia();
    }

    private void initView2() {
        if (getIntent() != null) {
            mapUriPage = (HashMap<Integer, Uri>) getIntent().getSerializableExtra(Config.Constant.DATA_MU_PDF_CORE);
        }
        if (mapUriPage == null)
            return;
        ArrayList<Uri> datas = new ArrayList<>(mapUriPage.values());

        ThumbnailAdapter = new ThumbnailAdapter(this, datas, object -> {
            disposable.clear();
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Config.Constant.DATA_PAGE, object);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
        binding.rcvThumbnail.setAdapter(ThumbnailAdapter);
    }
    private void loadNativeNomedia() {

        if (IAPUtils.INSTANCE.isPremium()) {
            binding.layoutNative.setVisibility(View.GONE);
            return;
        }

        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.setVisibility(View.VISIBLE);

            View loadingView = LayoutInflater.from(this)
                    .inflate(R.layout.ads_native_loading_short, null);
            binding.layoutNative.removeAllViews();
            binding.layoutNative.addView(loadingView);

            NativeCallback callback = new NativeCallback() {
                @Override
                public void onNativeAdLoaded(@Nullable NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);

                    int layoutRes = R.layout.ads_native_bot_no_media_short;
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(ThumbnailActivity.this)
                            .inflate(layoutRes, null);

                    binding.layoutNative.removeAllViews();
                    binding.layoutNative.addView(adView);

                    // Gán dữ liệu quảng cáo vào view
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    binding.layoutNative.setVisibility(View.GONE);
                }
            };

            Admob.getInstance().loadNativeAd(
                    getApplicationContext(),
                    FirebaseRemoteConfigUtil.Companion.getInstance().getAdsConfigValue("native_filedetail"),
                    callback
            );
        } else {
            binding.layoutNative.setVisibility(View.GONE);
        }
    }


    private void initListener2() {
        binding.icBack.setOnClickListener(v -> {
            finish();
        });
    }
}
