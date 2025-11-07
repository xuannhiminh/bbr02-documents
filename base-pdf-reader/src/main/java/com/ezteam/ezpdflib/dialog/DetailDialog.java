package com.ezteam.ezpdflib.dialog;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ezstudio.pdftoolmodule.model.FileModel;
import com.ezteam.baseproject.utils.DateUtils;
import com.ezteam.ezpdflib.databinding.LibDialogDetailBinding;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

public class DetailDialog extends BaseDialog<LibDialogDetailBinding, DetailDialog.ExtendBuilder> {

    public DetailDialog(@NonNull Context context, ExtendBuilder builder) {
        super(context, builder);
    }

    @Override
    protected LibDialogDetailBinding getViewBinding() {
        return LibDialogDetailBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected void initListener() {
        binding.icClose.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    protected void initView() {
        super.initView();
        File file = new File(builder.path);
        binding.tvFileName.setText(FilenameUtils.getName(file.getPath()));
        binding.tvPath.setText(file.getPath());
        binding.tvLastModified.setText(DateUtils.getStampByDate(
                new Date(file.lastModified()),
                DateUtils.DATE_FORMAT_8
        ));
        binding.tvSize.setText(getFileLength(file.length()));
    }

    public String getFileLength(double paramDouble) {
        double d1 = paramDouble / 1024.0D;
        double d2 = d1 / 1024.0D;
        double d3 = d2 / 1024.0D;
        if (paramDouble < 1024.0D) {
            return paramDouble + " bytes";
        }
        if (d1 < 1024.0D) {
            return new BigDecimal(d1).setScale(2, 4).toString() + " kb";
        }
        if (d2 < 1024.0D) {
            return new BigDecimal(d2).setScale(2, 4).toString() + " mb";
        }
        return new BigDecimal(d3).setScale(2, 4).toString() + " gb";
    }

    @Override
    protected TextView getTitle() {
        return binding.tvTitle;
    }

    @Override
    protected TextView getMessage() {
        return null;
    }

    @Override
    protected TextView getNegativeButton() {
        return binding.tvNegative;
    }

    @Override
    protected TextView getPositiveButton() {
        return binding.tvPositive;
    }

    public static class ExtendBuilder extends BuilderDialog {
        private String path;
        public ExtendBuilder(Context context) {
            super(context);
        }

        public ExtendBuilder setPathFile(String path) {
            this.path = path;
            return this;
        }

        @Override
        public BaseDialog build() {
            return new DetailDialog(context,this);
        }
    }
}
