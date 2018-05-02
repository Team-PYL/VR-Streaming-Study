package com.team_pyl.gyrodash;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by redsnow on 2017. 12. 1..
 */

public class NetworkServiceManager {
<<<<<<< HEAD
    private String SERVER_URL = "http://192.168.10.3:5000/data/create";
=======
    private String DATA_RELOAD = "/data/reload";
    private String DATA_SEND = "/data/create";
    private String SERVER_URL = "http://starbox.iptime.org:5000";
>>>>>>> 30637e51c948d40258e7dba5f3c472210650d9b1

    /* Connection */
    /*
    목적: TCP connection을 맺기 위해 사용되는 함수
     */
    private HttpURLConnection getConnection(String method, String path) {
        try {
            URL urla = new URL(SERVER_URL + path);
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
    private void sendJson(HttpURLConnection myConnection, JSONObject obj) {
        try {
            myConnection.getOutputStream().write(obj.toString().getBytes());
            myConnection.getOutputStream().flush();
        } catch (Exception e) {
            Log.w("TAG", "sending error : "+ e.getLocalizedMessage());
        }
    }

    void sendData(final JSONObject obj){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Networking task should be here
                try{
                    URL hostURL = new URL(SERVER_URL+DATA_SEND);
                    HttpURLConnection myConnection = (HttpURLConnection)hostURL.openConnection();
                    myConnection.setRequestProperty("User-Agent", "Viewer-v0.1");
                    myConnection.setRequestMethod("POST");
                    myConnection.setDoOutput(true);
                    myConnection.setDoInput(true);
                    myConnection.setRequestProperty("Content-Type", "application/json");

                    sendJson(myConnection, obj);

                    if (myConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader in = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                        String inputLine;
                        StringBuffer tempResponse = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            tempResponse.append(inputLine);
                        }
                        in.close();
                        String response = tempResponse.toString();
                        Log.d("YSTAG", response);
                    }else{
                        Log.d("YSTAG", "Error while POSTing!");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    void clearData(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Networking task should be here
                try{
                    URL hostURL = new URL(SERVER_URL+DATA_RELOAD);
                    HttpURLConnection myConnection = (HttpURLConnection)hostURL.openConnection();
                    myConnection.setRequestProperty("User-Agent", "Viewer-v0.1");
                    myConnection.setRequestMethod("POST");
                    myConnection.setDoOutput(true);
                    myConnection.setDoInput(true);
                    myConnection.setRequestProperty("Content-Type", "application/json");

                    if (myConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader in = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                        String inputLine;
                        StringBuffer tempResponse = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            tempResponse.append(inputLine);
                        }
                        in.close();
                        String response = tempResponse.toString();
                        Log.d("YSTAG", response);
                    }else{
                        Log.d("YSTAG", "Error while clear!");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
