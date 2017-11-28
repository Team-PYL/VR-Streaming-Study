package com.team_pyl.dashstreaming3;

import android.widget.MediaController;

import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by sang on 2017-11-27.
 */

public class PlayerControl implements MediaController.MediaPlayerControl {
    private final ExoPlayer exoPlayer;

    public PlayerControl(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferPercentage() {
        return exoPlayer.getBufferedPercentage();
    }

    @Override
    public int getCurrentPosition() {
        return exoPlayer.getDuration() == com.google.android.exoplayer2.C.TIME_UNSET ? 0
                : (int) exoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return exoPlayer.getDuration() == com.google.android.exoplayer2.C.TIME_UNSET ? 0
                : (int) exoPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void start() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(int timeMillis) {
        long seekPosition = exoPlayer.getDuration() == com.google.android.exoplayer2.C.TIME_UNSET ? 0
                : Math.min(Math.max(0, timeMillis), getDuration());
        exoPlayer.seekTo(seekPosition);
    }

}