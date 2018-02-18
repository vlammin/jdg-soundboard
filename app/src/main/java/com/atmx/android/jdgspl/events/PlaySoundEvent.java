/*
 * PlaySoundEvent
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.events;

import com.atmx.android.jdgspl.objects.Sound;

public class PlaySoundEvent {

    private Sound sound;

    public PlaySoundEvent(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }
}
