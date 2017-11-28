package com.team_pyl.gyrodash;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /* Gyroscope */
    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;
    CreateJsonFile CreateJson = new CreateJsonFile();

    //Using the Gyroscope
    private SensorEventListener mGyroLis;
    private Sensor mGgyroSensor = null;

    private int isPushed = 0;
    private int isFirst = 1;

    String startJson = "[";
    String endJson = "]";
    String gyro_data = "";

    //Roll and Pitch
    private double pitch;
    private double roll;
    private double yaw;

    private double firstPitch;
    private double firstRoll;
    private double firstYaw;

    //timestamp and dt
    private double timestamp;
    private double dt;

    // for radian -> dgree
    private double RAD2DGR = 180 / Math.PI;
    private static final float NS2S = 1.0f / 1000000000.0f;

    private final String url = "";//사용할 서버 url 넣기

    public static String TAGGYRO = "MainLog";
    public static double FILE_NAME;



    /* Dash Streaming */
    /**
     * Preserve the video's state and duration when rotating the phone. This improves
     * performance when rotating or reloading the video.
     */
    public static String TAG = "MainVR";
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_VIDEO_DURATION = "videoDuration";
    private static final String STATE_PROGRESS_TIME = "progressTime";


    /**
     * The video view and its custom UI elements.
     */
    private VrVideoView videoWidgetView;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private SeekBar seekBar;
    private TextView statusText;
    public LogThread thrd = new LogThread();
    /**
     * By default, the video will start playing as soon as it is loaded.
     */
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Dash Streaming */
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        statusText = (TextView) findViewById(R.id.status_text);
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);

        initWidgets();

        // initialize based on the saved state
        if (savedInstanceState != null) {
            long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
            videoWidgetView.seekTo(progressTime);
            seekBar.setMax((int) savedInstanceState.getLong(STATE_VIDEO_DURATION));
            seekBar.setProgress((int) progressTime);

            isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
            if (isPaused) {
                videoWidgetView.pauseVideo();
            }
        } else {
            seekBar.setEnabled(false);
        }

        /* External storage */
        //external storage가 유효한지 확인
        if (CreateJson.isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(), "외부 저장장치가 유효합니다", Toast.LENGTH_SHORT).show();
            if (CreateJson.isExternalStorageReadWrite()) {
                Toast.makeText(getApplicationContext(), "읽고 쓸 수 있습니다", Toast.LENGTH_SHORT).show();
                // getFreeSpace() / getTotalSpace()

            } else {
                Toast.makeText(getApplicationContext(), "읽고 쓸 수 없습니다", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "외부 저장장치가 유효하지 않습니다", Toast.LENGTH_SHORT).show();
        }

        /* Gyroscope */
        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mGgyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGyroLis = new GyroscopeListener();
        mSensorManager.registerListener(mGyroLis, mGgyroSensor, SensorManager.SENSOR_DELAY_UI);

    }

    private void initWidgets() {
        // initialize the seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // if the user changed the position, seek to the new position.
                if (fromUser) {
                    videoWidgetView.seekTo(progress);
                    updateStatusText();

                }
//                Log.w(TAG, " onProgressChanged");
                thrd.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ignore for now.
                Log.w(TAG, " onStartTrackingTouch");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ignore for now.
                Log.w(TAG, " onStopTrackingTouch");

            }
        });

        // initialize the video listener
        videoWidgetView.setEventListener(new VrVideoEventListener() {
            /**
             * Called by video widget on the UI thread when it's done loading the video.
             */
            @Override
            public void onLoadSuccess() {
                Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
                seekBar.setMax((int) videoWidgetView.getDuration());
                seekBar.setEnabled(true);
                updateStatusText();
            }

            /**
             * Called by video widget on the UI thread on any asynchronous error.
             */
            @Override
            public void onLoadError(String errorMessage) {
                Toast.makeText(
                        getApplicationContext(), "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                        .show();
                Log.e(TAG, "Error loading video: " + errorMessage);
            }

            @Override
            public void onClick() {
                if (isPaused) {
                    videoWidgetView.playVideo();
                    Log.w(TAG, " play");

                } else {
                    videoWidgetView.pauseVideo();
                    Log.w(TAG, " paused");

                }

                isPaused = !isPaused;
                updateStatusText();
            }

            /**
             * Update the UI every frame.
             */
            @Override
            public void onNewFrame() {
                updateStatusText();
                seekBar.setProgress((int) videoWidgetView.getCurrentPosition());

            }

            /**
             * Make the video play in a loop. This method could also be used to move to the next video in
             * a playlist.
             */
            @Override
            public void onCompletion() {
                videoWidgetView.seekTo(0);
                Log.w(TAG, " onCompletion");

                //여기서 데이터를 보낸다.
                gyro_data = startJson+gyro_data+endJson;
                Log.e(TAGGYRO,"LAST  "+gyro_data);

                try {
                    getPosts(new JSONObject(gyro_data));//서버에 post하기 위한 함수 호출

                }catch (Exception e){
                    Log.w(TAGGYRO, "getting POST function error", e);
                }
                //데이터 초기화
                CreateJson.writeFile(CreateJson.getFileName(), gyro_data);
                gyro_data = "";

//                CreateJson.read("1511896959704.json");
//            1511896959704.txt
//            1511886866616.json
            }

        });
    }

    private void updateStatusText() {
        String status = (isPaused ? "Paused: " : "Playing: ") +
                String.format(Locale.getDefault(), "%.2f", videoWidgetView.getCurrentPosition() / 1000f) +
                " / " +
                videoWidgetView.getDuration() / 1000f +
                " seconds.";
        statusText.setText(status);


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
        Log.w(TAG, " onSaveInstanceState");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w(TAG, " onPause");
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video was playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
        mSensorManager.unregisterListener(mGyroLis);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, " onResume");

        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();

        if (videoWidgetView.getDuration() <= 0) {
            try {
                videoWidgetView.loadVideo(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"), new VrVideoView.Options());
            } catch (IOException e) {

                Log.w(TAG, "onResume - IOException");
            }
//            videoWidgetView.loadVideoFromAsset("congo_2048.mp4", new VrVideoView.Options());
        }
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, " onDestroy");
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();
        mSensorManager.unregisterListener(mGyroLis);

    }

    private class GyroscopeListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            /* 각 축의 각속도 성분을 받는다. */
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

            /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
             * dt : 센서가 현재 상태를 감지하는 시간 간격
             * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            /* 맨 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
            if (dt - timestamp * NS2S != 0) {

                /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
                 * 여기까지의 pitch, roll의 단위는 '라디안'이다.
                 * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */
                pitch = pitch + gyroY * dt;
                roll = roll + gyroX * dt;
                yaw = yaw + gyroZ * dt;

                //만약 처음 버튼을 눌러 새로 정보를 받는 일을 해야 한다면
                //첫번째 변수를 저장해 둔다.
                if (isFirst == 1) {
                    firstPitch = pitch;
                    firstRoll = roll;
                    firstYaw = yaw;
                    isFirst = 0;
                }

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    class LogThread extends Thread {
        public void run() {
            try {
                Thread.sleep(50);
                // 안하면 조금 버벅대는감이 없잖아 있고, 어차피 사람이 움직이는 속도는 한정적이므로
                // 어느정도의 sleep을 줘도 괜찮을것 같아서
                String currentTime = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

                Log.w(TAGGYRO, "[TimeStamp]: "+videoWidgetView.getCurrentPosition()
                        + "           [Date]: " + currentTime
                        + "           [Pitch]: " + String.format("%.1f", (pitch * RAD2DGR) - (firstPitch * RAD2DGR))
                        + "           [Roll]: " + String.format("%.1f", (roll * RAD2DGR) - (firstRoll * RAD2DGR))
                        + "           [Yaw]: " + String.format("%.1f", (yaw * RAD2DGR) - (firstYaw * RAD2DGR)));
                if (!gyro_data.toString().equals("")) {
                    gyro_data = gyro_data + ",";
                }
//                gyro_data = gyro_data + "{\"date\"" + ":" + "\"" + currentTime + "\"" + "," + "\"pitch\"" + ":" + "\"" + String.format("%.1f", (pitch * RAD2DGR) - (firstPitch * RAD2DGR)) + "\"" + "," + "\"roll\"" + ":" + "\"" + String.format("%.1f", (roll * RAD2DGR) - (firstRoll * RAD2DGR)) + "\"" + "," + "\"yaw\"" + ":" + "\"" + String.format("%.1f", (yaw * RAD2DGR) - (firstYaw * RAD2DGR)) + "\"" + "}";
                gyro_data = gyro_data + "{\"TS\"" + ":" + "\"" + videoWidgetView.getCurrentPosition() + "\"" + "," + "\"date\"" + ":" + "\"" + currentTime + "\"" + "," + "\"pitch\"" + ":" + "\"" + String.format("%.1f", (pitch * RAD2DGR) - (firstPitch * RAD2DGR)) + "\"" + "," + "\"roll\"" + ":" + "\"" + String.format("%.1f", (roll * RAD2DGR) - (firstRoll * RAD2DGR)) + "\"" + "," + "\"yaw\"" + ":" + "\"" + String.format("%.1f", (yaw * RAD2DGR) - (firstYaw * RAD2DGR)) + "\"" + "}";



            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* Connection */
    /*
    목적: TCP connection을 맺기 위해 사용되는 함수
     */
    private HttpURLConnection getConnection(String method, String path) {
        try {
            URL urla = new URL(url + path);
            HttpURLConnection con = (HttpURLConnection) urla.openConnection();

            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "application/json");

            System.out.println("\nSending '" +  "' request to: " + urla.toString());
            return con;

        } catch (Exception e) {
            return null;
        }
    }
    /*
    목적: 데이터를 보내기 위해 사용되는 함수
     */
    private void sendJson(HttpURLConnection con, JSONObject json) {
        try {
            OutputStream out = con.getOutputStream();
            out.write(json.toString().getBytes());
            out.flush();
        } catch (Exception e) {
            Log.w("TAG", "sending error to server with data", e);

        }
    }
    /*
    목적: 서버에 post하기 위한 함수로 TCP connection과 데이터 전송을 수행
     */
    public void getPosts(final JSONObject obj) {
        new Thread() {
            @Override
            public void run() {
                HttpURLConnection con = getConnection("GET", "/posts"); //url-API문서를 위한

                System.out.println(obj.toString());
                sendJson(con, obj);//데이터를 보내는 함수

                int responseCode = 0;
                try { responseCode = con.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Got response!");

                // TODO Handle not OK response
                //서버와 통신이 원활한지에 대한 정보를 받고 그에 따라 if문을 수행하다.
                if (responseCode == HttpURLConnection.HTTP_OK) {

                }
            }
        }.start();
    }

}
