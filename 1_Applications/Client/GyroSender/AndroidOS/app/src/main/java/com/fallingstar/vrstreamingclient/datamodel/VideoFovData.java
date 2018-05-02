package com.fallingstar.vrstreamingclient.datamodel;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoFovData {
    String uuid;
    String video_codec;
    String audio_codec;
    int video_duration;
    HashMap<Integer, String> FoV_arr;

    public VideoFovData(HashMap<String, Object> params){
        this.uuid = (String)params.get("uuid");
        this.video_codec = (String)params.get("video_codec");
        this.audio_codec = (String)params.get("audio_codec");
        this.video_duration = (int)params.get("video_duration");
        this.FoV_arr = (HashMap<Integer, String>) params.get("fov_arr");
    }
}
