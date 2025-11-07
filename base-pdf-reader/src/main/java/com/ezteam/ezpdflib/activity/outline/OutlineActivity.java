package com.ezteam.ezpdflib.activity.outline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.ezteam.ezpdflib.R;
import com.ezteam.ezpdflib.adapter.OutlineAdapter;
import com.ezteam.ezpdflib.databinding.LibActivityOutlineBinding;
import com.ezteam.ezpdflib.model.OutlineModel;
import com.ezteam.ezpdflib.util.Config;
import com.ezteam.nativepdf.OutlineItem;
//import com.google.android.gms.ads.ez.EzAdControl;

import java.util.ArrayList;

public class OutlineActivity extends LocalizationActivity {

    private OutlineAdapter outlineAdapter;
    private ArrayList<OutlineModel> lstData = new ArrayList<>();
    private ArrayList<OutlineModel> lstDataLevel0 = new ArrayList<>();
    private LibActivityOutlineBinding binding;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LibActivityOutlineBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
//        EzAdControl.getInstance(this).showAds();
        initView();
        initListener();
    }

    private void initView() {
        if (getIntent() != null) {
            OutlineItem[] lstOutline = SingleOutline.getInstance().getLstOutline();

            if (lstOutline.length != 0) {
                for (OutlineItem outlineItem : lstOutline) {
                    lstData.add(
                            new OutlineModel(
                                    outlineItem.level,
                                    outlineItem.title,
                                    outlineItem.page
                            )
                    );
                }
            }

        }

        if (lstData != null) {
            for (int i = 0; i < lstData.size(); i++) {
                OutlineModel outlineModel = lstData.get(i);
                if (outlineModel.getLevel() == lstData.get(0).getLevel()) {
                    OutlineModel itemChild = new OutlineModel(outlineModel.getLevel(), outlineModel.getTitle(), outlineModel.getPage());
                    itemChild.setListChild(getListChild(outlineModel.getLevel(), i));
                    lstDataLevel0.add(itemChild);
                }
            }

            outlineAdapter = new OutlineAdapter(this, lstDataLevel0, new OutlineAdapter.ItemClickListener() {
                @Override
                public void onItemClickListener(OutlineModel outlineModel) {
                    goToPage(outlineModel.getPage());
                }

                @Override
                public void onItemShowChildListener(OutlineModel outlineModel) {
                    if (!outlineModel.getListChild().isEmpty()) {
                        openFrmChild(outlineModel);
                    }
                }
            });

            binding.rcvOutline.setAdapter(outlineAdapter);

            binding.llNodata.setVisibility(outlineAdapter.list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openFrmChild(OutlineModel outlineModel) {
        int index = 0;
        for (int i = 0; i < lstData.size(); i++) {
            if (lstData.get(i).getLevel() == outlineModel.getLevel()
                    && lstData.get(i).getTitle().equals(outlineModel.getTitle())) {
                index = i;
                break;
            }
        }
        replaceFragment(FragmentOutline.getInstance(outlineModel.getListChild(), lstData, index, new FragmentOutline.ItemOutlineListener() {
            @Override
            public void goPage(OutlineModel outlineModel) {
                goToPage(outlineModel.getPage());
            }

            @Override
            public void openChild(OutlineModel outlineModel) {
                openFrmChild(outlineModel);
            }
        }));
    }

    private ArrayList<OutlineModel> getListChild(int levelParent, int indexParent) {
        ArrayList<OutlineModel> lstChild = new ArrayList<>();
        int levelChild = levelParent;
        if (indexParent == lstData.size() - 1)
            return lstChild;
        for (int i = indexParent + 1; i < lstData.size(); i++) {
            OutlineModel outlineItem = lstData.get(i);
            if (outlineItem.getLevel() == levelParent)
                break;
            /* gán level của thằng con*/
            if (levelChild == levelParent) {
                levelChild = outlineItem.getLevel();
                lstChild.add(outlineItem);
            } else if (outlineItem.getLevel() == levelChild) {
                lstChild.add(outlineItem);
            }
        }
        return lstChild;
    }


    private void goToPage(int page) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Config.Constant.DATA_PAGE, page);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void initListener() {
        binding.imBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped) { //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.pull_in_right, 0, 0, R.anim.push_out_right);
            ft.add(R.id.layout_container, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
