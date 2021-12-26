package com.task.videoplayer;

import static com.task.videoplayer.VideosAdapter.videoFolder;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPlayerActivity extends AppCompatActivity {

    int position = -1;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.video_view);

        position = getIntent().getIntExtra("position", -1);
        String path = videoFolder.get(position).getPath();
        if (path != null) {
            videoView.setVideoPath(path);
            videoView.setOnPreparedListener(mp -> videoView.start());
        } else {
            Toast.makeText(this, "path didn't exits", Toast.LENGTH_SHORT).show();
        }

    }
}