/*
 * SoundModel
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.atmx.android.jdgspl.R;
import com.atmx.android.jdgspl.objects.Favorite;
import com.atmx.android.jdgspl.objects.Sound;
import com.atmx.android.jdgspl.objects.SoundSection;
import com.atmx.android.jdgspl.objects.Video;
import com.atmx.android.jdgspl.tools.Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class SoundModel {
    private static final String TAG = SoundModel.class.getSimpleName();

    private DbHelper dbHelper;

    SoundModel(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private static class Schema implements BaseColumns {
        private static final String TABLE = "sound";
        private static final String CUSTOM_ID = "custom_id";
        private static final String TITLE = "title";
        private static final String NEW = "new";
        private static final String CATEGORY_ID = "category_id";
        private static final String RESOURCE_ID = "resource_id";
        private static final String VIDEO_ID = "video_id";
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

    private static final String[] COMPLETE_PROJECTION = {
        aliased(Schema._ID),
        aliased(Schema.CUSTOM_ID),
        aliased(Schema.TITLE),
        aliased(Schema.NEW),
        aliased(Schema.CATEGORY_ID),
        aliased(Schema.RESOURCE_ID),
        aliased(Schema.VIDEO_ID)
    };

    public static class Sort {
        public static final String TITLE = prefixed(Schema.TITLE);
    }

    private static String SECTION_TITLE = "title";
    private static String SECTION_VIDEO_TITLE = "video_title";

    private static String getSectionMode(String order) {
        if (order.equals(Sort.TITLE))
            return SECTION_TITLE;
        else
            return SECTION_VIDEO_TITLE;
    }

    static final String SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + Schema.TABLE + " (" +
            Schema._ID + " INTEGER PRIMARY KEY," +
            Schema.CUSTOM_ID + " TEXT," +
            Schema.TITLE + " TEXT COLLATE UNICODE," +
            Schema.NEW + " INTEGER, " +
            Schema.CATEGORY_ID + " INTEGER," +
            Schema.RESOURCE_ID + " INTEGER," +
            Schema.VIDEO_ID + " INTEGER," +
            "FOREIGN KEY (" + Schema.VIDEO_ID + ") " +
            "REFERENCES " + VideoModel.SQL_REFERENCE_KEY +
            ")";

    static final String SQL_DROP_TABLE =
        "DROP TABLE IF EXISTS " + Schema.TABLE;

    private Sound makeObject(Cursor cursor) {
        return new Sound(
            cursor.getLong(
                cursor.getColumnIndexOrThrow(aliasOf(Schema._ID))
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.CUSTOM_ID))
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.TITLE))
            ),
            cursor.getInt(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.NEW))
            ) == 1,
            cursor.getInt(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.RESOURCE_ID))
            ),
            cursor.getInt(
                cursor.getColumnIndexOrThrow(aliasOf(Schema.CATEGORY_ID))
            ),
            VideoModel.makeObject(cursor)
        );
    }

    private static ContentValues makeValues(Sound sound) {
        ContentValues values = new ContentValues();
        values.put(Schema.CUSTOM_ID, sound.getCustomId());
        values.put(Schema.TITLE, sound.getTitle());
        values.put(Schema.NEW, sound.isNew() ? 1 : 0);
        values.put(Schema.CATEGORY_ID, sound.getCategoryId());
        values.put(Schema.RESOURCE_ID, sound.getResourceId());
        values.put(Schema.VIDEO_ID, sound.getVideo().getId());

        return values;
    }

    private Cursor getQuery(
        boolean filterFavorites,
        String selection,
        String[] selectionArgs,
        String groupBy,
        String having,
        String sortOrder
    ) {
        SQLiteDatabase db = dbHelper.getDatabase();

        String tables = Schema.TABLE + " " +
            VideoModel.SQL_INNER_JOIN + " = " + prefixed(Schema.VIDEO_ID);

        String[] projection = ArrayUtils.addAll(
            COMPLETE_PROJECTION,
            VideoModel.COMPLETE_PROJECTION
        );

        if (filterFavorites)
            tables += " " + FavoriteModel.SQL_INNER_JOIN + " = " + prefixed(Schema.CUSTOM_ID);

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tables);

        return builder.query(
            db,
            projection,
            selection,
            selectionArgs,
            groupBy,
            having,
            sortOrder);
    }

    private void addMultiple(List<Sound> list) {
        SQLiteDatabase db = dbHelper.getDatabase();
        db.beginTransaction();
        try {
            for (Sound sound : list) {
                db.insertOrThrow(Schema.TABLE, null, makeValues(sound));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Could not insert multiple entries", e);
        } finally {
            db.endTransaction();
        }
    }

    Sound getByTitle(String title) {
        Sound sound = null;

        String selection = prefixed(Schema.TITLE) + " = ?";
        String[] selectionArgs = {title};

        Cursor cursor = this.getQuery(false, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst())
            sound = makeObject(cursor);

        cursor.close();

        return sound;
    }

    private List<Sound> getAll(String sort, String order) {
        List<Sound> list = new ArrayList<>();

        String sortOrder = sort + " " + order;
        if (!sort.equals(Sort.TITLE))
            sortOrder += "," + Sort.TITLE + " ASC";

        Cursor cursor = this.getQuery(false, null, null, null, null, sortOrder);

        while (cursor.moveToNext()) {
            list.add(makeObject(cursor));
        }
        cursor.close();

        return list;
    }

    private List<Sound> getAllByCategoryId(int categoryId, String sort, String order) {
        List<Sound> list = new ArrayList<>();

        String selection = prefixed(Schema.CATEGORY_ID) + " = ?";
        String[] selectionArgs = {String.valueOf(categoryId)};

        String sortOrder = sort + " " + order;
        if (!sort.equals(Sort.TITLE))
            sortOrder += "," + Sort.TITLE + " ASC";

        Cursor cursor = this.getQuery(false, selection, selectionArgs, null, null, sortOrder);

        while (cursor.moveToNext()) {
            list.add(makeObject(cursor));
        }
        cursor.close();

        return list;
    }

    private List<Sound> getAllFavorites(String sort, String order) {
        List<Sound> list = new ArrayList<>();

        String sortOrder = sort + " " + order;
        if (!sort.equals(Sort.TITLE))
            sortOrder += "," + Sort.TITLE + " ASC";

        Cursor cursor = this.getQuery(true, null, null, null, null, sortOrder);

        while (cursor.moveToNext()) {
            list.add(makeObject(cursor));
        }
        cursor.close();

        return list;
    }

    private List<Sound> getAllNews(String sort, String order) {
        List<Sound> list = new ArrayList<>();

        String selection = prefixed(Schema.NEW) + " = ?";
        String[] selectionArgs = {String.valueOf("1")};

        String sortOrder = sort + " " + order;
        if (!sort.equals(Sort.TITLE))
            sortOrder += "," + Sort.TITLE + " ASC";

        Cursor cursor = this.getQuery(false, selection, selectionArgs, null, null, sortOrder);

        while (cursor.moveToNext()) {
            list.add(makeObject(cursor));
        }
        cursor.close();

        return list;
    }

    private List<Sound> searchAll(String sort, String order, String search) {
        List<Sound> list = new ArrayList<>();

        if (StringUtils.isNotBlank(search)) {
            String selection = prefixed(Schema.TITLE) + " LIKE ?";
            String[] selectionArgs = {"%" + search + "%"};

            String sortOrder = sort + " " + order;
            if (!sort.equals(Sort.TITLE))
                sortOrder += "," + Sort.TITLE + " ASC";

            Cursor cursor = this.getQuery(false, selection, selectionArgs, null, null, sortOrder);

            while (cursor.moveToNext()) {
                list.add(makeObject(cursor));
            }
            cursor.close();
        }

        return list;
    }

    public boolean existsFavorite(Sound sound) {
        Favorite favorite = new Favorite(sound);
        return dbHelper.getFavoriteModel().exists(favorite);
    }

    public long getCount() {
        SQLiteDatabase db = dbHelper.getDatabase();

        return DatabaseUtils.queryNumEntries(db, Schema.TABLE);
    }

    public long getNewsCount() {
        SQLiteDatabase db = dbHelper.getDatabase();

        String selection = prefixed(Schema.NEW) + " = ?";
        String[] selectionArgs = {String.valueOf("1")};

        return DatabaseUtils.queryNumEntries(db, Schema.TABLE, selection, selectionArgs);
    }

    public long getCountByCategoryId(int categoryId) {
        SQLiteDatabase db = dbHelper.getDatabase();

        String selection = prefixed(Schema.CATEGORY_ID) + " = ?";
        String[] selectionArgs = {String.valueOf(categoryId)};

        return DatabaseUtils.queryNumEntries(db, Schema.TABLE, selection, selectionArgs);
    }

    private List<SoundSection> getSectionedData(List<Sound> list, String sectionMode) {
        List<SoundSection> soundSections = new ArrayList<>();

        for (Sound sound : list) {
            String title;
            if (sectionMode.equals(SECTION_VIDEO_TITLE))
                title = sound.getVideo().getTitle();
            else
                title = Utils.toAscii(
                    sound.getTitle().substring(0, 1).toUpperCase()
                );

            int index = soundSections.indexOf(new SoundSection(title));
            if (index != -1) {
                soundSections.get(index).add(sound);
            } else {
                List<Sound> subList = new ArrayList<>();
                subList.add(sound);
                soundSections.add(new SoundSection(title, subList));
            }
        }

        return soundSections;
    }

    public List<SoundSection> getAllMap(String sort, String order) {
        List<Sound> sounds = getAll(sort, order);
        return getSectionedData(sounds, getSectionMode(sort));
    }

    public List<SoundSection> getAllByCategoryIdMap(int categoryId, String sort, String order) {
        List<Sound> sounds = getAllByCategoryId(categoryId, sort, order);
        return getSectionedData(sounds, getSectionMode(sort));
    }

    public List<SoundSection> getAllFavoritesMap(String sort, String order) {
        List<Sound> sounds = getAllFavorites(sort, order);
        return getSectionedData(sounds, getSectionMode(sort));
    }

    public List<SoundSection> getAllNewsMap(String sort, String order) {
        List<Sound> sounds = getAllNews(sort, order);
        return getSectionedData(sounds, getSectionMode(sort));
    }

    public List<SoundSection> searchAllMap(String sort, String order, String search) {
        List<Sound> sounds = searchAll(sort, order, search);
        return getSectionedData(sounds, getSectionMode(sort));
    }

    void fillFromXml(Context context) {
        List<Sound> list = new ArrayList<>();

        try {
            XmlResourceParser xpp = context.getResources().getXml(R.xml.sounds);

            int event = xpp.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("sound")) {
                        Video video = dbHelper.getVideoModel().getByYoutubeId(
                            xpp.getAttributeValue(null, "video-youtube-id")
                        );
                        Sound sound = new Sound(
                            xpp.getAttributeValue(null, "custom_id"),
                            xpp.getAttributeValue(null, "title"),
                            xpp.getAttributeIntValue(null, "new", 0) == 1,
                            xpp.getAttributeResourceValue(null, "resource", 0),
                            xpp.getAttributeResourceValue(null, "category", 0),
                            video
                        );

                        list.add(sound);
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
