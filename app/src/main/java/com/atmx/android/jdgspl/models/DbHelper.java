/*
 * DbHelper
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper instance;

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "jdgspl.db";

    private SQLiteDatabase dataBase;
    private SoundModel soundModel;
    private FavoriteModel favoriteModel;
    private VideoModel videoModel;

    public static synchronized DbHelper getInstance(Context context) {
        if (instance == null)
            instance = new DbHelper(context.getApplicationContext());

        return instance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.dataBase = getWritableDatabase();

        this.videoModel = new VideoModel(this);
        this.soundModel = new SoundModel(this);
        this.favoriteModel = new FavoriteModel(this);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(VideoModel.SQL_CREATE_TABLE);
        db.execSQL(SoundModel.SQL_CREATE_TABLE);
        db.execSQL(FavoriteModel.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SoundModel.SQL_DROP_TABLE);
        db.execSQL(VideoModel.SQL_DROP_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void fillDbFromXml(Context context) {
        this.dataBase.execSQL(SoundModel.SQL_DROP_TABLE);
        this.dataBase.execSQL(VideoModel.SQL_DROP_TABLE);

        this.dataBase.execSQL(VideoModel.SQL_CREATE_TABLE);
        this.dataBase.execSQL(SoundModel.SQL_CREATE_TABLE);

        this.videoModel.fillFromXml(context);
        this.soundModel.fillFromXml(context);
    }

    public void migrateOldFavorites() {
        this.favoriteModel.migrateToNewSchema();
    }

    SQLiteDatabase getDatabase() {
        return this.dataBase;
    }

    public SoundModel getSoundModel() {
        return this.soundModel;
    }

    public FavoriteModel getFavoriteModel() {
        return this.favoriteModel;
    }

    VideoModel getVideoModel() {
        return this.videoModel;
    }
}
