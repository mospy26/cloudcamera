package com.comp5216.cloudcamera;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.comp5216.cloudcamera.views.CameraSurfaceView;
import com.google.android.material.snackbar.Snackbar;

public class CameraActivity extends AppCompatActivity {
    Camera camera;
    FrameLayout frameLayout;
    CameraSurfaceView cameraSurfaceView;
    Button capture;

    // need imageview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        frameLayout = (FrameLayout) findViewById(R.id.framelayout_camera);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);
        }
        else {
            initCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] grants) {
        initCamera();
    }

    private void initCamera() {
        camera = checkCamera();
        camera.setDisplayOrientation(90);
        cameraSurfaceView = new CameraSurfaceView(this, camera);
        frameLayout.addView(cameraSurfaceView);
        capture = findViewById(R.id.button_take_photo);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, pictureCallback);
            }
        });
    }

    private Camera checkCamera() {
        Camera tryCamera = null;
        try {
            tryCamera = Camera.open();
        } catch (Exception e) {
            Log.e("Camera", e.getStackTrace().toString());
            e.printStackTrace();
        }
        return tryCamera;
    }

    // needs change
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                return;
            }
            // {TODO}: save file
        }
    };
}
