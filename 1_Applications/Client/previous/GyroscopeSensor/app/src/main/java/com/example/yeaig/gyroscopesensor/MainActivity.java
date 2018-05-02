package com.example.yeaig.gyroscopesensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

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
                        //버튼이 처음 눌렸을 때
                        //데이터를 받는다
                        if (isPushed == 0) {
                            mSensorManager.registerListener(mGyroLis, mGgyroSensor, SensorManager.SENSOR_DELAY_UI);
                            Log.e("LOG", "now push");
                            isPushed = 1;
                        }
                        //다음으로 버튼을 눌렀을 때
                        //데이터 받기를 멈춘다
                        else if (isPushed == 1) {
                            Log.e("LOG", "now not push");
                            mSensorManager.unregisterListener(mGyroLis);
                            isPushed = 0;
                            isFirst=1;
                            //여기서 데이터를 보낸다.
                            gyro_data = startJson+gyro_data+endJson;
                            Log.e("LOG","LAST  "+gyro_data);

                            try {
                                getPosts(new JSONObject(gyro_data));//서버에 post하기 위한 함수 호출

                            }catch (Exception e){
                                Log.w("TAG", "getting POST function error", e);
                            }
                            //데이터 초기화
                            gyro_data = "";
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
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

            /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
             * dt : 센서가 현재 상태를 감지하는 시간 간격
             * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

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

                Log.e("LOG", "[Date]: " + currentTime
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
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
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