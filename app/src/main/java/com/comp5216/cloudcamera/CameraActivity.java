package com.comp5216.cloudcamera;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.comp5216.cloudcamera.views.CameraSurfaceView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    Camera camera;
    FrameLayout frameLayout;
    CameraSurfaceView cameraSurfaceView;
    Button capture;
    Bitmap clickedImageBitmap;
    Bitmap compressedImage;
    ByteArrayOutputStream imageArray;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        frameLayout = (FrameLayout) findViewById(R.id.framelayout_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initFirebase();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);
        } else {
            initCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] grants) {
        if (reqCode == 50) initCamera();
        else if (reqCode == 100) {
            String fileName = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            createDirectoryAndSaveImage(fileName);
            pushToFirebase(fileName);
        }
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
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            clickedImageBitmap = bitmap;
            displayPreview();
        }
    };

    private void displayPreview() {
        setContentView(R.layout.image_preview);
        final ImageView imageView = findViewById(R.id.imageview_preview);

        ViewTreeObserver vto = imageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = imageView.getMeasuredHeight();
                int finalWidth = imageView.getMeasuredWidth();
//                imageView.setImageBitmap(Bitmap.createScaledBitmap(
//                        clickedImageBitmap, finalWidth, finalHeight, false));
                imageView.setImageBitmap(clickedImageBitmap);
                imageView.setRotation(imageView.getRotation() + 90);
                return true;
            }
        });

        Button saveAndQuit = findViewById(R.id.button_confirmsave);
        saveAndQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());

                compressImage(clickedImageBitmap);
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                else {
                    createDirectoryAndSaveImage(fileName);
                }

                pushToFirebase(fileName);
                finish();
            }
        });
    }

    private void compressImage(Bitmap clickedImageBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        compressedImage = Bitmap.createBitmap(clickedImageBitmap, 0, 0, clickedImageBitmap.getWidth(), clickedImageBitmap.getHeight(), matrix, true);
        imageArray = new ByteArrayOutputStream();
        compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, imageArray);
    }

    private void createDirectoryAndSaveImage(String fileName) {

        File directory = new File(Environment.getExternalStorageDirectory() + "/cloudphotos/");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName + ".jpeg");
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(imageArray.toByteArray());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pushToFirebase(String fileName) {

        StorageReference storageRef = storage.getReferenceFromUrl("gs://friendly-eats-25bad.appspot.com");
        StorageReference clickedRef = storageRef.child(fileName);
        UploadTask uploadTask = clickedRef.putBytes(imageArray.toByteArray());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("NOOOOO", "NOOOOO");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
    }
}
