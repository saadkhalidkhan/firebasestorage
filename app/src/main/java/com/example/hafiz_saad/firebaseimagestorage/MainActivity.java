package com.example.hafiz_saad.firebaseimagestorage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button select, upload, getImage;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    //Firebase
    public static FirebaseStorage storage;
    public static StorageReference storageReference;
    private ImageLoader imageLoader;
    private DatabaseReference mDatabase;
    private String download_url;
    private List<String> imageURLs;
    private RecyclerView.LayoutManager mLayoutManager;
    private String user;
    private RecyclerView recyclerView;
    private recycleAdapter recycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = SignInActivity.email.getText().toString().replace(".","%");
        init_Views();
        init_Clicks();

    }
    public void init_Views(){
        getImage = (Button) findViewById(R.id.getImage);
        select = (Button) findViewById(R.id.select);
        upload = (Button) findViewById(R.id.upload);
        imageView = (ImageView) findViewById(R.id.image);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    public void init_Clicks(){
        select.setOnClickListener(this);
        upload.setOnClickListener(this);
        getImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select:
                    chooseImage();
                break;
            case R.id.upload:
                uploadImage();
                break;
            case R.id.getImage:
                getUploadedImage();
                break;
        }
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final StorageReference ref = storageReference.child("images/"+  UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded ", Toast.LENGTH_SHORT).show();
                            download_url = taskSnapshot.getDownloadUrl().toString();
                            String key = mDatabase.child(user).push().getKey();

                            mDatabase.child(user).child("Images").child(key).child("imageurl").setValue(download_url);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
//            download_url ="https://firebasestorage.googleapis.com/v0/b/image-storage-244d8.appspot.com/o/images%2F08ebce99-057e-4bdf-86bb-ca698d878cd0?alt=media&token=" + download_url;


        }
    }
    public void getUploadedImage(){
        mDatabase = FirebaseDatabase.getInstance().getReference(user);
        mDatabase.child("Images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Images","Uploaded Images URL");
                Log.d("Images",dataSnapshot.getValue().toString());
                CollectImages((Map<String,Object>) dataSnapshot.getValue());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase = mDatabase.getParent();

//        Log.d("Images",images.getDatabase().toString());
//        storageReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/image-storage-244d8.appspot.com/o/images%2Fce280ad1-f4a7-44f9-a286-e67346c42cd7?alt=media&token=88eb3b39-c677-4bf4-a0b9-696fe83978d7");
//        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Toast.makeText(getApplicationContext(),uri.toString(), Toast.LENGTH_SHORT).show();
//                imageLoader.displayImage(uri.toString(),imageView);
//                //Handle whatever you're going to do with the URL here
//            }
//        });
    }
    private void CollectImages(Map<String,Object> users) {

         imageURLs = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            imageURLs.add((String) singleUser.get("imageurl"));
            Log.d("Images", String.valueOf(imageURLs));
            recycleAdapter = new recycleAdapter(getApplicationContext(),imageURLs);
            recyclerView.setAdapter(recycleAdapter);
        }

    }
}
