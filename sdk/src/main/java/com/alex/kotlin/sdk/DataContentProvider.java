package com.alex.kotlin.sdk;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * @author Alex
 * @date 2020-01-03 14:55
 * @email 18238818283@sina.cn
 * @desc ...
 */
public class DataContentProvider extends ContentProvider {

    private static final int APP_START = 1;
    private static final int APP_END = 2;
    private static final int APP_PAUS_TIME = 3;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static ContentResolver contentResolver;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            String packageName = getContext().getPackageName();
            uriMatcher.addURI(packageName + ".DataContentProvider", DataBaseHelper.APP_START_TABLE, APP_START);
            uriMatcher.addURI(packageName + ".DataContentProvider", DataBaseHelper.APP_END_TABLE, APP_END);
            uriMatcher.addURI(packageName + ".DataContentProvider", DataBaseHelper.APP_PAUSE_TIME_TABLE, APP_PAUS_TIME);

            sharedPreferences = getContext().getSharedPreferences("DataStatisticsApi", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.apply();
            contentResolver = getContext().getContentResolver();
        }
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int code = uriMatcher.match(uri);
        switch (code) {
            case APP_START:
                boolean isStart = values.getAsBoolean(DataBaseHelper.APP_START);
                editor.putBoolean(DataBaseHelper.APP_START, isStart);
                contentResolver.notifyChange(uri, null);
                break;
            case APP_END:
                boolean isEnd = values.getAsBoolean(DataBaseHelper.APP_END);
                editor.putBoolean(DataBaseHelper.APP_END, isEnd);
                break;
            case APP_PAUS_TIME:
                long pauseTime = values.getAsLong(DataBaseHelper.APP_PAUSE_TIME);
                editor.putLong(DataBaseHelper.APP_PAUSE_TIME, pauseTime);
                break;
            default:
                break;
        }
        editor.commit();
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = uriMatcher.match(uri);
        MatrixCursor matrixCursor = null;
        switch (code) {
            case APP_START:
                int appStart = sharedPreferences.getBoolean(DataBaseHelper.APP_START, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{DataBaseHelper.APP_START});
                matrixCursor.addRow(new Object[]{appStart});
                break;
            case APP_END:
                int appEnd = sharedPreferences.getBoolean(DataBaseHelper.APP_END, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{DataBaseHelper.APP_END});
                matrixCursor.addRow(new Object[]{appEnd});
                break;
            case APP_PAUS_TIME:
                long pauseTime = sharedPreferences.getLong(DataBaseHelper.APP_PAUSE_TIME, 0);
                matrixCursor = new MatrixCursor(new String[]{DataBaseHelper.APP_PAUSE_TIME});
                matrixCursor.addRow(new Object[]{pauseTime});
                break;
            default:
                break;
        }
        return matrixCursor;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
