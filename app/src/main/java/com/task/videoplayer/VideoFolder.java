package com.task.videoplayer;

import static com.task.videoplayer.MainActivity.orderBy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class VideoFolder extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    private RecyclerView recyclerView;
    private String name;
    private ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();
    private VideosAdapter videosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_folder);

        recyclerView = findViewById(R.id.video_recyclerview);
        TextView folderName = findViewById(R.id.folderName);
        SearchView searchView = findViewById(R.id.searchView);

        name = getIntent().getStringExtra("folderName");
        int index = name.lastIndexOf("/");
        String onlyFolderName = name.substring(index + 1);
        folderName.setText(onlyFolderName);

        searchView.setOnQueryTextListener(this);

        loadVideos();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String input = newText.toLowerCase();
        ArrayList<VideoModel> searchList = new ArrayList<>();
        for (VideoModel model : videoModelArrayList) {
            if (model.getTitle().toLowerCase().contains(input)) {
                searchList.add(model);
            }
        }
        videosAdapter.updateSearchList(searchList);

        return false;
    }

    private void loadVideos() {
        videoModelArrayList = getAllVideoFromFolder(this, name);
        if (name != null && videoModelArrayList.size() > 0) {
            videosAdapter = new VideosAdapter(videoModelArrayList, this);
            //if your recyclerview lagging then just add this line
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setNestedScrollingEnabled(false);

            recyclerView.setAdapter(videosAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
        } else {
            Toast.makeText(this, "can't find any videos", Toast.LENGTH_SHORT).show();
        }

    }

    private ArrayList<VideoModel> getAllVideoFromFolder(Context context, String name) {
        ArrayList<VideoModel> list = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.RESOLUTION
        };
        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + name + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                int size = cursor.getInt(3);
                String resolution = cursor.getString(4);
                int duration = cursor.getInt(5);
                String disName = cursor.getString(6);
                String bucket_display_name = cursor.getString(7);
                String width_height = cursor.getString(8);

                //this method convert 1204 in 1MB
                String human_can_read;
                if (size < 1024) {
                    human_can_read = String.format(context.getString(R.string.size_in_b), (double) size);
                } else if (size < Math.pow(1024, 2)) {
                    human_can_read = String.format(context.getString(R.string.size_in_kb), (double) (size / 1024));
                } else if (size < Math.pow(1024, 3)) {
                    human_can_read = String.format(context.getString(R.string.size_in_mb), size / Math.pow(1024, 2));
                } else {
                    human_can_read = String.format(context.getString(R.string.size_in_gb), size / Math.pow(1024, 3));
                }

                //this method convert any random video duration like 1331533132 into 1:21:12
                String duration_formatted;
                int sec = (duration / 1000) % 60;
                int min = (duration / (1000 * 60)) % 60;
                int hrs = duration / (1000 * 60 * 60);

                if (hrs == 0) {
                    duration_formatted = String.valueOf(min)
                            .concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                } else {
                    duration_formatted = String.valueOf(hrs)
                            .concat(":".concat(String.format(Locale.UK, "%02d", min)
                                    .concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                }

                VideoModel files = new VideoModel(id, path, title,
                        human_can_read, resolution, duration_formatted,
                        disName, width_height);
                if (name.endsWith(bucket_display_name))
                    list.add(files);

            }
            cursor.close();
        }
        return list;
    }

}