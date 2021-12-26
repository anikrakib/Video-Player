package com.task.videoplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FolderAdapter folderAdapter;
    RecyclerView recyclerView;
    private final ArrayList<String> folderList = new ArrayList<>();
    public ArrayList<VideoModel> videoList;
    public static String orderBy;
    private static final String MY_SORT_PREF = "sortOrder";


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.folder_recyclerview);
        ImageView sort = findViewById(R.id.sort);

        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();

        sort.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), sort);
            popupMenu.getMenuInflater().inflate(R.menu.sort_by_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.sort_by_date:
                        editor.putString("sorting", "sortByDate");
                        editor.apply();
                        this.recreate();
                        break;

                    case R.id.sort_by_name:
                        editor.putString("sorting", "sortByName");
                        editor.apply();
                        this.recreate();
                        break;

                    case R.id.sort_by_size:
                        editor.putString("sorting", "sortBySize");
                        editor.apply();
                        this.recreate();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        videoList = fetchAllVideos(this);
        if (folderList != null && folderList.size() > 0) {
            folderAdapter = new FolderAdapter(folderList, videoList, this);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
        } else {
            Toast.makeText(this, "can't find any videos folder", Toast.LENGTH_SHORT).show();
        }

    }

    //this is the method will fetch  all videos from internal or external storage
    private ArrayList<VideoModel> fetchAllVideos(MainActivity context) {
        ArrayList<VideoModel> videoModels = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        //which one you want to set default in sorting
        // i am setting by date
        String sort = preferences.getString("sorting", "sortByDate");

        switch (sort) {
            case "sortByDate":
                orderBy = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;

            case "sortByName":
                orderBy = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;

            case "sortBySize":
                orderBy = MediaStore.MediaColumns.SIZE + " ASC";
                break;
        }
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.RESOLUTION};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String size = cursor.getString(3);
                String resolution = cursor.getString(4);
                String duration = cursor.getString(5);
                String disName = cursor.getString(6);
                String width_height = cursor.getString(7);

                VideoModel videoFiles = new VideoModel(id, path, title, size, resolution, duration, disName, width_height);
                int slashFirstIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFirstIndex);
                if (!folderList.contains(subString)) {
                    folderList.add(subString);
                }
                videoModels.add(videoFiles);
            }
            cursor.close();
        }
        return videoModels;

    }

}