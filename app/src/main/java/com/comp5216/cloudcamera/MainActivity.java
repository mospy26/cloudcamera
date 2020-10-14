package com.comp5216.cloudcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.comp5216.cloudcamera.adapter.GridViewPhotosAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 *
 *
 */
public class MainActivity extends AppCompatActivity {

    Intent cameraIntent;
    GridView gridView;
    GridViewPhotosAdapter gridViewAdapter; // Custom adapter for grid view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraIntent = new Intent(this, CameraActivity.class);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    /**
     * Will run when file permissions are given
     *
     * @param reqCode
     * @param permissions
     * @param grants
     */
    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] grants) {
        if (reqCode == 100) {
            initFabButtons();
            initGridView();
            View mainPage = findViewById(R.id.gridview_main_page);
            Snackbar.make(mainPage, "Syncing, hang on ... ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            syncToFirebase(mainPage);
        }
    }

    /**
     * Initialises floating action buttons i.e. camera and sync button
     */
    public void initFabButtons() {
        FloatingActionButton fab = findViewById(R.id.fab_main_page);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // handle clicking fab button
                Snackbar.make(view, "Syncing, hang on ... ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivityForResult(cameraIntent, 1);
            }
        });

        FloatingActionButton syncFab = findViewById(R.id.fab_sync);
        syncFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Syncing hang on ... ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                syncToFirebase(view);
            }
        });
    }

    /**
     * Initialises grid view containing photos
     */
    public void initGridView() {
        gridView = findViewById(R.id.gridview_main_page);
        setAdapter();
    }

    /**
     * Helper to set grid view adapter
     */
    public void setAdapter() {
        gridViewAdapter = new GridViewPhotosAdapter(this);
        gridView.setAdapter(gridViewAdapter);
    }

    /**
     * This function is responsible for syncing the local images to the cloud
     *  - Compares file names both in the cloud and locally
     *  - Uploades the images not in cloud but in local directory, deletes images in cloud that are not in local directory
     *
     * @param view The view to provide feedback from firebase in the form of SnackBars
     */
    public void syncToFirebase(final View view) {
        FirebaseApp.initializeApp(this);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://friendly-eats-25bad.appspot.com");
        File directory = new File(Environment.getExternalStorageDirectory() + "/cloudphotos/");
        final ArrayList<String> localFiles = new ArrayList<>();
        for (File file : directory.listFiles()) {
            localFiles.add(file.getName());
        }

        final ArrayList<Boolean> hasLoadedFiles = new ArrayList<>();
        hasLoadedFiles.add(false);

        final ArrayList<String> firebaseFiles = new ArrayList<>();
        storageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {
                        }

                        for (StorageReference item : listResult.getItems()) {
                            firebaseFiles.add(item.getName());
                        }

                        if (firebaseFiles.size() == 1 && firebaseFiles.get(0).equals("")) {
                            return;
                        }

                        Collections.sort(localFiles);
                        Collections.sort(firebaseFiles);

                        ArrayList<String> toDeleteOnFirebase = new ArrayList<>();
                        ArrayList<String> toPushOnFirebase = new ArrayList<>();

                        for (String fileName : firebaseFiles) {
                            if (!localFiles.contains(fileName)) toDeleteOnFirebase.add(fileName);
                        }

                        for (String fileName : localFiles) {
                            if (!firebaseFiles.contains(fileName)) toPushOnFirebase.add(fileName);
                        }

                        try {
                            deleteFilesInFirebase(toDeleteOnFirebase, storageRef);
                            uploadFilesInFirebase(toPushOnFirebase, storageRef);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Snackbar.make(view, "Synced!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        firebaseFiles.add("");
                    }
                });
    }

    /**
     * Deletes files from cloud that are not present in the local directory
     *
     * @param fileNames List of filenames to be deleted
     * @param storageRef Firebase Storage reference object
     * @throws IOException
     */
    public void deleteFilesInFirebase(ArrayList<String> fileNames, StorageReference storageRef) throws IOException {
        System.out.println(fileNames);
        for (String fileName : fileNames) {
            StorageReference clickedRef = storageRef.child(fileName);
            clickedRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d("Delete from Firebase", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.e("Delete from Firebase", "onFailure: did not delete file");
                }
            });
        }
    }

    /**
     * Uploades files to cloud that are present in the local directory but not in cloud
     *
     * @param fileNames List of file names of that are to be uploaded
     * @param storageRef Firebase Storage reference object
     * @throws IOException
     */
    public void uploadFilesInFirebase(ArrayList<String> fileNames, StorageReference storageRef) throws IOException {

        for (String fileName : fileNames) {
            StorageReference clickedRef = storageRef.child(fileName);
            File file = new File(Environment.getExternalStorageDirectory() + "/cloudphotos/" + fileName);
            byte[] bytes = new byte[(int) file.length()];
            new FileInputStream(file).read(bytes);
            UploadTask uploadTask = clickedRef.putBytes(bytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }
    }

    /**
     * Runs when the subactivity i.e. Camera activity finishes successfully
     *
     * @param correlationId
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int correlationId, int resultCode,
                                    Intent data) {
        super.onActivityResult(correlationId, resultCode, data);
        setAdapter();
    }
}