package com.alex.kotlin.sdk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


/**
 * @author Alex
 * @date 2020-01-03 14:47
 * @email 18238818283@sina.cn
 * @desc ...
 */
class DataBaseHelper {

    static final String APP_START = "app_start";
    static final String APP_END = "app_end";
    static final String APP_PAUSE_TIME = "app_pause_time";

    static final String  APP_START_TABLE = "app_start";
    static final String  APP_END_TABLE = "app_end";
    static final String  APP_PAUSE_TIME_TABLE = "app_pause_time";
    
    private static final String DATA_CONTENT_PROVIDER = ".DataContentProvider/";

    private Uri APP_START_URI;
    private Uri APP_END_URI;
    private Uri APP_PAUSE_TIME_URI;
    private ContentResolver contentResolver;

    DataBaseHelper(Context context) {
        String packageName = context.getPackageName();
        APP_START_URI = Uri.parse("content://" + packageName + DATA_CONTENT_PROVIDER +APP_START_TABLE);
        APP_END_URI = Uri.parse("content://" + packageName + DATA_CONTENT_PROVIDER +   APP_END_TABLE);
        APP_PAUSE_TIME_URI = Uri.parse("content://" + packageName + DATA_CONTENT_PROVIDER +   APP_PAUSE_TIME_TABLE);
        contentResolver = context.getContentResolver();
    }

    public Uri getAPP_START_URI() {
        return APP_START_URI;
    }

    void commitAppStartState(boolean isStart) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_START, isStart);
        contentResolver.insert(APP_START_URI, contentValues);
    }

    boolean getAppEndEventState() {
        int isEnd = 1;
        Cursor cursor = contentResolver.query(APP_END_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                isEnd = cursor.getInt(cursor.getColumnIndex(APP_END));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return isEnd == 1;
    }

    void commitAppEndState(boolean send) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_END, send);
        contentResolver.insert(APP_END_URI, contentValues);
    }

    long getLastPauseTime() {
        long pauseTime = 0;
        Cursor cursor = contentResolver.query(APP_PAUSE_TIME_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                pauseTime = cursor.getLong(cursor.getColumnIndex(APP_PAUSE_TIME));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return pauseTime;
    }

    void setOnPauseTime(long time) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_PAUSE_TIME, time);
        contentResolver.insert(APP_PAUSE_TIME_URI, contentValues);
    }

}
