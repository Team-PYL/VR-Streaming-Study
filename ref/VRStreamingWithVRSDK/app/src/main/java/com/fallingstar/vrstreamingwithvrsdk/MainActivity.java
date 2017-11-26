package com.fallingstar.vrstreamingwithvrsdk;

import android.content.Context;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_VIDEO_DURATION = "videoDuration";
    private static final String STATE_PROGRESS_TIME = "progressTime";

    private VrVideoView videoWidgetView;

    private SeekBar seekBar;
    private TextView statusText;

    private boolean isPaused = false;

    private Context currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentActivity = this.getApplicationContext();
        initWidgets();
        if (savedInstanceState != null){
            long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
            videoWidgetView.seekTo(progressTime);
            seekBar.setMax((int)savedInstanceState.getLong(STATE_VIDEO_DURATION));
            seekBar.setProgress((int)progressTime);

            isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
            if (isPaused){
                videoWidgetView.pauseVideo();
            }
        }else {
            seekBar.setEnabled(false);
        }
    }

    private void initWidgets(){
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        statusText = (TextView)findViewById(R.id.status_text);
        videoWidgetView = (VrVideoView)findViewById(R.id.video_view);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    videoWidgetView.seekTo(progress);
                    updateStatusText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        videoWidgetView.setEventListener(new VrVideoEventListener(){
            @Override
            public void onLoadSuccess() {
                super.onLoadSuccess();
                Log.i("YSTAG", "Successfully loaded video " + videoWidgetView.getDuration());
                seekBar.setMax((int)videoWidgetView.getDuration());
                seekBar.setEnabled(true);
                updateStatusText();
            }

            @Override
            public void onLoadError(String errorMessage) {
                super.onLoadError(errorMessage);
                Toast.makeText(currentActivity, "Error loading video: "+errorMessage, Toast.LENGTH_LONG).show();
                Log.e("YSTAG", "Error loading video : "+errorMessage);
            }

            @Override
            public void onClick() {
                super.onClick();
                if (isPaused){
                    videoWidgetView.playVideo();
                }else{
                    videoWidgetView.pauseVideo();
                }
                isPaused = !isPaused;
                updateStatusText();
            }

            @Override
            public void onNewFrame() {
                super.onNewFrame();
                updateStatusText();
                seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
            }

            @Override
            public void onCompletion() {
                super.onCompletion();
                videoWidgetView.seekTo(0);
            }
        });
    }
    private void updateStatusText() {
        String status = (isPaused ? "Paused: " : "Playing: ") +
                String.format(Locale.getDefault(), "%.2f", videoWidgetView.getCurrentPosition()/1000f) +
                " / " +
                videoWidgetView.getDuration() / 1000f +
                " seconds.";
        statusText.setText(status);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        outState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        outState.putBoolean(STATE_IS_PAUSED, isPaused);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoWidgetView.pauseRendering();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoWidgetView.resumeRendering();
        updateStatusText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoWidgetView.shutdown();
    }
}
