package com.team_pyl.gyrodash;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by sang on 2017-11-29.
 */

public class Logging implements SensorEventListener {

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Gyroscope
    private SensorEventListener mGyroLis;
    private Sensor mGgyroSensor = null;

    private int isPushed = 0;
    private int isFirst = 1;

    String startJson = "[";
    String endJson ="]";
    String gyro_data="";

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
    private static final float NS2S = 1.0f/1000000000.0f;

    private final String url="";//사용할 서버 url 넣기

    public static String TAGGYRO = "MainLog";


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
         /* 각 축의 각속도 성분을 받는다. */
        double gyroX = sensorEvent.values[0];
        double gyroY = sensorEvent.values[1];
        double gyroZ = sensorEvent.values[2];

            /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
             * dt : 센서가 현재 상태를 감지하는 시간 간격
             * NS2S : nano second -> second */
        dt = (sensorEvent.timestamp - timestamp) * NS2S;
        timestamp = sensorEvent.timestamp;

            /* 맨 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
        if (dt - timestamp*NS2S != 0) {

                /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
                 * 여기까지의 pitch, roll의 단위는 '라디안'이다.
                 * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */
            pitch = pitch + gyroY*dt;
            roll = roll + gyroX*dt;
            yaw = yaw + gyroZ*dt;

            //만약 처음 버튼을 눌러 새로 정보를 받는 일을 해야 한다면
            //첫번째 변수를 저장해 둔다.
            if(isFirst==1){
                firstPitch = pitch;
                firstRoll = roll;
                firstYaw = yaw;
                isFirst=0;
            }
            String currentTime = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

            Log.e(TAGGYRO, "[Date]: " + currentTime
                    +"            [Pitch]: " + String.format("%.1f", (pitch*RAD2DGR)-(firstPitch*RAD2DGR))
                    + "           [Roll]: " + String.format("%.1f", (roll*RAD2DGR)-(firstRoll*RAD2DGR))
                    + "           [Yaw]: " + String.format("%.1f", (yaw*RAD2DGR)-(firstYaw*RAD2DGR)));
            if(!gyro_data.toString().equals(""))
            {
                gyro_data = gyro_data+",";
            }
            gyro_data = gyro_data+"{\"date\""+":"+"\""+currentTime+"\""+ "," + "\"pitch\""+":" + "\"" + String.format("%.1f", (pitch*RAD2DGR)-(firstPitch*RAD2DGR)) + "\"" + "," + "\"roll\""+":" + "\"" + String.format("%.1f", (roll*RAD2DGR)-(firstRoll*RAD2DGR)) + "\"" + "," + "\"yaw\""+":" + "\"" + String.format("%.1f", (yaw*RAD2DGR)-(firstYaw*RAD2DGR))+ "\"" + "}";

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
