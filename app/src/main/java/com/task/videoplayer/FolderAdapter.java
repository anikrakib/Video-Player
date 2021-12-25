package com.task.videoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {

    private final ArrayList<String> folderName;
    private final ArrayList<VideoModel> videoModels;
    private final Context context;

    public FolderAdapter(ArrayList<String> folderName, ArrayList<VideoModel> videoModels, Context context) {
        this.folderName = folderName;
        this.videoModels = videoModels;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        int index = folderName.get(position).lastIndexOf("/");
        String folderNames = folderName.get(position).substring(index + 1);

        holder.name.setText(folderNames);
        holder.countVideos.setText(countVideos(folderName.get(position)) + " Videos");
        holder.itemView.setOnClickListener(view -> context.startActivity(new Intent(context, VideoFolder.class).putExtra("folderName", folderName.get(position))));
    }

    @Override
    public int getItemCount() {
        return folderName.size();
    }

    int countVideos(String folders) {
        int count = 0;
        for (VideoModel model : videoModels) {
            if (model.getPath().substring(0,
                    model.getPath().lastIndexOf("/"))
                    .endsWith(folders)) {
                count++;
            }
        }
        return count;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, countVideos;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.folderName);
            countVideos = itemView.findViewById(R.id.videosCount);
        }
    }
}
