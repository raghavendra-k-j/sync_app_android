package com.example.syncapp.activities.mypictures;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syncapp.databinding.ListItemPictureBinding;
import com.example.syncapp.entity.MyPicture;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.util.ArrayList;

public class MyPicturesAdapter extends RecyclerView.Adapter<MyPicturesAdapter.PictureViewHolder> {

    private final Context context;
    private final ArrayList<MyPicture> myPictureArrayList;
    private final LayoutInflater layoutInflater;


    public MyPicturesAdapter(Context context, ArrayList<MyPicture> myPictureArrayList) {
        this.context = context;
        this.myPictureArrayList = myPictureArrayList;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyPicturesAdapter.PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PictureViewHolder(ListItemPictureBinding.inflate(layoutInflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyPicturesAdapter.PictureViewHolder holder, int position) {
        MyPicture myPicture = myPictureArrayList.get(position);
        holder.binding.listItemPictureImageView.setImageBitmap(myPicture.getBitmap(context));
    }

    @Override
    public int getItemCount() {
        return myPictureArrayList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<MyPicture> myPictureArrayList) {
        this.myPictureArrayList.clear();
        this.myPictureArrayList.addAll(myPictureArrayList);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addItem(MyPicture myPicture) {
        this.myPictureArrayList.add(myPicture);
        notifyDataSetChanged();
    }

    public static class PictureViewHolder extends RecyclerView.ViewHolder {
        ListItemPictureBinding binding;
        public PictureViewHolder(@NonNull ListItemPictureBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
