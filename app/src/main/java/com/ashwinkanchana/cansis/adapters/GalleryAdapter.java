package com.ashwinkanchana.cansis.adapters;



import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.data.Gallery;
import com.zolad.zoominimageview.ZoomInImageViewAttacher;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {
    private Context galleryContext;

    private List<Gallery> galleryList;
    private ZoomInImageViewAttacher zoomView;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView sub_name;
        public ImageView urlk;


        public MyViewHolder(View view) {
            super(view);
            sub_name = view.findViewById(R.id.sub_name);
            urlk = view.findViewById(R.id.urlk);


        }
    }



    public GalleryAdapter(Context context, List<Gallery> galleryList) {
        this.galleryContext = context;
        this.galleryList = galleryList;

    }


    @Override
    public GalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        return new GalleryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Gallery gallery = galleryList.get(position);
        try{
            zoomView = new ZoomInImageViewAttacher();
            RequestOptions options;
            //Devices older than N crashes with transparent placeholders
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                options = new RequestOptions()
                        .placeholder(R.drawable.grey_placeholder)
                        .error(R.drawable.error_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH);

            } else {
                options = new RequestOptions()
                        .placeholder(R.drawable.grey_placeholder_old)
                        .error(R.drawable.error_placeholder_old)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH);
            }
            Glide.with(holder.itemView.getContext())
                    .load(gallery.getUrlk())
                    .apply(options)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            zoomView.detach();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            zoomView.attachImageView(holder.urlk);
                            return false;
                        }
                    })
                    .into(holder.urlk);
        }catch (Exception e){
            e.printStackTrace();
        }
        holder.sub_name.setText(gallery.getSub_name());
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GalleryAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final GalleryAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}

