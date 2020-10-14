package com.comp5216.cloudcamera.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.comp5216.cloudcamera.MainActivity;
import com.comp5216.cloudcamera.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Custom Grid View Adapter to load images into grid recycler view
 *
 * @author Mustafa
 * @version 1.0
 */
public class GridViewPhotosAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> imageUrls;
    LayoutInflater layoutInflater;

    public GridViewPhotosAdapter(Context context) {
        this.context = context;
        imageUrls = getImagePaths();
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
            GridView gridView = ((MainActivity) context).findViewById(R.id.gridview_main_page);
            imageView.setLayoutParams(new GridView.LayoutParams(gridView.getLayoutParams().width, 480));
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

    /**
     * Helper method to get all image file names
     *
     * @return List of image file names that are in the local directory to be displayed into the grid view
     */
    private ArrayList<String> getImagePaths() {
        ArrayList<String> paths = new ArrayList<>();
        File directory = new File(Environment.getExternalStorageDirectory() + "/cloudphotos/");
        if (!directory.exists()) {
            return paths;
        }

        for (File f: directory.listFiles()) {
            paths.add(directory + "/" + f.getName());
        }

        return paths;
    }
}
