package com.jetsynthesys.jetanalytics;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

/**
 * Created by siddhartho.gosh on 09-11-2017.
 */

public class JetAnalytics {

    private Context mContext;
    private JetxDbHelper mAnalyticsDbHelper;
    private JetxUploadTimer mJetxUploadTimer = null;
    private boolean moreSessionsLeft = false, moreEventsLeft = false, moreCrashesLeft = false;
    private static final String TAG = "JetAnalytics";
    private JSONObject mRequestObject;
    private String mAppVersion = "", mUserCode = "", mAuthKey = "", mDeviceId = "", mCountry = "";
    private String advid = "";
    private String mNetworkType = "", mOperatorName = "", mCity = "", mLanguage = "";
    private static JetAnalytics mJetAnalytics = null;
    private String mSessionIds[], mEventIds[], mCrashIds[];
    private static boolean isServerConfigured = false;

    public static JetAnalytics getInstance() {
        if (mJetAnalytics == null) {
            mJetAnalytics = new JetAnalytics();
        }
        return mJetAnalytics;
    }

    private JetAnalytics() {
    }


    public static void enableLog(Boolean logenabled) {

        Utils.LOG_ENABLED = logenabled;
    }

    public void init(Context context, String AuthKey, String appVersion, String... extras) {

        try {

            if (context != null) {
                if (mJetxUploadTimer == null && Utils.getSessionId().equalsIgnoreCase("unknown")) {//so that init is called only once

                    mDeviceId = Utils.getDeviceId(context);
                    Utils.setAuthorizationKey(AuthKey);
                    Utils.setSessionId(System.currentTimeMillis() + mDeviceId + "_and");
                    mContext = context;
                    getDeviceDetails();
                    setGAAdId(context);
                    mAnalyticsDbHelper = new JetxDbHelper(mContext);
                    mAuthKey = AuthKey;
                    mAppVersion = appVersion;
                    if (extras.length == 1) {
                        mUserCode = extras[0];
                    } else if (extras.length >= 2) {
                        mUserCode = extras[0];
                    }

                    initConfig(extras);
                    initCrashInterceptor();

                } else {
                    Utils.Log("Already Initialized");
                }
            }
        } catch (Exception e) {
            Utils.LogDebug(e.toString());
        }
    }


    public void setUserCode(String usercode) {
        mUserCode = usercode;
    }

    public void setGAAdId(final Context context) {

        Log.e(TAG, "setGAAdId");

        String GAId = new JetxDataFetcher(context).getGAId();

        if (GAId == null || GAId.isEmpty()) {

            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    AdvertisingIdClient.Info idInfo = null;
                    try {
                        idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    } catch (GooglePlayServicesNotAvailableException e) {
                        Log.e(TAG, e.toString());
                    } catch (GooglePlayServicesRepairableException e) {
                        Log.e(TAG, e.toString());
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    String advertId = null;
                    try {
                        advertId = idInfo.getId();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    return advertId;
                }

                @Override
                protected void onPostExecute(String advertId) {

                    if (advertId != null && !advertId.isEmpty()) {
                        advid = advertId;
                        new JetxDataFetcher(context).setGAId(advid);
                    } else {
                        advid = "";
                    }

                    Log.e(TAG, "setGAAdId onPostExecute advid: "+advid);
                }
            };

            task.execute();

        } else {
            advid = GAId;
        }

        Log.e(TAG, "setGAAdId advid: "+advid);
    }

