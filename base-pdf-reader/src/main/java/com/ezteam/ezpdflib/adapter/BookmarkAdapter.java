package com.ezteam.ezpdflib.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ezteam.ezpdflib.R;
import com.ezteam.ezpdflib.databinding.LibItemBookmarkBinding;
import com.ezteam.ezpdflib.listener.EzItemListener;
import com.ezteam.ezpdflib.model.Bookmark;

import java.text.SimpleDateFormat;
import java.util.List;


public class BookmarkAdapter extends BaseRecyclerAdapter<Bookmark, BookmarkAdapter.ViewHolder> {

    private EzItemListener<Bookmark> itemListener;

    public void setItemListener(EzItemListener<Bookmark> itemListener) {
        this.itemListener = itemListener;
    }

    public BookmarkAdapter(Context context, List<Bookmark> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(list.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.lib_item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LibItemBookmarkBinding bookmarkBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookmarkBinding = LibItemBookmarkBinding.bind(itemView);
        }

        public void bindData(Bookmark bookmark) {
            bookmarkBinding.tvTitle.setText(mContext.getString(R.string.page, String.valueOf(bookmark.getPage() + 1)));
            bookmarkBinding.tvCreateDate.setText(longToDateString(bookmark.getTimeSave(), "yyyy-MM-dd HH:mm:ss"));
            itemView.setOnClickListener(v -> {
                if (itemListener != null)
                    itemListener.onListener(bookmark);
            });
        }

        public String longToDateString(long time, String fomat) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fomat);
                return sdf.format(time);
            } catch (Exception e) {
                return "";
            }
        }
    }
}
