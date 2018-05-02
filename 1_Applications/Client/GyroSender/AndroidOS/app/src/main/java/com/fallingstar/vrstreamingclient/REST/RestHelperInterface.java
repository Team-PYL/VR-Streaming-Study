package com.fallingstar.vrstreamingclient.REST;

import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.fallingstar.vrstreamingclient.datamodel.VideoFovData;

public interface RestHelperInterface {
    public static final String Base_URL = "http://0.0.0.0:58080";


    @POST("/devices/{ID}")
    Call<JsonObject> postDeviceData(@Path("ID") String id);

    @POST("/devices/{ID}/fov")
    Call<JsonObject> postFovData(@Path("ID") String id, @Body VideoFovData data);

}
