package com.ezteam.ezpdflib.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ezteam.ezpdflib.R;
import com.ezteam.ezpdflib.databinding.LibItemThumbnailBinding;
import com.ezteam.ezpdflib.listener.EzItemListener;

import java.util.List;

public class ThumbnailAdapter extends BaseRecyclerAdapter<Uri, ThumbnailAdapter.ViewHolder> {

    private EzItemListener<Integer> itemListener;

    public ThumbnailAdapter(Context context, List<Uri> list, EzItemListener<Integer> itemListener) {
        super(context, list);
        this.itemListener = itemListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(list.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.lib_item_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LibItemThumbnailBinding ThumbnailBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ThumbnailBinding = LibItemThumbnailBinding.bind(itemView);
        }

        public void bindData(Uri bitmap) {
            Glide.with(mContext)
                    .load(bitmap)
                    .into(ThumbnailBinding.imThumbnail);

            itemView.setOnClickListener(v -> {
                if (itemListener != null)
                    itemListener.onListener(getAdapterPosition());
            });
        }
    }
}
