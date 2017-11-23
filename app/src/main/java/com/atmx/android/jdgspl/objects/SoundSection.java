/*
 * SoundSection
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.objects;

import java.util.ArrayList;
import java.util.List;

public class SoundSection {

    private String title;
    private List<Sound> sounds;

    public SoundSection(String title) {
        this.title = title;
        this.sounds = new ArrayList<>();
    }

    public SoundSection(String title, List<Sound> sounds) {
        this.title = title;
        this.sounds = sounds;
    }

    public String getTitle() {
        return this.title;
    }

    public int size() {
        return this.sounds.size();
    }

    public Sound get(int index) {
        return this.sounds.get(index);
    }

    public void add(Sound sound) {
        this.sounds.add(sound);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SoundSection))
            return false;

        SoundSection other = (SoundSection) obj;

        return this.title != null ? this.title.equals(other.title) : other.title == null;
    }

    @Override
    public int hashCode() {
        return this.title != null ? this.title.hashCode() : 0;
    }
}
