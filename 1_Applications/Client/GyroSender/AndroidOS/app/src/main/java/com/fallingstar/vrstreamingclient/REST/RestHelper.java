package com.fallingstar.vrstreamingclient.REST;

import android.os.AsyncTask;
import android.util.Log;

import com.fallingstar.vrstreamingclient.datamodel.DeviceData;
import com.fallingstar.vrstreamingclient.datamodel.VideoFovData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by redsnow on 2018. 4. 2..
 */

public class RestHelper {
    static Retrofit retrofit;
    static RestHelperInterface restHelper;

    public RestHelper(String base_url) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(base_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }

    public static int postDeviceData(String device_uuid, DeviceData device_data) {
        return 0;
    }

    public static int postFovData(String device_uuid, VideoFovData fov_data) {
        final String uuid = device_uuid;
        final VideoFovData data = fov_data;

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                restHelper = retrofit.create(RestHelperInterface.class);
                Call<JsonObject> call = restHelper.postFovData(uuid, data);

                try {
                    return call.execute().body().toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.d("REST RESPONSE", s);
            }
        }.execute();

        return 0;
    }
}
