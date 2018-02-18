/*
 * PlayerTrackingEvent
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.events;

public class PlayerTrackingEvent {

    private boolean isTracking;

    public PlayerTrackingEvent(boolean isTracking) {
        this.isTracking = isTracking;
    }

    public boolean isTracking() {
        return isTracking;
    }
}
