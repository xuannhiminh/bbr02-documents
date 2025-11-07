package com.ezteam.ezpdflib.activity.outline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ezteam.ezpdflib.adapter.OutlineAdapter;
import com.ezteam.ezpdflib.databinding.LibFragmentOutlineBinding;
import com.ezteam.ezpdflib.model.OutlineModel;
import com.ezteam.ezpdflib.util.KeyboardUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentOutline extends Fragment {

    private static final String LIST_ALL = "list all";
    private static final String LIST_CHILD = "list child";
    private static final String INDEX_START = "index start";

    public static FragmentOutline getInstance(ArrayList<OutlineModel> listChild, ArrayList<OutlineModel> listAll, int indexStart, ItemOutlineListener itemOutlineListener) {
        FragmentOutline fragmentOutline = new FragmentOutline();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(LIST_ALL, listAll);
        bundle.putParcelableArrayList(LIST_CHILD, listChild);
        bundle.putInt(INDEX_START, indexStart);
        fragmentOutline.setArguments(bundle);
        fragmentOutline.itemOutlineListener = itemOutlineListener;
        return fragmentOutline;
    }

    private OutlineAdapter outlineAdapter;
    private ArrayList<OutlineModel> lstAll = new ArrayList<>();
    private ArrayList<OutlineModel> lstChild = new ArrayList<>();
    private ArrayList<OutlineModel> lstDataChild = new ArrayList<>();
    private int indexStart;
    private ItemOutlineListener itemOutlineListener;
    private LibFragmentOutlineBinding binding;

    public interface ItemOutlineListener {
        void goPage(OutlineModel outlineModel);

        void openChild(OutlineModel outlineModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LibFragmentOutlineBinding.inflate(inflater, container, false);
        KeyboardUtils.hideSoftKeyboard(getActivity());
        KeyboardUtils.autoHideClickView(binding.getRoot(), getActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        if (getArguments() != null) {
            lstAll = getArguments().getParcelableArrayList(LIST_ALL);
            lstChild = getArguments().getParcelableArrayList(LIST_CHILD);
            indexStart = getArguments().getInt(INDEX_START);
        }
        if (lstAll != null) {
            for (int i = indexStart + 1; i < lstAll.size(); i++) {
                OutlineModel outlineModel = lstAll.get(i);
                if (outlineModel.getLevel() < lstChild.get(0).getLevel())
                    break;
                if (outlineModel.getLevel() == lstChild.get(0).getLevel()) {
                    OutlineModel itemChild = new OutlineModel(outlineModel.getLevel(), outlineModel.getTitle(), outlineModel.getPage());
                    itemChild.setListChild(getListChild(outlineModel.getLevel(), i));
                    lstDataChild.add(itemChild);
                }
            }

            outlineAdapter = new OutlineAdapter(getActivity(), lstDataChild, new OutlineAdapter.ItemClickListener() {
                @Override
                public void onItemClickListener(OutlineModel outlineModel) {
                    if (itemOutlineListener != null)
                        itemOutlineListener.goPage(outlineModel);
                }

                @Override
                public void onItemShowChildListener(OutlineModel outlineModel) {
                    if (!outlineModel.getListChild().isEmpty()) {
                        if (itemOutlineListener != null)
                            itemOutlineListener.openChild(outlineModel);
                    }
                }
            });

            binding.rcvOutline.setAdapter(outlineAdapter);
        }
    }

    private ArrayList<OutlineModel> getListChild(int levelParent, int indexParent) {
        ArrayList<OutlineModel> lstChild = new ArrayList<>();
        int levelChild = levelParent;
        if (indexParent == lstAll.size() - 1)
            return lstChild;
        for (int i = indexParent + 1; i < lstAll.size(); i++) {
            OutlineModel outlineItem = lstAll.get(i);
            if (outlineItem.getLevel() <= levelParent)
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

}
