package com.team_pyl.dashstreaming3;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    Button btnGet;                          // "Get" button in XML
    EditText textUrl;                       // "URL" edit text in XML
    SimpleExoPlayerView playerView;         // Player view in XML
    SimpleExoPlayer player;                 // player
    DataSource.Factory dataSourceFactory;   // data source factory
    DashMediaSource dashMediaSource;        // media source
    static String URL = "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd";
    // Sample video source

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGet = (Button) findViewById(R.id.btn_get);
        textUrl = (EditText) findViewById(R.id.text_url);
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        setListener();

    }

    // Work: Set listener
    public void setListener() {
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo(textUrl.getText().toString());
            }
        });
    }

    // Work: Play video
    public void playVideo(String url) {
        url = URL;
        // 일단 지금은 샘플 영상이 재생되도록 해놓았으나, 위 줄을 지우면 url text edit 에서 받아와서
        // 재생하게 되어있음

        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayer"));
        Uri uri = Uri.parse(URL);
        // 영상 source 설정
        dashMediaSource = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);

        // bandwidth
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));

        // player new instance 생성
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        // player view 와 player 객체 연결
        playerView.setPlayer(player);
        // 영상 준비 및 재생
        player.prepare(dashMediaSource);
    }
}
