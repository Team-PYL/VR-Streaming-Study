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
    private String SERVER_URL = "http://192.9.88.238:5000/data/create";

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
                HttpURLConnection con = getConnection("POST", SERVER_URL+"/data/create"); //url-API문서를 위한

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

    public void sendData(final JSONObject obj){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Networking task should be here
                try{
                    URL hostURL = new URL(SERVER_URL);
                    HttpURLConnection myConnection = (HttpURLConnection)hostURL.openConnection();
                    myConnection.setRequestProperty("User-Agent", "Viewer-v0.1");
                    myConnection.setRequestMethod("POST");
                    myConnection.setDoOutput(true);
                    myConnection.setDoInput(true);
                    myConnection.setRequestProperty("Content-Type", "application/json");

                    myConnection.getOutputStream().write(obj.toString().getBytes());
                    myConnection.getOutputStream().flush();

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

    /*
       Build the network service to send the json data.
     */
    public void buildNetworkService(String ip, int port){

    }
}
