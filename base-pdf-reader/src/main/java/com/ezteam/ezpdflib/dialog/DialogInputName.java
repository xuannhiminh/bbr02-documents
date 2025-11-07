package com.ezteam.ezpdflib.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ezteam.ezpdflib.R;
import com.ezteam.ezpdflib.databinding.LibDialogInputNameBinding;

import java.util.HashMap;

public class DialogInputName extends BaseDialog<LibDialogInputNameBinding, DialogInputName.ExtendBuilder> {

    public static final String DATA_INPUT = "data input";

    public DialogInputName(@NonNull Context context, ExtendBuilder builder) {
        super(context, builder);
    }

    @Override
    protected LibDialogInputNameBinding getViewBinding() {
        return LibDialogInputNameBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected void initView() {
        super.initView();
        getWindow().getAttributes().softInputMode =
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        if (!TextUtils.isEmpty(builder.label)) {
            binding.llInputText.setHint(builder.label);
        }
        if (!TextUtils.isEmpty(builder.inputValue)) {
            binding.edtInput.setText(builder.inputValue);
            binding.edtInput.selectAll();
        }
    }

    @Override
    protected void initListener() {

    }

    protected TextView getTitle() {
        return binding.tvTitle;
    }

    @Override
    protected TextView getPositiveButton() {
        return binding.tvOk;
    }

    @Override
    protected TextView getNegativeButton() {
        return binding.tvCancel;
    }

    @Override
    protected void handleClickPositiveButton(HashMap<String, Object> datas) {
        String data = binding.edtInput.getText().toString();
        if (TextUtils.isEmpty(data) && !TextUtils.isEmpty(builder.label)) {
            binding.llInputText.setError(getContext().getString(R.string.not_empty, builder.label));
        } else {
            datas.put(DATA_INPUT, binding.edtInput.getText().toString());
            super.handleClickPositiveButton(datas);
        }
    }

    public static class ExtendBuilder extends BuilderDialog {

        private String label;
        private String inputValue;

        public ExtendBuilder(Context context) {
            super(context);
        }

        @Override
        public BaseDialog build() {
            return new DialogInputName(context, this);
        }

        public ExtendBuilder setLabel(String label) {
            this.label = label;
            return this;
        }

        public ExtendBuilder setInputValue(String inputValue) {
            this.inputValue = inputValue;
            return this;
        }
    }
}
