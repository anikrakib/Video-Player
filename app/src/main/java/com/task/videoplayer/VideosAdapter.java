package com.task.videoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyHolder> {

    public ArrayList<VideoModel> videoFolder;
    private final Context context;

    public VideosAdapter(ArrayList<VideoModel> videoFolder, Context context) {
        this.videoFolder = videoFolder;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Glide.with(context).load(videoFolder.get(position).getPath()).into(holder.thumbnail);
        holder.title.setText(videoFolder.get(position).getTitle());
        holder.duration.setText(videoFolder.get(position).getDuration());
        holder.size.setText("Size: " + videoFolder.get(position).getSize());
        holder.resolution.setText("Quality: " + videoFolder.get(position).getResolution());

    }

    @Override
    public int getItemCount() {
        return videoFolder.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, menu;
        TextView title, size, duration, resolution;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.video_title);
            size = itemView.findViewById(R.id.video_size);
            duration = itemView.findViewById(R.id.video_duration);
            resolution = itemView.findViewById(R.id.video_quality);
            menu = itemView.findViewById(R.id.moreOption);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSearchList(ArrayList<VideoModel> searchList) {
        videoFolder = new ArrayList<>();
        videoFolder.addAll(searchList);
        notifyDataSetChanged();
    }
}