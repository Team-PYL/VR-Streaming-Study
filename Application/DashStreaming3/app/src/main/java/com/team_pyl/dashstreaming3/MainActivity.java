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

    Button btnGet;
    EditText textUrl;
    SimpleExoPlayerView playerView;
    SimpleExoPlayer player;
    DataSource.Factory dataSourceFactory;
    DashMediaSource dashMediaSource;
    static String URL = "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd";
//    "http://sites.google.com/site/ubiaccessmobile/sample_video.mp4"
//    "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGet = (Button) findViewById(R.id.btn_get);
        textUrl = (EditText) findViewById(R.id.text_url);
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        setListener();

//
//        simpleExoPlayerView = new SimpleExoPlayerView(getApplicationContext());
//
//        // 1. Create a default TrackSelector
//        Handler mainHandler = new Handler();
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory =
//                new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector =
//                new DefaultTrackSelector(videoTrackSelectionFactory);
//
//        // 2. Create the player
//        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
//
//        // Bind the player to the view.
//        simpleExoPlayerView.setPlayer(player);


    }

    public void setListener() {
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo(textUrl.getText().toString());
            }
        });
    }

    public void playVideo(String url) {
        url = URL;

        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayer"));
        Uri uri = Uri.parse(URL);
        dashMediaSource = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        playerView.setPlayer(player);
        player.prepare(dashMediaSource);
    }
}
