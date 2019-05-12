/*
 * PlayerDialog
 * JDGSoundboard
 *
 * Copyright (c) 2019 Vincent Lammin
 */

package com.atmx.android.jdgspl.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.atmx.android.jdgspl.R;
import com.atmx.android.jdgspl.events.PlayerFfwEvent;
import com.atmx.android.jdgspl.events.PlayerProgressEvent;
import com.atmx.android.jdgspl.events.PlayerRewEvent;
import com.atmx.android.jdgspl.events.PlayerStopEvent;
import com.atmx.android.jdgspl.events.PlayerTrackingEvent;
import com.atmx.android.jdgspl.objects.Sound;
import com.atmx.android.jdgspl.tools.Utils;

import org.greenrobot.eventbus.EventBus;

public class PlayerDialog extends DialogFragment {

    private SeekBar seekBar;
    private TextView currentTimeView;
    private TextView endTimeView;

    private OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
        boolean isTracking = false;

        public void onStartTrackingTouch(SeekBar bar) {
            isTracking = true;
            EventBus.getDefault().post(new PlayerTrackingEvent(isTracking));
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (!isTracking && fromUser)
                EventBus.getDefault().post(new PlayerProgressEvent(seekBar.getProgress()));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            isTracking = false;
            EventBus.getDefault().post(new PlayerTrackingEvent(isTracking));
            EventBus.getDefault().post(new PlayerProgressEvent(seekBar.getProgress()));
        }
    };

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_player, null);

        AlertDialog dialog = new AlertDialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                EventBus.getDefault().post(new PlayerStopEvent());
            }
        };
        dialog.setView(view);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            Sound sound = bundle.getParcelable("sound");
            int duration = bundle.getInt("duration");

            if (sound != null) {
                dialog.setCanceledOnTouchOutside(false);

                seekBar = view.findViewById(R.id.mediacontroller_progress);
                if (seekBar != null) {
                    SeekBar seeker = seekBar;
                    seeker.setOnSeekBarChangeListener(seekBarListener);
                    seekBar.setMax(1000);
                }

                View btnStop = view.findViewById(R.id.stop);
                btnStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new PlayerStopEvent());
                    }
                });

                View btnFfw = view.findViewById(R.id.ffwd);
                btnFfw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new PlayerFfwEvent());
                    }
                });

                View btnRew = view.findViewById(R.id.rew);
                btnRew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new PlayerRewEvent());
                    }
                });

                TextView soundTitle = view.findViewById(R.id.play_title);
                soundTitle.setText(sound.getTitle());

                endTimeView = view.findViewById(R.id.time);
                currentTimeView = view.findViewById(R.id.time_current);

                setEndTime(duration);
            }
        }

        return dialog;
    }

    private void setEndTime(int msTime) {
        if (endTimeView != null)
            endTimeView.setText(Utils.msTimeToString(msTime));
        if (seekBar != null)
            seekBar.setMax(msTime);
    }

    public void setCurrentTime(int msTime) {
        if (currentTimeView != null)
            currentTimeView.setText(Utils.msTimeToString(msTime));
    }

    public void setProgress(int progress) {
        if (seekBar != null)
            seekBar.setProgress(progress);
    }
}
