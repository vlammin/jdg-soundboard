/*
 * Favorite
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.objects;

public class Favorite {

    private String title;
    private Sound sound;

    public Favorite(Sound sound) {
        this.sound = sound;
    }

    public Favorite(String title, Sound sound) {
        this.title = title;
        this.sound = sound;
    }

    public String getTitle() {
        return this.title;
    }

    public Sound getSound() {
        return this.sound;
    }
}
