/*
 * ShowSoundDetailEvent
 * JDGSoundboard
 *
 * Copyright (c) 2019 Vincent Lammin
 */

package com.atmx.android.jdgspl.events;

import com.atmx.android.jdgspl.objects.Sound;

public class ShowSoundDetailEvent {

    private Sound sound;

    public ShowSoundDetailEvent(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }
}
