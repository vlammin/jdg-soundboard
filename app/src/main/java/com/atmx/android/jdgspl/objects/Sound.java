/*
 * Sound
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.atmx.android.jdgspl.R;

public class Sound implements Parcelable {

    private long id;
    private String customId;
    private String title;
    private boolean isNew;
    private int resourceId;
    private int categoryId;
    private Video video;

    public Sound(
        String customId,
        String title,
        boolean isNew,
        int resourceId,
        int categoryId,
        Video video
    ) {
        this.customId = customId;
        this.title = title;
        this.isNew = isNew;
        this.resourceId = resourceId;
        this.categoryId = categoryId;
        this.video = video;
    }

    public Sound(
        long id,
        String customId,
        String title,
        boolean isNew,
        int resourceId,
        int categoryId,
        Video video
    ) {
        this.id = id;
        this.customId = customId;
        this.title = title;
        this.isNew = isNew;
        this.resourceId = resourceId;
        this.categoryId = categoryId;
        this.video = video;
    }

    public long getId() {
        return this.id;
    }

    public String getCustomId() {
        return this.customId;
    }

    public String getTitle() {
        return (this.title != null ? this.title : "");
    }

    public boolean isNew() {
        return this.isNew;
    }

    public int getResourceId() {
        return this.resourceId;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public Video getVideo() {
        return this.video;
    }

    public boolean isMusic() {
        return this.getCategoryId() == R.string.cat2_music;
    }

    /**
     * Parcelable handling
     **/

    public static final Parcelable.Creator<Sound> CREATOR = new Parcelable.Creator<Sound>() {
        @Override
        public Sound createFromParcel(Parcel in) {
            return new Sound(in);
        }

        @Override
        public Sound[] newArray(int size) {
            return new Sound[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.customId);
        out.writeString(this.title);
        out.writeInt(this.isNew ? 1 : 0);
        out.writeInt(this.resourceId);
        out.writeInt(this.categoryId);
        out.writeParcelable(this.video, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Sound(Parcel in) {
        this.id = in.readLong();
        this.customId = in.readString();
        this.title = in.readString();
        this.isNew = in.readInt() != 0;
        this.resourceId = in.readInt();
        this.categoryId = in.readInt();
        this.video = in.readParcelable(Video.class.getClassLoader());
    }
}
