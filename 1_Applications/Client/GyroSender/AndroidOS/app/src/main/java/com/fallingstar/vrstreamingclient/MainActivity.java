package com.fallingstar.vrstreamingclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fallingstar.vrstreamingclient.REST.RestHelper;
import com.fallingstar.vrstreamingclient.datamodel.VideoFovData;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.google.vr.sdk.widgets.video.VrVideoView.Options;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int isPushed = 1;
    private int isFirst = 1;

    private double timestamp;
    private double dt;

    public static double FILE_NAME;
    private float[] yawAndPitch = new float[2];


    /* Variables for Streaming(Playing) VR Videos */
    private static String TAG = "MainActivity";
    private static String TAG_GYRO = "GYRO DATA";
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_VIDEO_DURATION = "videoDuration";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    private boolean isPaused = false;

    /* Widgets for Streaming(Playing) VR Videos */
    private VrVideoView videoViewWidget;
    private SeekBar seekBar;
    private TextView statusText;

    /* Thread instance that logging the gyro(FoV) data */
    private GyroLogThread gyroThread = new GyroLogThread();

    /* Data to send to the server */
    private String json = "";
    private HashMap<Integer, String> FoVMap = new HashMap<>();
    private RestHelper restHelper = new RestHelper("http://192.168.0.106:5000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
        loadSavedInstanceState(savedInstanceState);
        gyroThread.start();
    }

    private void loadSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
            videoViewWidget.seekTo(progressTime);
            seekBar.setMax((int)savedInstanceState.getLong(STATE_VIDEO_DURATION));
            seekBar.setProgress((int)progressTime);

            isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
            if (isPaused) {
                videoViewWidget.pauseVideo();
            }
        }else{
            seekBar.setEnabled(false);
        }
    }
    private int checkExternalStorage() {
        //TODO: Implement the checkExternalStorage function

        return 0;
    }

    private void initWidgets() {
        videoViewWidget = findViewById(R.id.vrVideoView_main);
        seekBar = findViewById(R.id.skBar_duration);
        statusText = findViewById(R.id.txt_status);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean changedByUser) {
                if (changedByUser) {
                    videoViewWidget.seekTo(progress);
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
        videoViewWidget.setEventListener(new VrVideoEventListener() {
            @Override
            public void onLoadSuccess() {
                super.onLoadSuccess();
                Log.i(TAG, "Successfully loaded video " + videoViewWidget.getDuration());
                seekBar.setMax((int)videoViewWidget.getDuration());
                seekBar.setEnabled(true);
                updateStatusText();
            }

            @Override
            public void onLoadError(String errorMessage) {
                super.onLoadError(errorMessage);
                Toast.makeText(MainActivity.this, "Error while loading video", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error while loading video : " + errorMessage);
            }

            @Override
            public void onClick() {
                super.onClick();
                if (isPaused) {
                    videoViewWidget.playVideo();
                    Log.w(TAG, "Video playing");
                }else{
                    videoViewWidget.pauseVideo();
                    Log.w(TAG, "Video paused");
                }

                isPaused = !isPaused;
                updateStatusText();
            }

            @Override
            public void onNewFrame() {
                super.onNewFrame();
                updateStatusText();
                seekBar.setProgress((int)videoViewWidget.getCurrentPosition());
            }

            @Override
            public void onCompletion() {
                super.onCompletion();
                Log.w(TAG, "Video Ended");

                HashMap<String, Object> req_body = new HashMap<>();
                req_body.put("uuid", "asdf1234");
                req_body.put("video_codec", "MPEG");
                req_body.put("audio_codec", "MP3");
                req_body.put("video_duration", (int)videoViewWidget.getDuration());
                req_body.put("fov_arr", FoVMap);

                for (Integer key : FoVMap.keySet()){
                    Log.d(TAG, FoVMap.get(key));
                }

                VideoFovData fov_data = new VideoFovData(req_body);

                RestHelper.postFovData("asdf1234", fov_data);

                FoVMap.clear();
                videoViewWidget.pauseVideo();
                isPaused = !isPaused;
                updateStatusText();
            }
        });
    }
    private void updateStatusText() {
        String status;

        if (isPaused){
            status = "Paused: ";
        }else {
            status = "Playing: ";
        }
        status += String.format(Locale.getDefault(), "%.2f", videoViewWidget.getCurrentPosition() / 1000f) +
                " / " +
                videoViewWidget.getDuration() / 1000f +
                " seconds.";
        statusText.setText(status);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_PROGRESS_TIME, videoViewWidget.getCurrentPosition());
        outState.putLong(STATE_VIDEO_DURATION, videoViewWidget.getDuration());
        outState.putBoolean(STATE_IS_PAUSED, isPaused);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "onPause");
        videoViewWidget.pauseVideo();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
        videoViewWidget.resumeRendering();
        if (videoViewWidget.getDuration() <= 0){
            try{
                Options options = new Options();
                options.inputType = Options.TYPE_STEREO_OVER_UNDER;
                options.inputFormat = Options.FORMAT_DEFAULT;
                videoViewWidget.loadVideoFromAsset("congo_2048.mp4", options);
            }catch (IOException e){
                Log.e(TAG, "onResume-IOException : "+e.getLocalizedMessage());
            }
        }
        updateStatusText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestory");
        videoViewWidget.pauseRendering();
        videoViewWidget.shutdown();
    }

    private class GyroLogThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true){
                if (!isPaused) {
                    try {
                        sleep(500);

                        String currentTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
                        long currentPosition = videoViewWidget.getCurrentPosition();
                        String logMsg;
                        float yaw, pitch;

                        videoViewWidget.getHeadRotation(yawAndPitch);
                        yaw = yawAndPitch[0];
                        pitch = yawAndPitch[1];

                        logMsg = "[TimeStamp] : "+currentPosition
                                +  "     [Date] : "+currentTime
                                +  "      [Yaw] : "+yawAndPitch[0]
                                +  "    [Pitch] : "+yawAndPitch[1];

                        FoVMap.put((int)currentPosition, "{" +
                                "pos : "+currentPosition+", " +
                                "yaw : "+yaw+", " +
                                "pitch : "+pitch+", " +
                                "date : "+currentTime+
                                "}");

//                        Log.d(TAG, logMsg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}