/*
 * FavoriteModel
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.atmx.android.jdgspl.objects.Favorite;
import com.atmx.android.jdgspl.objects.Sound;

import java.util.ArrayList;
import java.util.List;

public class FavoriteModel {
    private static final String TAG = FavoriteModel.class.getSimpleName();

    private DbHelper dbHelper;

    FavoriteModel(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private static class Schema implements BaseColumns {
        private static final String TABLE = "favorite";
        private static final String TITLE = "title";
        private static final String SOUND_CUSTOM_ID = "sound_custom_id";
    }

    private static String prefixed(String columnName) {
        return Schema.TABLE + "." + columnName;
    }

    static final String SQL_INNER_JOIN =
        "INNER JOIN " + Schema.TABLE + " ON " + prefixed(Schema.SOUND_CUSTOM_ID);

    static final String SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + Schema.TABLE + " (" +
            Schema._ID + " INTEGER PRIMARY KEY," +
            Schema.TITLE + " TEXT COLLATE UNICODE," +
            Schema.SOUND_CUSTOM_ID + " TEXT" +
            ")";

    private ContentValues makeValues(Favorite favorite) {
        ContentValues values = new ContentValues();
        values.put(Schema.TITLE, favorite.getTitle());
        values.put(Schema.SOUND_CUSTOM_ID, favorite.getSound().getCustomId());

        return values;
    }

    private void addMultiple(List<Favorite> list) {
        SQLiteDatabase db = dbHelper.getDatabase();
        db.beginTransaction();
        try {
            for (Favorite favorite : list) {
                db.insert(Schema.TABLE, null, this.makeValues(favorite));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Could not insert multiple entries", e);
        } finally {
            db.endTransaction();
        }
    }

    public long add(Favorite favorite) {
        SQLiteDatabase db = dbHelper.getDatabase();

        return db.insert(Schema.TABLE, null, this.makeValues(favorite));
    }

    public int delete(Favorite favorite) {
        SQLiteDatabase db = dbHelper.getDatabase();

        String selection = prefixed(Schema.SOUND_CUSTOM_ID) + " = ?";
        String[] selectionArgs = {favorite.getSound().getCustomId()};

        return db.delete(Schema.TABLE, selection, selectionArgs);
    }

    public boolean exists(Favorite favorite) {
        SQLiteDatabase db = dbHelper.getDatabase();

        String selection = prefixed(Schema.SOUND_CUSTOM_ID) + " = ?";
        String[] selectionArgs = {favorite.getSound().getCustomId()};

        long count = DatabaseUtils.queryNumEntries(db, Schema.TABLE, selection, selectionArgs);
        return count > 0;
    }

    public long getCount() {
        SQLiteDatabase db = dbHelper.getDatabase();

        return DatabaseUtils.queryNumEntries(db, Schema.TABLE);
    }

    /**
     * Migration from old to new schema
     **/

    private static class OldSchema implements BaseColumns {
        private static final String TABLE = "favorites";
        private static final String NAME = "f_name";
    }

    private boolean oldSchemaExists() {
        SQLiteDatabase db = dbHelper.getDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT DISTINCT tbl_name FROM sqlite_master " +
                "WHERE tbl_name = '" + OldSchema.TABLE + "'", null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private List<Favorite> getAllFromOldSchema() {
        List<Favorite> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getDatabase();

        String[] projection = {OldSchema.NAME};

        Cursor cursor = db.query(
            OldSchema.TABLE,
            projection,
            null,
            null,
            null,
            null,
            null
        );

        while (cursor.moveToNext()) {
            Sound sound = dbHelper.getSoundModel().getByTitle(
                cursor.getString(cursor.getColumnIndexOrThrow(OldSchema.NAME))
            );
            list.add(new Favorite(sound.getTitle(), sound));
        }
        cursor.close();

        return list;
    }

    void migrateToNewSchema() {
        if (oldSchemaExists()) {
            Log.i(TAG, "Old schema found for favorites");
            List<Favorite> list = getAllFromOldSchema();
            addMultiple(list);
            dbHelper.getDatabase().execSQL("DROP TABLE IF EXISTS " + OldSchema.TABLE);
        }
    }
}
