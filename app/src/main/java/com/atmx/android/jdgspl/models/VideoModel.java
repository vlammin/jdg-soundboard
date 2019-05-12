/*
 * VideoModel
 * JDGSoundboard
 *
 * Copyright (c) 2019 Vincent Lammin
 */

package com.atmx.android.jdgspl.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.atmx.android.jdgspl.R;
import com.atmx.android.jdgspl.objects.Video;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class VideoModel {
    private static final String TAG = VideoModel.class.getSimpleName();

    private DbHelper dbHelper;

    VideoModel(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private static class Schema implements BaseColumns {
        private static final String TABLE = "video";
        private static final String YOUTUBE_ID = "youtube_id";
        private static final String TITLE = "title";
        private static final String PUBLISH_DATE = "publish_date";
    }

    private static String prefixed(String columnName) {
        return Schema.TABLE + "." + columnName;
    }

    private static String aliasOf(String columnName) {
        return Schema.TABLE + "_" + columnName;
    }

    private static String aliased(String columnName) {
        return prefixed(columnName) + " AS " + aliasOf(columnName);
    }

    static final String[] COMPLETE_PROJECTION = {
        aliased(Schema._ID),
        aliased(Schema.YOUTUBE_ID),
        aliased(Schema.TITLE),
        aliased(Schema.PUBLISH_DATE)
    };

    public static class Sort {
        public static final String TITLE = prefixed(Schema.TITLE);
        public static final String PUBLISH_DATE = prefixed(Schema.PUBLISH_DATE);
    }

    static final String SQL_INNER_JOIN =
        "INNER JOIN " + Schema.TABLE + " ON " + prefixed(Schema._ID);

    static final String SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + Schema.TABLE + " (" +
            Schema._ID + " INTEGER PRIMARY KEY," +
            Schema.YOUTUBE_ID + " TEXT," +
            Schema.TITLE + " TEXT COLLATE UNICODE," +
            Schema.PUBLISH_DATE + " TEXT" +
            ")";

    static final String SQL_DROP_TABLE =
        "DROP TABLE IF EXISTS " + Schema.TABLE;

    static final String SQL_REFERENCE_KEY =
        Schema.TABLE + "(" + Schema._ID + ")";

    static Video makeObject(Cursor cursor) {
        return new Video(
            cursor.getLong(
                cursor.getColumnIndexOrThrow(aliasOf(Schema._ID))
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.YOUTUBE_ID))
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.TITLE))
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.PUBLISH_DATE))
            )
        );
    }

    private ContentValues makeValues(Video video) {
        ContentValues values = new ContentValues();
        values.put(Schema.YOUTUBE_ID, video.getYoutubeId());
        values.put(Schema.TITLE, video.getTitle());
        values.put(Schema.PUBLISH_DATE, video.getPublishDate());

        return values;
    }

    private Cursor getQuery(
        String selection,
        String[] selectionArgs,
        String groupBy,
        String having,
        String sortOrder
    ) {
        SQLiteDatabase db = dbHelper.getDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Schema.TABLE);

        return builder.query(
            db,
            COMPLETE_PROJECTION,
            selection,
            selectionArgs,
            groupBy,
            having,
            sortOrder);
    }

    private void addMultiple(List<Video> list) {
        SQLiteDatabase db = dbHelper.getDatabase();
        db.beginTransaction();
        try {
            for (Video video : list) {
                db.insertOrThrow(Schema.TABLE, null, this.makeValues(video));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Could not insert multiple entries", e);
        } finally {
            db.endTransaction();
        }
    }

    Video getByYoutubeId(String youtubeId) {
        Video video = null;

        String selection = prefixed(Schema.YOUTUBE_ID) + " = ?";
        String[] selectionArgs = {youtubeId};

        Cursor cursor = getQuery(selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst())
            video = makeObject(cursor);

        cursor.close();

        return video;
    }

    void fillFromXml(Context context) {
        List<Video> list = new ArrayList<>();

        try {
            XmlResourceParser xpp = context.getResources().getXml(R.xml.videos);

            int event = xpp.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("video")) {
                        Video video = new Video(
                            xpp.getAttributeValue(null, "youtube-id"),
                            xpp.getAttributeValue(null, "title"),
                            xpp.getAttributeValue(null, "publish-date")
                        );

                        list.add(video);
                    }
                }

                event = xpp.next();
            }

            this.addMultiple(list);

        } catch (Exception e) {
            Log.e(TAG, "Could not fill from XML", e);
        }
    }
}
