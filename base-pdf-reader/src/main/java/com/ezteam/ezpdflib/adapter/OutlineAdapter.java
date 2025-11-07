package com.ezteam.ezpdflib.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ezteam.ezpdflib.R;
import com.ezteam.ezpdflib.databinding.LibItemOutlineBinding;
import com.ezteam.ezpdflib.model.OutlineModel;

import java.util.ArrayList;

public class OutlineAdapter extends BaseRecyclerAdapter<OutlineModel, OutlineAdapter.ViewHolder> {

    private ItemClickListener itemListener;

    public interface ItemClickListener {
        void onItemClickListener(OutlineModel outlineModel);

        void onItemShowChildListener(OutlineModel outlineModel);
    }

    public OutlineAdapter(Context context, ArrayList<OutlineModel> list, ItemClickListener itemListener) {
        super(context, list);
        this.itemListener = itemListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(list.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.lib_item_outline, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LibItemOutlineBinding outlineBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            outlineBinding = LibItemOutlineBinding.bind(itemView);
        }

        public void bindData(OutlineModel outlineItem) {
            if (outlineItem == null)
                return;
            outlineBinding.tvPageNumber.setText(String.valueOf(outlineItem.getPage()));
            outlineBinding.imNext.setVisibility(outlineItem.getListChild().size() != 0 ? View.VISIBLE : View.INVISIBLE);
            if (!TextUtils.isEmpty(outlineItem.getTitle())) {
                outlineBinding.tvLabel.setText(outlineItem.getTitle().trim());
            }

            itemView.setOnClickListener(v -> {
                if (itemListener != null)
                    itemListener.onItemClickListener(outlineItem);
            });

            outlineBinding.imNext.setOnClickListener(v -> {
                if (itemListener != null)
                    itemListener.onItemShowChildListener(outlineItem);
            });
        }
    }
}
