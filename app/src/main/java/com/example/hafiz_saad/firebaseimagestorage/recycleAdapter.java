package com.example.hafiz_saad.firebaseimagestorage;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

public class recycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> images;
    private Context context;
    private ImageLoader imageLoader;
    public recycleAdapter(Context applicationContext, List<String> imageURLs) {
//        Fresco.initialize(applicationContext);
        this.images = imageURLs;
        this.context = applicationContext;
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(applicationContext));
        imageLoader = ImageLoader.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.showimages,parent,false);
        return new Items(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        MainActivity.storageReference = MainActivity.storage.getReferenceFromUrl(images.get(position));
        MainActivity.storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageLoader.displayImage(uri.toString(),((Items)holder).imageView);
//                ((Items)holder).fresco.setImageURI(uri.toString());

                //Handle whatever you're going to do with the URL here
            }
        });
//        ((Items) holder).fresco.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
    public class Items extends RecyclerView.ViewHolder{
        ImageView imageView;
//        SimpleDraweeView fresco;

        public Items(View view) {
            super(view);
//            fresco = (SimpleDraweeView) view.findViewById(R.id.images);;
            imageView = (ImageView) view.findViewById(R.id.images);
        }
    }
}
