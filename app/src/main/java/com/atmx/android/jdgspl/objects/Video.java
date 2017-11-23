/*
 * Video
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Video implements Parcelable {

    private long id;
    private String youtubeId;
    private String title;
    private String publishDate;

    public Video(String youtubeId, String title, String publishDate) {
        this.youtubeId = youtubeId;
        this.title = title;
        this.publishDate = publishDate;
    }

    public Video(long id, String youtubeId, String title, String publishDate) {
        this.id = id;
        this.youtubeId = youtubeId;
        this.title = title;
        this.publishDate = publishDate;
    }

    public long getId() {
        return this.id;
    }

    public String getYoutubeId() {
        return this.youtubeId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPublishDate() {
        return this.publishDate;
    }

    public String getFormattedPublishDate() {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            Date date = inFormat.parse(this.publishDate);
            return new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(date);
        } catch (ParseException ex) {
            return getPublishDate();
        }
    }

    /**
     * Parcelable handling
     **/

    private Video(Parcel in) {
        this.id = in.readLong();
        this.youtubeId = in.readString();
        this.title = in.readString();
        this.publishDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.youtubeId);
        out.writeString(this.title);
        out.writeString(this.publishDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}
