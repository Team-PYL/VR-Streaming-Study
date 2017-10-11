package com.example.yeaig.proto;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Gyroscope
    private SensorEventListener mGyroLis;
    private Sensor mGgyroSensor = null;

    private int isPushed = 0;
    private int isFirst = 1;
    //Roll and Pitch
    private double pitch;
    private double roll;
    private double yaw;

    //timestamp and dt
    private double timestamp;
    private double dt;

    // for radian -> dgree
    private double RAD2DGR = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;

    double gyroX=0;
    double gyroY=0;
    double gyroZ=0;

    double gyroX_first=0;
    double gyroY_first=0;
    double gyroZ_first=0;

    double gyroX_now = 0;
    double gyroY_now = 0;
    double gyroZ_now = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mGgyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGyroLis = new GyroscopeListener();

        //Touch Listener for Accelometer
        findViewById(R.id.a_start).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (isPushed == 0) {
                            mSensorManager.registerListener(mGyroLis, mGgyroSensor, SensorManager.SENSOR_DELAY_UI);
                            Log.e("LOG", "now push");
                            isPushed = 1;
                        } else if (isPushed == 1) {
                            Log.e("LOG", "now not push");
                            mSensorManager.unregisterListener(mGyroLis);
                            isPushed = 0;

                        }
                    }

        });

    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e("LOG", "onPause()");
        mSensorManager.unregisterListener(mGyroLis);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        mSensorManager.unregisterListener(mGyroLis);
    }

    private class GyroscopeListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            /* 각 축의 각속도 성분을 받는다. */
            if(isFirst==1) {
                 gyroX_first = event.values[0];
                 gyroY_first = event.values[1];
                 gyroZ_first = event.values[2];

                gyroX =  0;
                gyroY =  0;
                gyroZ =  0;

                isFirst=0;
            }
            else{
                 gyroX_now = event.values[0];
                 gyroY_now = event.values[1];
                 gyroZ_now = event.values[2];

                 gyroX =  gyroX_now-gyroX_first;
                 gyroY =  gyroY_now-gyroY_first;
                 gyroZ =  gyroZ_now-gyroZ_first;
            }
            /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
             * dt : 센서가 현재 상태를 감지하는 시간 간격
             * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            String currentTime = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

            /* 맨 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
            if (dt - timestamp*NS2S != 0) {
     /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
                 * 여기까지의 pitch, roll의 단위는 '라디안'이다.
                 * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */
                pitch = pitch + gyroY*dt;
                roll = roll + gyroX*dt;
                yaw = yaw + gyroZ*dt;

                Log.e("LOG", "GYROSCOPE           [DATE]:" + currentTime
//                        + "           [X]:" + String.format("%.4f", event.values[0])
//                        + "           [Y]:" + String.format("%.4f", event.values[1])
//                        + "           [Z]:" + String.format("%.4f", event.values[2])
                        + "           [Pitch]: " + String.format("%.1f", pitch*RAD2DGR)
                        + "           [Roll]: " + String.format("%.1f", roll*RAD2DGR)
                        + "           [Yaw]: " + String.format("%.1f", yaw*RAD2DGR)
                        + "           [dt]: " + String.format("%.4f", dt));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}