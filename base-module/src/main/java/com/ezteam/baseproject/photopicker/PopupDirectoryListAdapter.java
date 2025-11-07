package com.ezteam.baseproject.photopicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ezteam.baseproject.R;

import java.util.List;


/**
 * Created by donglua on 15/6/28.
 */
public class PopupDirectoryListAdapter extends BaseAdapter {


  private List<PhotoDirectory> directories;
  private RequestManager glide;

  public PopupDirectoryListAdapter(RequestManager glide, List<PhotoDirectory> directories) {
    this.directories = directories;
    this.glide = glide;
  }


  @Override public int getCount() {
    return directories.size();
  }


  @Override public PhotoDirectory getItem(int position) {
    return directories.get(position);
  }


  @Override public long getItemId(int position) {
    return directories.get(position).hashCode();
  }


  @Override public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      LayoutInflater mLayoutInflater = LayoutInflater.from(parent.getContext());
      convertView = mLayoutInflater.inflate(R.layout.picker_item_directory, parent, false);
      holder = new ViewHolder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.bindData(directories.get(position));

    return convertView;
  }

  private class ViewHolder {

    public ImageView ivCover;
    public TextView tvName;
    public TextView tvCount;

    public ViewHolder(View rootView) {
      ivCover = rootView.findViewById(R.id.iv_dir_cover);
      tvName  = rootView.findViewById(R.id.tv_dir_name);
      tvCount = rootView.findViewById(R.id.tv_dir_count);
    }

    public void bindData(PhotoDirectory directory) {
      final RequestOptions options = new RequestOptions();
      options.dontAnimate()
          .dontTransform()
          .override(400, 400);
      glide.setDefaultRequestOptions(options)
          .load(directory.getCoverPath())
          .thumbnail(0.1f)
          .into(ivCover);
      tvName.setText(directory.getName());
      tvCount.setText(directory.getPhotos().size() + "");
    }
  }

}
