package com.jetsynthesys.jetanalytics;

/**
 * Created by siddhartho.gosh on 10-11-2017.
 */

class JetxCrashModel {

    public String gameId = "";
    public String timeStamp = "";
    public String crashDumpInfo = "";
    public String gameVersion = "";
    public String deviceId = "";
    public String sessionId = "";

    public String toJsonString() {
        String str = "{\"game_id\":\"" + gameId + "\",\"session_id\":\"" + sessionId + "\",\"device_id\":\"" + deviceId +
                "\",\"crash_dump\":\"" + crashDumpInfo + "\",\"time_stamp\":\"" + timeStamp + "\",\"game_version\":\"" + gameVersion +
                "\"}";
        return str;
    }
}