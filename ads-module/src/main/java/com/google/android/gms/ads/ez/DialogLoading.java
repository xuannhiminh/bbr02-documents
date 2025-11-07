package com.google.android.gms.ads.ez;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class DialogLoading extends Dialog {

    public DialogLoading(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_load_ads);


        Window window = getWindow();
        window.setGravity(17);
        window.setLayout(-1, -1);
        setCancelable(true);
        window.setBackgroundDrawableResource(R.drawable.bg_dialog_loading);

    }

    @Override
    public void show() {
        if (Utils.checkNetworkEnable(getContext())) {
            super.show();
        }

    }



}