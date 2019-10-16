package com.jetsynthesys.jetanalytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

class JetxDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "JetAnalyticsDb";
    private static final int DATABASE_VERSION = 2;
    private static final String SESSIONS_TABLE_NAME = "sessntab";
    private static final String CRASH_TABLE_NAME = "crashtab";
    private static final String EVENTS_TABLE_NAME = "eventab";

    String CREATE_CRASH_TABLE = "CREATE TABLE "
            + CRASH_TABLE_NAME
            + "(id INTEGER PRIMARY KEY, " + JetxConstants.GAME_ID
            + " TEXT, " + JetxConstants.SESSION_ID
            + " TEXT, " + JetxConstants.CRASH_DUMP
            + " TEXT, " + JetxConstants.DEVICE_ID
            + " TEXT, " + JetxConstants.TIME_STAMP
            + " TEXT, " + JetxConstants.GAME_VERSION
            + " TEXT, " + JetxConstants.ALREADY_SENT
            + " TEXT)";
    String CREATE_EVENTS_TABLE = "CREATE TABLE "
            + EVENTS_TABLE_NAME
            + "(id INTEGER PRIMARY KEY, " + JetxConstants.GAME_ID
            + " TEXT, " + JetxConstants.SESSION_ID
            + " TEXT, " + JetxConstants.EVENT_NAME
            + " TEXT, " + JetxConstants.DEVICE_ID
            + " TEXT, " + JetxConstants.TIME_STAMP
            + " TEXT, " + JetxConstants.USERCODE
            + " TEXT, " + JetxConstants.GA_ADV_ID
            + " TEXT, " + JetxConstants.GAME_VERSION
            + " TEXT, " + JetxConstants.PARAM1
            + " TEXT, " + JetxConstants.PARAM2
            + " TEXT, " + JetxConstants.PARAM3
            + " TEXT, " + JetxConstants.PARAM4
            + " TEXT, " + JetxConstants.PARAM5
            + " TEXT, " + JetxConstants.PARAM6
            + " TEXT, " + JetxConstants.PARAM7
            + " TEXT, " + JetxConstants.PARAM8
            + " TEXT, " + JetxConstants.PARAM9
            + " TEXT, " + JetxConstants.PARAM10
            + " TEXT, " + JetxConstants.PARAM11
            + " TEXT, " + JetxConstants.PARAM12
            + " TEXT, " + JetxConstants.PARAM13
            + " TEXT, " + JetxConstants.PARAM14
            + " TEXT, " + JetxConstants.PARAM15
            + " TEXT, " + JetxConstants.ALREADY_SENT
            + " TEXT)";
    String CREATE_SESSIONS_TABLE = "CREATE TABLE "
            + SESSIONS_TABLE_NAME
            + "(id INTEGER PRIMARY KEY, " + JetxConstants.GAME_ID
            + " TEXT, " + JetxConstants.MAKEMODEL
            + " TEXT, " + JetxConstants.OPERATING_SYSTEM
            + " TEXT, " + JetxConstants.USERCODE
            + " TEXT, " + JetxConstants.TIME_STAMP
            + " TEXT, " + JetxConstants.CITY
            + " TEXT, " + JetxConstants.OPERATOR_NAME
            + " TEXT, " + JetxConstants.NETWORK_TYPE
            + " TEXT, " + JetxConstants.LANGUAGE
            + " TEXT, " + JetxConstants.GAME_VERSION
            + " TEXT, " + JetxConstants.DEVICE_ID
            + " TEXT, " + JetxConstants.SESSION_ID
            + " TEXT, " + JetxConstants.COUNTRY
            + " TEXT, " + JetxConstants.OS_VERSION
            + " TEXT, " + JetxConstants.SOURCE_ACQUISITION
            + " TEXT, " + JetxConstants.ALREADY_SENT + " TEXT)";


    public JetxDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SESSIONS_TABLE);
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_CRASH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed

        if (oldVersion != newVersion) {
            String ALTER_TABLE_EVENTS_TABLE_NAME = "ALTER TABLE " + EVENTS_TABLE_NAME + " ADD COLUMN " + JetxConstants.GA_ADV_ID + " TEXT ";
            db.execSQL(ALTER_TABLE_EVENTS_TABLE_NAME);
        }

        db.execSQL("DROP TABLE IF EXISTS " + SESSIONS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CRASH_TABLE_NAME);


        // Creating tables again
        onCreate(db);
    }

    public void insertSession(HashMap<String, String> sessionMap) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (Map.Entry<String, String> entry : sessionMap.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
            values.put(JetxConstants.ALREADY_SENT, "false");
            db.insert(SESSIONS_TABLE_NAME, null, values);
            db.close();
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics insertSession: " + e);
        }
    }

    public void insertEvent(HashMap<String, String> eventsMap) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (Map.Entry<String, String> entry : eventsMap.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
            values.put(JetxConstants.ALREADY_SENT, "false");
            db.insert(EVENTS_TABLE_NAME, null, values);
            db.close();
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics insertEvent: " + e);
        }
    }

    public void insertCrash(HashMap<String, String> sessionMap) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (Map.Entry<String, String> entry : sessionMap.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
            values.put(JetxConstants.ALREADY_SENT, "false");
            db.insert(CRASH_TABLE_NAME, null, values);
            db.close();
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics insertCrash: " + e);
        }
    }

    public void updateDataSent(String[] ids, int type) {
        try {
            //create a wildcard string
            String wilds = "", tableName = "";

            for (int i = 0; i < ids.length; i++) {
                wilds = wilds + "?";
                if (i != (ids.length - 1))
                    wilds = wilds + ",";
            }
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues args = new ContentValues();
            args.put(JetxConstants.ALREADY_SENT, "true");
            if (type == JetxConstants.TYPE_SESSION)
                tableName = SESSIONS_TABLE_NAME;
            else if (type == JetxConstants.TYPE_EVENT)
                tableName = EVENTS_TABLE_NAME;
            else if (type == JetxConstants.TYPE_CRASH)
                tableName = CRASH_TABLE_NAME;
            db.update(tableName, args, "id IN (" + wilds + ")", ids);
            db.close();
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics updateDataSent: " + e);
        }
    }


    public Map<String, Object> selectEventsfromTop(int limit) {

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            StringBuilder sb = new StringBuilder();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query(false, EVENTS_TABLE_NAME, new String[]{"id", JetxConstants.GAME_ID, JetxConstants.SESSION_ID, "event_name", "deviceid", "timestamp",
                            "usercode", "gameversion", "param1", "param2", "param3", "param4", "param5", "param6", "param7", "param8", "param9", "param10", "param11", "param12", "param13", "param14", "param15"}, "alreadysent=?",
                    new String[]{"false"}, null, null, "id desc", "" + limit);
            // Log.d("GETTA****>", "" + cursor.getCount());
            if (cursor.getCount() > 0) {
                String[] id = new String[cursor.getCount()];
                int icnt = 0;

                cursor.moveToFirst();
                do {
                    JetxEventModel jetxEventModel = new JetxEventModel();
                    jetxEventModel.eventName = cursor.getString(cursor.getColumnIndex(JetxConstants.EVENT_NAME));
                    jetxEventModel.gameId = cursor.getString(cursor.getColumnIndex(JetxConstants.GAME_ID));
                    jetxEventModel.userCode = cursor.getString(cursor.getColumnIndex(JetxConstants.USERCODE));
                    jetxEventModel.timeStamp = cursor.getString(cursor.getColumnIndex(JetxConstants.TIME_STAMP));
                    jetxEventModel.gameVersion = cursor.getString(cursor.getColumnIndex(JetxConstants.GAME_VERSION));
                    jetxEventModel.deviceId = cursor.getString(cursor.getColumnIndex(JetxConstants.DEVICE_ID));
                    jetxEventModel.sessionId = cursor.getString(cursor.getColumnIndex(JetxConstants.SESSION_ID));
                    jetxEventModel.param[0] = cursor.getString(cursor.getColumnIndex(JetxConstants.PARAM1));
                    jetxEventModel.param[1] = cursor.getString(cursor.getColumnIndex(JetxConstants.PARAM2));
                    jetxEventModel.param[2] = cursor.getString(cursor.getColumnIndex(JetxConstants.PARAM3));
                    jetxEventModel.param[3] = cursor.getString(cursor.getColumnIndex(JetxConstants.PARAM4));
                    jetxEventModel.param[4] = cursor.getString(cursor.getColumnIndex("param5"));
                    jetxEventModel.param[5] = cursor.getString(cursor.getColumnIndex("param6"));
                    jetxEventModel.param[6] = cursor.getString(cursor.getColumnIndex("param7"));
                    jetxEventModel.param[7] = cursor.getString(cursor.getColumnIndex("param8"));
                    jetxEventModel.param[8] = cursor.getString(cursor.getColumnIndex("param9"));
                    jetxEventModel.param[9] = cursor.getString(cursor.getColumnIndex("param10"));
                    jetxEventModel.param[10] = cursor.getString(cursor.getColumnIndex("param11"));
                    jetxEventModel.param[11] = cursor.getString(cursor.getColumnIndex("param12"));
                    jetxEventModel.param[12] = cursor.getString(cursor.getColumnIndex("param13"));
                    jetxEventModel.param[13] = cursor.getString(cursor.getColumnIndex("param14"));
                    jetxEventModel.param[14] = cursor.getString(cursor.getColumnIndex("param15"));
                    sb.append(jetxEventModel.toJsonString());
                    id[icnt] = cursor.getString(cursor.getColumnIndex("id"));
                    icnt++;
                    if (!cursor.isLast())
                        sb.append(",");
                } while (cursor.moveToNext());
                params.put("id", id);
                params.put("tabdata", sb.toString());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics selectEventsfromTop: " + e);
        }
        return params;
    }

    public Map<String, Object> selectSessionsfromTop(int limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            StringBuilder sb = new StringBuilder();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query(false, SESSIONS_TABLE_NAME, new String[]{"id", "gameid", "makemodel", "os", "usercode",
                            "timestamp", "city", "operatorname", "networktype", "language", "gameversion", "deviceid", "sessionid", "country", "osversion", "srcacqsn"}, "alreadysent=?",
                    new String[]{"false"}, null, null, "id desc", "" + limit);
            // Log.d("GETTA****>", "" + cursor.getCount());
            if (cursor.getCount() > 0) {
                String[] id = new String[cursor.getCount()];
                int icnt = 0;
                cursor.moveToFirst();
                do {
                    JetxSessionModel jetxSessionModel = new JetxSessionModel();
                    jetxSessionModel.gameId = cursor.getString(cursor.getColumnIndex("gameid"));
                    jetxSessionModel.makeModel = cursor.getString(cursor.getColumnIndex("makemodel"));
                    jetxSessionModel.os = cursor.getString(cursor.getColumnIndex("os"));
                    jetxSessionModel.userCode = cursor.getString(cursor.getColumnIndex("usercode"));
                    jetxSessionModel.timeStamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                    jetxSessionModel.city = cursor.getString(cursor.getColumnIndex("city"));
                    jetxSessionModel.operatorName = cursor.getString(cursor.getColumnIndex("operatorname"));
                    jetxSessionModel.networkType = cursor.getString(cursor.getColumnIndex("networktype"));
                    jetxSessionModel.language = cursor.getString(cursor.getColumnIndex("language"));
                    jetxSessionModel.gameVersion = cursor.getString(cursor.getColumnIndex("gameversion"));
                    jetxSessionModel.deviceId = cursor.getString(cursor.getColumnIndex("deviceid"));
                    jetxSessionModel.sessionId = cursor.getString(cursor.getColumnIndex("sessionid"));
                    jetxSessionModel.country = cursor.getString(cursor.getColumnIndex("country"));
                    jetxSessionModel.osVersion = cursor.getString(cursor.getColumnIndex("osversion"));
                    jetxSessionModel.acquisitionSource = cursor.getString(cursor.getColumnIndex("srcacqsn"));
                    sb.append(jetxSessionModel.toJsonString());
                    id[icnt] = cursor.getString(cursor.getColumnIndex("id"));
                    icnt++;
                    if (!cursor.isLast())
                        sb.append(",");
                } while (cursor.moveToNext());
                params.put("id", id);
                params.put("tabdata", sb.toString());
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics selectSessionsfromTop: " + e);
        }
        return params;
    }

    public Map<String, Object> selectCrashesfromTop(int limit) {

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            StringBuilder sb = new StringBuilder();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query(false, CRASH_TABLE_NAME, new String[]{"id", "gameid", "sessionid", "crashdump", "deviceid", "timestamp", "gameversion"}, "alreadysent=?",
                    new String[]{"false"}, null, null, "id desc", "" + limit);
            // Log.d("GETTA****>", "" + cursor.getCount());
            if (cursor.getCount() > 0) {
                String[] id = new String[cursor.getCount()];
                int icnt = 0;
                cursor.moveToFirst();
                do {
                    JetxCrashModel jetxCrashModel = new JetxCrashModel();
                    jetxCrashModel.gameId = cursor.getString(cursor.getColumnIndex("gameid"));
                    jetxCrashModel.crashDumpInfo = cursor.getString(cursor.getColumnIndex("crashdump"));
                    jetxCrashModel.timeStamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                    jetxCrashModel.gameVersion = cursor.getString(cursor.getColumnIndex("gameversion"));
                    jetxCrashModel.deviceId = cursor.getString(cursor.getColumnIndex("deviceid"));
                    jetxCrashModel.sessionId = cursor.getString(cursor.getColumnIndex("sessionid"));
                    sb.append(jetxCrashModel.toJsonString());
                    id[icnt] = cursor.getString(cursor.getColumnIndex("id"));
                    icnt++;
                    if (!cursor.isLast())
                        sb.append(",");
                } while (cursor.moveToNext());
                params.put("id", id);
                params.put("tabdata", sb.toString());
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics selectCrashesfromTop: " + e);
        }
        return params;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SESSIONS_TABLE_NAME, null, null);
        db.delete(EVENTS_TABLE_NAME, null, null);
        db.delete(CRASH_TABLE_NAME, null, null);
        db.close();
    }

    public void purge() {
        try {
            Utils.LogDebug("purge");
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(SESSIONS_TABLE_NAME, "alreadysent=?", new String[]{"true"});
            db.delete(EVENTS_TABLE_NAME, "alreadysent=?", new String[]{"true"});
            db.delete(CRASH_TABLE_NAME, "alreadysent=?", new String[]{"true"});
        } catch (Exception e) {
            Utils.LogDebug("JetAnalytics purge: " + e);
        }
    }

}