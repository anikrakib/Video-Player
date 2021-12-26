package com.task.videoplayer;

import static com.task.videoplayer.VideosAdapter.videoFolder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerActivity extends AppCompatActivity
        implements View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener, View.OnClickListener {

    RelativeLayout zoomLayout, durationLayout;
    LinearLayout videoPlayerLockScreen, unlockScreen;
    ConstraintLayout videoPlayerToolBar, videoPlayerFooter;
    ScaleGestureDetector scaleDetector;
    GestureDetectorCompat gestureDetector;

    int position = -1;
    private VideoView videoView;
    ImageView goBack, rewind, forward, playPause, muteOrNot, rotate, lockScreen;
    TextView title, videoPath, startTime, endTime, lockTextOne, lockTextTwo;
    SeekBar videoSeekBar;

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 5.0f;
    private Mode mode = Mode.NONE;

    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }

    int device_width;
    private boolean isEnable = true;
    private boolean isPause = false;
    private boolean isMute = false;
    private boolean isOpen = true;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;
    // Where the finger first  touches the screen
    private float startX = 0f;
    private float startY = 0f;
    // How much to translate the canvas
    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        /*Assigning Variables*/
        videoView = findViewById(R.id.video_view);
        videoPlayerFooter = findViewById(R.id.videoPlayerFooter);
        durationLayout = findViewById(R.id.durationLayout);
        videoPlayerToolBar = findViewById(R.id.videoPlayerToolBar);
        goBack = findViewById(R.id.back);
        title = findViewById(R.id.title);
        rewind = findViewById(R.id.goToPrevious5);
        playPause = findViewById(R.id.videoViewPlayPauseBtn);
        lockScreen = findViewById(R.id.lockScreen);
        rotate = findViewById(R.id.rotate);
        muteOrNot = findViewById(R.id.muteOrNot);
        forward = findViewById(R.id.goToForward5);
        endTime = findViewById(R.id.endTime);
        startTime = findViewById(R.id.startTime);
        videoPath = findViewById(R.id.videoPath);
        videoSeekBar = findViewById(R.id.video_seekbar);

        videoPlayerLockScreen = findViewById(R.id.videoPlayerLockScreenLayout);
        unlockScreen = findViewById(R.id.unlockScreenLayout);
        lockTextOne = findViewById(R.id.videoView_lock_text);
        lockTextTwo = findViewById(R.id.videoView_lock_text_two);


        /*Adding onClickListener*/
        goBack.setOnClickListener(this);
        rewind.setOnClickListener(this);
        playPause.setOnClickListener(this);
        forward.setOnClickListener(this);
        rotate.setOnClickListener(this);
        lockScreen.setOnClickListener(this);
        unlockScreen.setOnClickListener(this);
        videoPlayerLockScreen.setOnClickListener(this);
        //muteOrNot.setOnClickListener(this);

        /*getting path and preparing for play video*/
        position = getIntent().getIntExtra("position", -1);
        String path = videoFolder.get(position).getPath();
        String[] separated = path.split("/");

        videoPath.setText(separated[separated.length - 3] + "/" + separated[separated.length - 2]);
        title.setText(videoFolder.get(position).getTitle());
        endTime.setText(videoFolder.get(position).getDuration());

        videoView.setVideoPath(path);
        videoView.setOnPreparedListener(mp -> {
            videoSeekBar.setMax(videoView.getDuration());
            setVolumeControl(mp);
            videoView.start();
        });

        /*zoom in,out and double tap to go forward,backward
         *  and single tap to hide and show controls */
        zoomLayout = findViewById(R.id.zoom_layout);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        zoomLayout.setOnTouchListener(this);
        scaleDetector = new ScaleGestureDetector(getApplicationContext(), this);
        gestureDetector = new GestureDetectorCompat(getApplicationContext(), new GestureDetector());

        setHandler();
        initializeSeekBars();
    }

    private void initializeSeekBars() {
        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (videoSeekBar.getId() == R.id.video_seekbar) {
                    if (fromUser) {
                        videoView.seekTo(progress);
                        if (isPause) {
                            videoView.pause();
                        } else {
                            videoView.start();
                        }
                        int currentPosition = videoView.getCurrentPosition();
                        startTime.setText("" + convertIntoTime(videoView.getDuration() - currentPosition));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String convertIntoTime(int ms) {
        String time;
        int x, seconds, minutes, hours;
        x = ms / 1000;
        seconds = x % 60;
        x /= 60;
        minutes = x % 60;
        x /= 60;
        hours = x % 24;
        if (hours != 0)
            time = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        else time = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        return time;
    }

    private void setHandler() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
            @Override
            public void run() {
                if (videoView.getDuration() > 0) {
                    int currentPosition = videoView.getCurrentPosition();
                    videoSeekBar.setProgress(currentPosition);
                    startTime.setText("" + convertIntoTime(videoView.getDuration() - currentPosition));
                }
                handler.postDelayed(this, 0);
            }
        };
        handler.postDelayed(runnable, 500);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId", "SourceLockedOrientationActivity"})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.rotate:
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //set in landscape
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //set in portrait
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;

            case R.id.lockScreen:
                hideDefaultControls();
                videoPlayerLockScreen.setVisibility(View.VISIBLE);
                videoSeekBar.setVisibility(View.GONE);
                break;

            case R.id.videoPlayerLockScreenLayout:
                if (isOpen) {
                    unlockScreen.setVisibility(View.INVISIBLE);
                    lockTextOne.setVisibility(View.INVISIBLE);
                    lockTextTwo.setVisibility(View.INVISIBLE);
                    isOpen = false;
                } else {
                    unlockScreen.setVisibility(View.VISIBLE);
                    lockTextOne.setVisibility(View.VISIBLE);
                    lockTextTwo.setVisibility(View.VISIBLE);
                    isOpen = true;
                }
                break;

            case R.id.unlockScreenLayout:
                videoPlayerLockScreen.setVisibility(View.GONE);
                videoSeekBar.setVisibility(View.VISIBLE);
                showDefaultControls();
                break;

            case R.id.back:
                onBackPressed();
                break;

            case R.id.goToPrevious5:
                videoView.seekTo(videoView.getCurrentPosition() - 5000);
                break;

            case R.id.goToForward5:
                videoView.seekTo(videoView.getCurrentPosition() + 5000);
                break;

            case R.id.videoViewPlayPauseBtn:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    isPause = true;
                    playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow));
                } else {
                    videoView.start();
                    isPause = false;
                    playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
                }
                break;
        }
    }

    private class GestureDetector extends android.view.GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isEnable) {
                hideDefaultControls();
                isEnable = false;
            } else {
                showDefaultControls();
                isEnable = true;
            }
            return super.onSingleTapConfirmed(e);
        }

    }

    private void hideDefaultControls() {
        durationLayout.setVisibility(View.GONE);
        videoPlayerToolBar.setVisibility(View.GONE);
        videoPlayerFooter.setVisibility(View.GONE);

        //this function will hide status and navigation when we click on screen
        final Window window = this.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= 19) {
                uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(uiOption);
        }
    }

    private void showDefaultControls() {
        durationLayout.setVisibility(View.VISIBLE);
        videoPlayerToolBar.setVisibility(View.VISIBLE);
        videoPlayerFooter.setVisibility(View.VISIBLE);

        //this function will show status and navigation when we click on screen
        final Window window = this.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        final View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            uiOption &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
            uiOption &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= 19) {
                uiOption &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(uiOption);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                hideDefaultControls();
                if (scale > MIN_ZOOM) {
                    mode = Mode.DRAG;
                    startX = event.getX() - prevDx;
                    startY = event.getY() - prevDy;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                hideDefaultControls();
                isEnable = false;
                if (mode == Mode.DRAG) {
                    dx = event.getX() - startX;
                    dy = event.getY() - startY;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = Mode.ZOOM;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = Mode.DRAG;
                break;
            case MotionEvent.ACTION_UP:
                mode = Mode.NONE;
                prevDx = dx;
                prevDy = dy;
                break;
        }
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
            zoomLayout.requestDisallowInterceptTouchEvent(true);
            float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
            float maxDy = (child().getHeight() - (child().getHeight() / scale)) / 2 * scale;
            dx = Math.min(Math.max(dx, -maxDx), maxDx);
            dy = Math.min(Math.max(dy, -maxDy), maxDy);
            applyScaleAndTranslation();
        }
        return true;
    }

    private void applyScaleAndTranslation() {
        child().setScaleX(scale);
        child().setScaleY(scale);
        child().setTranslationX(dx);
        child().setTranslationY(dy);
    }

    private View child() {
        return zoomLayout();
    }

    private View zoomLayout() {
        return videoView;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = scaleDetector.getScaleFactor();
        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            scale *= scaleFactor;
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            lastScaleFactor = scaleFactor;
        } else {
            lastScaleFactor = 0;
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    private void setVolumeControl(MediaPlayer mp) {
        muteOrNot.setOnClickListener(v -> {
            if (isMute) {
                Log.d("TAG", "setVolume OFF");
                muteOrNot.setImageResource(R.drawable.ic_baseline_volume_off);
                mp.setVolume(0F, 0F);
            } else {
                Log.d("TAG", "setVolume ON");
                muteOrNot.setImageResource(R.drawable.ic_baseline_volume_up);
                mp.setVolume(1F, 1F);
            }
            isMute = !isMute;
        });
    }

}