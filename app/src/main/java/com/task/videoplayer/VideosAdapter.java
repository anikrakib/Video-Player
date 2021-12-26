package com.task.videoplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

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

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Glide.with(context).load(videoFolder.get(position).getPath()).into(holder.thumbnail);
        holder.title.setText(videoFolder.get(position).getTitle());
        holder.duration.setText(videoFolder.get(position).getDuration());
        holder.size.setText("Size: " + videoFolder.get(position).getSize());
        holder.resolution.setText("Quality: " + videoFolder.get(position).getResolution());
        holder.moreOption.setOnClickListener(view -> {
            /*Toast.makeText(context,"Clicked",Toast.LENGTH_SHORT).show();
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.more_option_video_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.share:
                        shareFile(position);
                        break;
                    case R.id.rename:
                        renameFiles(position, view);
                        break;
                    case R.id.delete:
                        deleteFiles(position, view);
                        break;
                    case R.id.properties:
                        showProperties(position);
                        break;
                }
                return false;
            });
            popupMenu.show();*/
            /*PopupMenu popupMenu = new PopupMenu(context,holder.moreOption);
            popupMenu.inflate(R.menu.more_option_video_menu);*/
            PopupMenu popupMenu = new PopupMenu(context,holder.moreOption);
            popupMenu.getMenuInflater().inflate(R.menu.more_option_video_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.share:
                        shareFile(position);
                        break;
                    case R.id.rename:
                        renameFiles(position, view);
                        break;
                    case R.id.delete:
                        deleteFiles(position, view);
                        break;
                    case R.id.properties:
                        showProperties(position);
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return videoFolder.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, moreOption;
        TextView title, size, duration, resolution;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.video_title);
            size = itemView.findViewById(R.id.video_size);
            duration = itemView.findViewById(R.id.video_duration);
            resolution = itemView.findViewById(R.id.video_quality);
            moreOption = itemView.findViewById(R.id.moreOption);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSearchList(ArrayList<VideoModel> searchList) {
        videoFolder = new ArrayList<>();
        videoFolder.addAll(searchList);
        notifyDataSetChanged();
    }

    private void shareFile(int position) {
        Uri uri = Uri.parse(videoFolder.get(position).getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "share"));
        Toast.makeText(context, "loading..", Toast.LENGTH_SHORT).show();
    }

    private void deleteFiles(int p, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete")
                .setMessage(videoFolder.get(p).getTitle())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // leave it as empty
                }).setPositiveButton("Ok", (dialog, which) -> {
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            Long.parseLong(videoFolder.get(p).getId()));
                    File file = new File(videoFolder.get(p).getPath());
                    boolean deleted = file.delete();
                    if (deleted) {
                        context.getApplicationContext().getContentResolver()
                                .delete(contentUri,
                                        null, null);
                        videoFolder.remove(p);
                        notifyItemRemoved(p);
                        notifyItemRangeChanged(p, videoFolder.size());
                        Snackbar.make(view, "File Deleted Success",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(view, "File Deleted Fail",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void renameFiles(int position, View view) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rename_layout);
        final EditText editText = dialog.findViewById(R.id.rename_edit_text);
        Button cancel = dialog.findViewById(R.id.cancel_rename_button);
        Button rename_btn = dialog.findViewById(R.id.rename_button);
        final File renameFile = new File(videoFolder.get(position).getPath());
        String nameText = renameFile.getName();
        nameText = nameText.substring(0, nameText.lastIndexOf("."));
        editText.setText(nameText);
        editText.clearFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.
                LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        cancel.setOnClickListener(v -> dialog.dismiss());
        rename_btn.setOnClickListener(v1 -> {
            String onlyPath = Objects.requireNonNull(renameFile.getParentFile()).getAbsolutePath();
            String ext = renameFile.getAbsolutePath();
            ext = ext.substring(ext.lastIndexOf("."));
            String newPath = onlyPath + "/" + editText.getText() + ext;
            File newFile = new File(newPath);
            boolean rename = renameFile.renameTo(newFile);
            if (rename) {
                context.getApplicationContext().getContentResolver().
                        delete(MediaStore.Files.getContentUri("external"),
                                MediaStore.MediaColumns.DATA + "=?",
                                new String[]{renameFile.getAbsolutePath()});
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(newFile));
                context.getApplicationContext().sendBroadcast(intent);
                notifyDataSetChanged();
                Snackbar.make(view, "Rename Success", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(view, "Rename Failed", Snackbar.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showProperties(int p) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.file_properties);

        String name = videoFolder.get(p).getTitle();
        String path = videoFolder.get(p).getPath();
        String size = videoFolder.get(p).getSize();
        String duration = videoFolder.get(p).getDuration();
        String resolution = videoFolder.get(p).getResolution();

        TextView titleTv = dialog.findViewById(R.id.pro_title);
        TextView storageTv = dialog.findViewById(R.id.pro_storage);
        TextView sizeTv = dialog.findViewById(R.id.pro_size);
        TextView durationTv = dialog.findViewById(R.id.pro_duration);
        TextView resolutionTv = dialog.findViewById(R.id.pro_resolution);

        titleTv.setText(name);
        storageTv.setText(path);
        sizeTv.setText(size);
        durationTv.setText(duration);
        resolutionTv.setText(resolution + "p");

        dialog.show();

    }

}