    private void initCrashInterceptor() {
        try {
            final Thread.UncaughtExceptionHandler oldHandler =
                    Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(
                    new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(
                                Thread paramThread,
                                Throwable paramThrowable
                        ) {
                            //Do your own error handling here
                            insertCrash("Localized Message: " + paramThrowable.getLocalizedMessage() + " ,Message: " + paramThrowable.getMessage());
                            if (oldHandler != null)
                                oldHandler.uncaughtException(
                                        paramThread,
                                        paramThrowable
                                ); //Delegates to Android's error handling
                            else
                                System.exit(2); //Prevents the service/app from freezing
                        }
                    });
        } catch (Exception e) {
            Utils.LogDebug("initCrashInterceptor:  " + e);
        }
    }


    private void getDeviceDetails() {
        try {
            if (mContext != null) {
                mLanguage = Locale.getDefault().getDisplayName();
                final TelephonyManager mTelephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

                mOperatorName = mTelephony.getSimOperatorName();
                mCountry = mTelephony.getSimCountryIso();
                mNetworkType = Utils.getNetworkClass(mTelephony.getNetworkType());

            }
        } catch (Exception e) {
            Utils.LogDebug("getDeviceDetails: " + e);
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Utils.LogDebug("IP Address" + ex.toString());
        }
        return null;
    }


    private void startUploadTimer() {
        try {
            mJetxUploadTimer = new JetxUploadTimer(JetxConstants.UPLOAD_TIME_INTERVAL, JetxConstants.UPLOAD_MINI_INTERVAL) {
                public void onTick(long millisUntilFinished) {
                    if (moreSessionsLeft)
                        uploadSession();
                    if (moreEventsLeft)
                        uploadEvents();
                    if (moreCrashesLeft)
                        uploadCrashReports();
                }

                public void onFinish() {
                    uploadSession();
                    uploadEvents();
                    uploadCrashReports();
                    startUploadTimer();//restart the timer
                }
            };
            mJetxUploadTimer.start();
        } catch (Exception e) {
            Utils.LogDebug("startUploadTimer: " + e);
        }
    }


    private void uploadSession() {
        try {
            Map<String, Object> params = mAnalyticsDbHelper.selectSessionsfromTop(JetxConstants.UPLOAD_BATCH_SIZE);

            mSessionIds = (String[]) params.get("id");
            if (mSessionIds != null && Utils.isConnected(mContext) && isServerConfigured) {
                if (mSessionIds.length > 0) {
                    if (mSessionIds.length == JetxConstants.UPLOAD_BATCH_SIZE)
                        moreSessionsLeft = true;
                    else
                        moreSessionsLeft = false;
                    String sstr = "{ \"data\":{\"Sessions\":[" + (String) params.get("tabdata") + "],\"Type\":\"Sessions\"}}";
                    uploadReqToServer(sstr, JetxConstants.TYPE_SESSION);
                } else moreSessionsLeft = false;
            } else moreSessionsLeft = false;
        } catch (Exception e) {
            Utils.LogDebug("uploadSession: " + e);
        }
    }

    private void initConfig(String... extras) {
        try {
            String configjson = "{ \"deviceId\":\"" + mDeviceId + "\", \"packageName\":\"" + mContext.getPackageName() + "\", \"platform\":\"android\", \"authKey\":\"" + mAuthKey + "\" }";
            uploadReqToServer(configjson, JetxConstants.TYPE_GET_CONFIG, extras);
        } catch (Exception e) {
            Utils.LogDebug("initConfig: " + e);
        }
    }


    private void uploadEvents() {
        try {
            Map<String, Object> params = mAnalyticsDbHelper.selectEventsfromTop(JetxConstants.UPLOAD_BATCH_SIZE);
            mEventIds = (String[]) params.get("id");
            if (mEventIds != null && Utils.isConnected(mContext) && isServerConfigured) {
                if (mEventIds.length > 0) {
                    if (mEventIds.length == JetxConstants.UPLOAD_BATCH_SIZE)
                        moreEventsLeft = true;
                    else
                        moreEventsLeft = false;
                    String sstr = "{ \"data\":{\"Events\":[" + (String) params.get("tabdata") + "],\"Type\":\"Events\"}}";
                    uploadReqToServer(sstr, JetxConstants.TYPE_EVENT);
                } else
                    moreEventsLeft = false;
            } else
                moreEventsLeft = false;
        } catch (Exception e) {
            Utils.LogDebug("uploadEvents: " + e);
        }
    }

    private void uploadCrashReports() {
        try {
            Map<String, Object> params = mAnalyticsDbHelper.selectCrashesfromTop(JetxConstants.UPLOAD_BATCH_SIZE);
            mCrashIds = (String[]) params.get("id");
            if (mCrashIds != null && Utils.isConnected(mContext) && isServerConfigured) {
                if (mCrashIds.length > 0) {
                    if (mCrashIds.length == JetxConstants.UPLOAD_BATCH_SIZE)
                        moreCrashesLeft = true;
                    else
                        moreCrashesLeft = false;
                    String sstr = "{ \"data\":{\"CrashDump\":[" + (String) params.get("tabdata") + "],\"Type\":\"CrashDump\"}}";
                    uploadReqToServer(sstr, JetxConstants.TYPE_CRASH);
                } else
                    moreCrashesLeft = false;
            } else
                moreCrashesLeft = false;
        } catch (Exception e) {
            Utils.LogDebug("uploadCrashReports: " + e);
        }
    }

    private void uploadReqToServer(final String sst, final int type, final String... extras) {
        Utils.LogDebug(sst);
        try {
            mRequestObject = new JSONObject(sst);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            RequestQueue queue = JetxVolleySingleton.getInstance(mContext).getRequestQueue();
            String url = "";
            if (type == JetxConstants.TYPE_GET_CONFIG) {
                url = JetxConstants.CONFIG_URL;
            } else {
                url = JetxConstants.BASE_URL;
            }
            JsonObjectRequest uploadAnalyticsReqt = new JsonObjectRequest(url, mRequestObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {//
                            if (response != null) {
                                try {
                                    String jsonString = response.toString();
                                    Utils.LogDebug("Raw Response" + jsonString);
                                    if (jsonString != null) {
                                        JSONObject jObject = new JSONObject(jsonString);
                                        if ("200".equals(jObject.getString("errorCode"))) {
                                            Utils.LogDebug("Success in sending");
                                            switch (type) {
                                                case JetxConstants.TYPE_SESSION:
                                                    if (mSessionIds.length > 0)
                                                        mAnalyticsDbHelper.updateDataSent(mSessionIds, type);
                                                    break;
                                                case JetxConstants.TYPE_EVENT:
                                                    if (mEventIds.length > 0)
                                                        mAnalyticsDbHelper.updateDataSent(mEventIds, type);
                                                    break;
                                                case JetxConstants.TYPE_CRASH:
                                                    if (mCrashIds.length > 0)
                                                        mAnalyticsDbHelper.updateDataSent(mCrashIds, type);
                                                    break;
                                                case JetxConstants.TYPE_GET_CONFIG:
                                                    isServerConfigured = true;
                                                    Utils.LogDebug("Success***sending" + jsonString);
                                                    String url = jObject.getString("url");
                                                    JetxConstants.BASE_URL = url.replace("\\", "");
                                                    insertSession(extras);
                                                    Utils.LogDebug("Initialized Succesfully");
                                                    //start the upload timer
                                                    uploadSession();
                                                    uploadEvents();
                                                    startUploadTimer();

                                                    break;

                                                case JetxConstants.TYPE_PRIORITY_SESSION:

                                                    break;

                                                default:
                                                    break;
                                            }
                                            mAnalyticsDbHelper.purge();
                                            ///////TODO
                                        } else {
                                            Utils.LogDebug("Raw Error Code" + jObject.getString("errorCode") + " RawData " + jsonString);
                                        }
                                    } else {
                                        Utils.LogDebug("Raw Error jsonString null");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Utils.LogDebug("Raw Error Response null");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    errorOnUploadData(sst, type, extras);
                    Utils.LogDebug("Raw Error: " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return null;
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    int mStatusCode = response.statusCode;
                    if (mStatusCode == 200) {
                        return super.parseNetworkResponse(response);
                    } else {
                        errorOnUploadData(sst, type, extras);
                        return null;
                    }
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", mAuthKey);
                    params.put(JetxConstants.TIME_STAMP, System.currentTimeMillis() + "");
                    params.put("Marvels", System.currentTimeMillis() + "");
                    return params;
                }
            };
            uploadAnalyticsReqt.setRetryPolicy(new DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(uploadAnalyticsReqt);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void errorOnUploadData(String sst, final int type, final String... extras) {
        JSONObject mRequestObject = null;
        try {
            mRequestObject = new JSONObject(sst);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (type) {
            case JetxConstants.TYPE_GET_CONFIG:
                Utils.LogDebug("Initialization Failed");
                break;

            case JetxConstants.TYPE_PRIORITY_EVENT:
                try {
                    String event_name = mRequestObject.getString("event_name");
                    storeEventInDB(event_name, extras);
                    Utils.Log("Type priority event failed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    /**
     * API to reset OTP edit text values
     */
    private void insertSession(String... extras) {
        try {
            if (Utils.isConnected(mContext) && isServerConfigured) {
                JetxSessionModel jetxSessionModel = new JetxSessionModel();
                jetxSessionModel.gameId = mContext.getPackageName();
                jetxSessionModel.timeStamp = System.currentTimeMillis() + "";
                jetxSessionModel.gameVersion = mAppVersion;
                jetxSessionModel.deviceId = mDeviceId;
                jetxSessionModel.sessionId = Utils.getSessionId();
                jetxSessionModel.makeModel = Build.MANUFACTURER + " " + Build.MODEL;
                jetxSessionModel.os = "Android";
                jetxSessionModel.city = mCity;
                jetxSessionModel.operatorName = mOperatorName;
                jetxSessionModel.networkType = mNetworkType;
                jetxSessionModel.language = mLanguage;
                jetxSessionModel.country = mCountry;
                jetxSessionModel.param2 = getLocalIpAddress();
                jetxSessionModel.osVersion = Build.VERSION.SDK_INT + "";
                if (extras.length == 1)
                    jetxSessionModel.userCode = mUserCode;
                if (extras.length >= 2) {
                    jetxSessionModel.userCode = mUserCode;
                    jetxSessionModel.acquisitionSource = extras[1];
                }
                String sstr = "{ \"data\":{\"Sessions\":[" + jetxSessionModel.toJsonString() + "],\"Type\":\"Sessions\"}}";
                uploadReqToServer(sstr, JetxConstants.TYPE_PRIORITY_SESSION);
            } else {
                saveSession(extras);

                Utils.LogDebug("Raw Error No Internet: " + extras);
            }
        } catch (Exception e) {
            Utils.LogDebug("insertSession: " + e);
        }

    }

    private void saveSession(String... extras) {
        try {
            HashMap<String, String> sessionMap = new HashMap<String, String>();
            sessionMap.put(JetxConstants.GAME_ID, mContext.getPackageName());
            sessionMap.put("makemodel", Build.MANUFACTURER + " " + Build.MODEL);
            sessionMap.put("os", "Android");
            sessionMap.put(JetxConstants.TIME_STAMP, System.currentTimeMillis() + "");
            sessionMap.put("city", mCity);//TODO
            sessionMap.put("operatorname", mOperatorName);
            sessionMap.put("networktype", mNetworkType);
            sessionMap.put("language", mLanguage);
            sessionMap.put(JetxConstants.GAME_VERSION, mAppVersion);
            sessionMap.put(JetxConstants.DEVICE_ID, mDeviceId);
            sessionMap.put(JetxConstants.SESSION_ID, Utils.getSessionId());
            sessionMap.put("country", mCountry);
            sessionMap.put("osversion", Build.VERSION.SDK_INT + "");

            if (extras.length == 1)
                sessionMap.put("usercode", extras[0]);
            if (extras.length == 2) {
                sessionMap.put("usercode", extras[0]);
                sessionMap.put("srcacqsn", extras[1]);
            }
            mAnalyticsDbHelper.insertSession(sessionMap);
        } catch (Exception e) {
            Utils.LogDebug("saveSession: " + e);
        }
    }

    /**
     * API to reset OTP edit text values
     */
    public void sendEvent(Context context, String eventName, String... param) {
        try {
            mContext = context;
            if (mContext != null) {
                sendPriorityEvent(eventName, param);
            }
        } catch (Exception e) {
            Utils.LogDebug("sendEvent: " + e);
        }
    }

    public void sendEvent(String eventName, String... param) {
        try {
            if (mContext != null) {
                sendPriorityEvent(eventName, param);
            }
        } catch (Exception e) {
            Utils.LogDebug("sendEvent: " + e);
        }
    }

    /**
     * API to reset OTP edit text values
     */
    public void sendPriorityEvent(String eventName, String... param) {
        try {
            if (Utils.isConnected(mContext) && isServerConfigured) {
                JetxEventModel jetxEventModel = new JetxEventModel();
                jetxEventModel.gameId = mContext.getPackageName();
                jetxEventModel.userCode = mUserCode;
                jetxEventModel.advid = advid;
                jetxEventModel.timeStamp = System.currentTimeMillis() + "";
                jetxEventModel.gameVersion = mAppVersion;
                jetxEventModel.eventName = eventName;
                jetxEventModel.deviceId = mDeviceId;
                jetxEventModel.sessionId = Utils.getSessionId();
                for (int inic = 0; inic < param.length; inic++) {
                    jetxEventModel.param[inic] = param[inic];
                }
                String sstr = "{ \"data\":{\"Events\":[" + jetxEventModel.toJsonString() + "],\"Type\":\"Events\"}}";
                uploadReqToServer(sstr, JetxConstants.TYPE_PRIORITY_EVENT);
            } else {
                storeEventInDB(eventName, param);
            }
        } catch (Exception e) {
            Utils.LogDebug("sendPriorityEvent: " + e);
        }

    }

    private void storeEventInDB(String eventName, String... param) {
        try {
            HashMap<String, String> sessionMap = new HashMap<String, String>();
            sessionMap.put(JetxConstants.GAME_ID, mContext.getPackageName());
            sessionMap.put(JetxConstants.SESSION_ID, Utils.getSessionId());
            sessionMap.put("event_name", eventName);
            sessionMap.put(JetxConstants.DEVICE_ID, mDeviceId);//TODO
            sessionMap.put(JetxConstants.TIME_STAMP, System.currentTimeMillis() + "");
            sessionMap.put("usercode", mUserCode);
            sessionMap.put("advid", advid);
            sessionMap.put(JetxConstants.GAME_VERSION, mAppVersion);
            for (int inic = 0; inic < param.length; inic++) {
                sessionMap.put("param" + (inic + 1), param[inic]);
            }
            mAnalyticsDbHelper.insertEvent(sessionMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * API to reset OTP edit text values
     */
    private void insertCrash(String crashdump) {
        try {

            HashMap<String, String> sessionMap = new HashMap<String, String>();
            sessionMap.put(JetxConstants.GAME_ID, mContext.getPackageName());
            sessionMap.put(JetxConstants.SESSION_ID, Utils.getSessionId());
            sessionMap.put(JetxConstants.DEVICE_ID, mDeviceId);//TODO
            sessionMap.put(JetxConstants.TIME_STAMP, System.currentTimeMillis() + "");
            sessionMap.put(JetxConstants.CRASH_DUMP, crashdump);
            sessionMap.put(JetxConstants.GAME_VERSION, mAppVersion);
            mAnalyticsDbHelper.insertCrash(sessionMap);

        } catch (Exception e) {
            Utils.LogDebug("insertCrash: " + e);
        }

    }
}
