package com.comp5216.cloudcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import com.comp5216.cloudcamera.adapter.GridViewPhotosAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    Intent cameraIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraIntent = new Intent(this, CameraActivity.class);
        initFabButton();
        initGridView();
        syncToFirebase();
    }

    public void initFabButton() {
        FloatingActionButton fab = findViewById(R.id.fab_main_page);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // handle clicking fab button
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    public void initGridView() {
        GridView gridView = findViewById(R.id.gridview_main_page);
        gridView.setAdapter(new GridViewPhotosAdapter(this));
    }

    public void syncToFirebase() {
        FirebaseApp.initializeApp(this);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://friendly-eats-25bad.appspot.com");
        File directory = new File(Environment.getExternalStorageDirectory() + "/cloudphotos/");
        ArrayList<String> localFiles = new ArrayList<>();
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
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }

                        for (StorageReference item : listResult.getItems()) {
                            Log.e("Faaa", item.getName());
                            firebaseFiles.add(item.getName());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        firebaseFiles.add("");
                    }
                });

        if (firebaseFiles.size() == 1 && firebaseFiles.get(0).equals("")) {
            // TODO could not load files
            return;
        }

        Collections.sort(localFiles);
        Collections.sort(firebaseFiles);

        System.out.println(localFiles);
        System.out.println(firebaseFiles);

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
    }

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
}