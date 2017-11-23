/*
 * PlayerEvent
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.events;


public class PlayerProgressEvent {

    private int progress;

    public PlayerProgressEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return this.progress;
    }
}
