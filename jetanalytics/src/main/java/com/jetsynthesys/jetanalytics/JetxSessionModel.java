package com.jetsynthesys.jetanalytics;

/**
 * Created by siddhartho.gosh on 10-11-2017.
 */

class JetxSessionModel {

    public String gameId = "";
    public String makeModel = "";
    public String os = "";
    public String userCode = "";
    public String timeStamp = "";
    public String city = "";
    public String operatorName = "";
    public String networkType = "";
    public String language = "";
    public String gameVersion = "";
    public String deviceId = "";
    public String sessionId = "";
    public String country = "";
    public String osVersion = "";
    public String acquisitionSource = "";
    public String param2 = "";

    public String toJsonString() {
        String str = "{\"game_id\":\"" + gameId +
                "\",\"make_model\":\"" + makeModel +
                "\",\"os\":\"" + os +
                "\",\"user_code\":\"" + userCode +
                "\",\"time_stamp\":\"" + timeStamp +
                "\",\"city\":\"" + city +
                "\",\"operatorName\":\"" + operatorName +
                "\",\"networkType\":\"" + networkType +
                "\",\"language\":\"" + language +
                "\",\"game_version\":\"" + gameVersion +
                "\",\"device_id\":\"" + deviceId +
                "\",\"session_id\":\"" + sessionId +
                "\",\"country\":\"" + country +
                "\",\"os_version\":\"" + osVersion +
                "\",\"param2\":\"" + param2 +
                "\",\"source_of_acquisition\":\"" + acquisitionSource + "\"}";
        return str;
    }
}