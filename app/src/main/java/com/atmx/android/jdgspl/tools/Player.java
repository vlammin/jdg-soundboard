/*
 * Player
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.tools;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.atmx.android.jdgspl.dialogs.PlayerDialog;
import com.atmx.android.jdgspl.events.PlayerFfwEvent;
import com.atmx.android.jdgspl.events.PlayerProgressEvent;
import com.atmx.android.jdgspl.events.PlayerRewEvent;
import com.atmx.android.jdgspl.events.PlayerStopEvent;
import com.atmx.android.jdgspl.events.PlayerTrackingEvent;
import com.atmx.android.jdgspl.objects.Sound;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

public class Player {

    private static final int MILLISECONDS_GAP = 3000;

    private static Player instance;
    private final RefreshHandler refreshHandler = new RefreshHandler(this);
    private MediaPlayer mediaPlayer;
    private PlayerDialog playerDialog;

    public static synchronized Player getInstance() {
        if (instance == null)
            instance = new Player();

        return instance;
    }

    public void play(AppCompatActivity activity, Sound sound) {
        release();
        mediaPlayer = MediaPlayer.create(activity, sound.getResourceId());

        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    release();
                }
            });

            if (sound.isMusic()) {
                registerEventBus();
                showPlayerDialog(activity, sound);
            }
        }
    }

    private void changePosition(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            refreshPlayerDialog();
        }
    }

    private void fastForward() {
        if (mediaPlayer != null) {
            int pos = mediaPlayer.getCurrentPosition();
            pos += MILLISECONDS_GAP;
            mediaPlayer.seekTo(pos);
            refreshPlayerDialog();
        }
    }

    private void rewind() {
        if (mediaPlayer != null) {
            int pos = mediaPlayer.getCurrentPosition();
            pos -= MILLISECONDS_GAP;
            mediaPlayer.seekTo(pos);
            refreshPlayerDialog();
        }
    }

    private void showPlayerDialog(AppCompatActivity activity, Sound sound) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("sound", sound);
        bundle.putInt("duration", mediaPlayer.getDuration());

        playerDialog = new PlayerDialog();
        playerDialog.setArguments(bundle);
        playerDialog.show(activity.getSupportFragmentManager(), "player_dialog");

        enableRefresh();
    }

    public void refreshPlayerDialog() {
        if (playerDialog != null && mediaPlayer != null) {
            int position = mediaPlayer.getCurrentPosition();
            playerDialog.setCurrentTime(position);
            playerDialog.setProgress(position);
        }
    }

    public void replacePlayerDialog(PlayerDialog playerDialog) {
        this.playerDialog = playerDialog;
    }

    public void release() {
        if (playerDialog != null) {
            playerDialog.dismissAllowingStateLoss();
            playerDialog = null;
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        unregisterEventBus();
    }

    private void disableRefresh() {
        refreshHandler.removeMessages(RefreshHandler.REFRESH_DIALOG);
    }

    private void enableRefresh() {
        refreshHandler.sendEmptyMessage(RefreshHandler.REFRESH_DIALOG);
    }

    /**
     * Event handling
     **/

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayerFfwEvent event) {
        fastForward();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayerProgressEvent event) {
        changePosition(event.getProgress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayerRewEvent event) {
        rewind();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayerStopEvent event) {
        release();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayerTrackingEvent event) {
        if (event.isTracking())
            disableRefresh();
        else
            enableRefresh();
    }

    /**
     * Custom Handler
     **/

    private static class RefreshHandler extends Handler {
        private static final int REFRESH_DIALOG = 10;
        private static final int INTERVAL_MS = 500;

        private final WeakReference<Player> weakReference;

        RefreshHandler(Player myClassInstance) {
            weakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            Player player = weakReference.get();
            if (player != null) {
                if (player.mediaPlayer != null && player.mediaPlayer.isPlaying()) {
                    player.refreshPlayerDialog();
                    int extraTime = player.mediaPlayer.getCurrentPosition() % INTERVAL_MS;
                    sendEmptyMessageDelayed(REFRESH_DIALOG, INTERVAL_MS - extraTime);
                }
            }
        }
    }
}
