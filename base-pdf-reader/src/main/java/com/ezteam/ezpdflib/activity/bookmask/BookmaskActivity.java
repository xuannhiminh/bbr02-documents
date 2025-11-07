package com.ezteam.ezpdflib.activity.bookmask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.ezteam.ezpdflib.adapter.BookmarkAdapter;
import com.ezteam.ezpdflib.database.DatabaseService;
import com.ezteam.ezpdflib.database.repository.FileRepository;
import com.ezteam.ezpdflib.databinding.LibActivityBookmaskBinding;
import com.ezteam.ezpdflib.model.Bookmark;
import com.ezteam.ezpdflib.util.Config;
//import com.google.android.gms.ads.ez.EzAdControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookmaskActivity extends LocalizationActivity {

    public CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FileRepository repository;
    private String path;

    private BookmarkAdapter bookmarkAdapter;
    private LibActivityBookmaskBinding binding;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LibActivityBookmaskBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
//        EzAdControl.getInstance(this).showAds();
        initView();
        initListener();
    }

    private void initView() {
        path = getIntent().getStringExtra(Config.Constant.DATA_URI_PDF);
        repository = new FileRepository(DatabaseService.getInstance(this).fileDao());

        binding.llNodata.setVisibility(View.VISIBLE);

        compositeDisposable.add(repository.getFileStatus(path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(filsStatus -> {
                    if (filsStatus != null) {
                        if (filsStatus.getLstbookmark() != null) {
                            setAdapter(filsStatus.getLstbookmark());
                        }
                    }
                }));

        bookmarkAdapter = new BookmarkAdapter(this, new ArrayList<>());
        binding.rcvData.setAdapter(bookmarkAdapter);
    }

    private void setAdapter(List<Bookmark> lstbookmark) {
        Collections.sort(lstbookmark, (o1, o2) -> o1.getPage() - o2.getPage());
        bookmarkAdapter.addAll(lstbookmark);
        binding.llNodata.setVisibility(
                bookmarkAdapter.list.isEmpty()
                        ? View.VISIBLE
                        : View.GONE);
    }

    private void initListener() {
        bookmarkAdapter.setItemListener(bookmark -> {

            Intent returnIntent = new Intent();
            returnIntent.putExtra(Config.Constant.DATA_PAGE, bookmark.getPage());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
        binding.imBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
