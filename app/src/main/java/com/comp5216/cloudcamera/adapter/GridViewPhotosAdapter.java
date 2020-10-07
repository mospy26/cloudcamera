package com.comp5216.cloudcamera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GridViewPhotosAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> imageUrls;
    LayoutInflater layoutInflater;

    public GridViewPhotosAdapter(Context context) {
        this.context = context;
        imageUrls = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            imageUrls.add("https://homepages.cae.wisc.edu/~ece533/images/airplane.png");
        }
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int i) {
        return imageUrls.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(480, 480));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) view;
        }
        String url = (String) getItem(i);
        Glide.with(context)
                .load(url)
                .centerCrop()
                .placeholder(android.R.drawable.spinner_background)
                .into(imageView);
        return imageView;
    }
}
