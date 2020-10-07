package com.comp5216.cloudcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.comp5216.cloudcamera.adapter.GridViewPhotosAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    Intent cameraIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraIntent = new Intent(this, CameraActivity.class);
        initFabButton();
        initGridView();
    }

    public void initFabButton() {
        FloatingActionButton fab = findViewById(R.id.fab_main_page);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // handle clicking fab button
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(cameraIntent);
            }
        });
    }

    public void initGridView() {
        GridView gridView = findViewById(R.id.gridview_main_page);
        gridView.setAdapter(new GridViewPhotosAdapter(this));
    }
}