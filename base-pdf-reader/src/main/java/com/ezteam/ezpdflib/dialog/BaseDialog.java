package com.ezteam.ezpdflib.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;


import java.util.HashMap;

public abstract class BaseDialog<BD extends ViewBinding, B extends BuilderDialog> extends Dialog {

    protected B builder;
    protected BD binding;

    public BaseDialog(@NonNull Context context, B builder) {
        super(context);
        this.builder = builder;
    }

    protected abstract BD getViewBinding();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initListener();
    }

    protected void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = getViewBinding();
        setContentView(binding.getRoot());
        Window window = getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        windowParams.dimAmount = 0.7f;
        window.setAttributes(windowParams);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    protected void initData() {
        if (!TextUtils.isEmpty(builder.title) && getTitle() != null) {
            getTitle().setVisibility(View.VISIBLE);
            getTitle().setText(builder.title);
        }

        if (!TextUtils.isEmpty(builder.message) && getMessage() != null) {
            getMessage().setVisibility(View.VISIBLE);
            getMessage().setText(builder.message);
        }

        if (!TextUtils.isEmpty(builder.positiveButton) && getPositiveButton() != null) {
            getPositiveButton().setVisibility(View.VISIBLE);
            getPositiveButton().setText(builder.positiveButton);
            getPositiveButton().setOnClickListener(v -> {
                builder.dismissDialogListener = null;
                handleClickPositiveButton(new HashMap<>());
            });
        }

        if (!TextUtils.isEmpty(builder.negativeButton) && getNegativeButton() != null) {
            getNegativeButton().setVisibility(View.VISIBLE);
            getNegativeButton().setText(builder.negativeButton);
            getNegativeButton().setOnClickListener(v -> {
                builder.dismissDialogListener = null;
                handleClickNegativeButton();
            });
        }

    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (builder.dismissDialogListener != null)
            builder.dismissDialogListener.onDismissDialogListener();
    }

    protected abstract void initListener();

    protected TextView getPositiveButton() {
        return null;
    }

    protected TextView getNegativeButton() {
        return null;
    }

    protected TextView getTitle() {
        return null;
    }

    protected TextView getMessage() {
        return null;
    }

    protected void handleClickNegativeButton() {
        if (builder.negativeButtonListener != null)
            builder.negativeButtonListener.onNegativeButtonListener(this);
    }

    protected void handleClickPositiveButton(HashMap<String, Object> datas) {
        if (builder.positiveButtonListener != null) {
            builder.positiveButtonListener.onPositiveButtonListener(this
                    , datas);
        }
    }

}
