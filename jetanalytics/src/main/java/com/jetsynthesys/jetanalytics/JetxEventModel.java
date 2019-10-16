package com.jetsynthesys.jetanalytics;

/**
 * Created by siddhartho.gosh on 10-11-2017.
 */

class JetxEventModel {

    public String gameId = "";
    public String userCode = "";
    public String timeStamp = "";
    public String eventName = "";
    public String advid = "";
    public String gameVersion = "";
    public String deviceId = "";
    public String sessionId = "";
    public String param[] = new String[15];


    public String toJsonString() {
        String str = "{\"game_id\":\"" + gameId + "\",\"session_id\":\"" + sessionId + "\",\"device_id\":\"" + deviceId +
                "\",\"user_code\":\"" + userCode + "\",\"time_stamp\":\"" + timeStamp + "\",\"game_version\":\"" + gameVersion +
                "\",\"event_name\":\"" + eventName + "\",\"advid\":\"" + advid +
                "\",\"param1\":\"" + param[0] +
                "\",\"param2\":\"" + param[1] +
                "\",\"param3\":\"" + param[2] +
                "\",\"param4\":\"" + param[3] +
                "\",\"param5\":\"" + param[4] +
                "\",\"param6\":\"" + param[5] +
                "\",\"param7\":\"" + param[6] +
                "\",\"param8\":\"" + param[7] +
                "\",\"param9\":\"" + param[8] +
                "\",\"param10\":\"" + param[9] +
                "\",\"param11\":\"" + param[10] +
                "\",\"param12\":\"" + param[11] +
                "\",\"param13\":\"" + param[12] +
                "\",\"param14\":\"" + param[13] +
                "\",\"param15\":\"" + param[14] + "\"}";
        return str;
    }
}