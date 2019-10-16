package com.jetsynthesys.jetanalytics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by siddhartho.gosh on 13-11-2017.
 */

class Utils {
    /**
     * API to check if network is connected
     *
     * @param context : activity context
     */
    private static String deviceId = "unknown";
    private static String authorizationKey = "unknown";
    private static String sessionId = "unknown";
    public static boolean LOG_ENABLED = false;

    static boolean isConnected(Context context) {
        boolean isConnectedStatus = false;
        try {
            //RK
            //here we first check the PROXY SETTING ENABLE OR NOT
            String connectedTo = checkConnectedToStatus(context);
            boolean isVpnEnabled = isVpnEnabled();

            if ("MOBILE_DATA".equals(connectedTo)) {
                isConnectedStatus = true;
            } else if ("WIFI".equals(connectedTo)) {
                //RK
                //CHECKING WIFI SETTINGS
                isConnectedStatus = getProxySettingDetails(context);
            } else {
                isConnectedStatus = false;
            }
            if (isVpnEnabled) {
//                System.out.println("VPN IS ENABLED");
                isConnectedStatus = false;
            }
            /*NetworkInfo info = getNetworkInfo(context);
            return (info != null && info.isConnected());*/
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return isConnectedStatus;
    }

    private static String checkConnectedToStatus(Context mContext) {
        String connectedTo = "";
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                connectedTo = "WIFI";
                // Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                connectedTo = "MOBILE_DATA";
                // Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            connectedTo = "NOT_CONNECTED";
            // not connected to the internet
        }
        return connectedTo;
    }

    private static boolean isVpnEnabled() {
        List<String> networkList = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    networkList.add(networkInterface.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (networkList.contains("tun0")) {
            return true;
        } else if (networkList.contains("ppp")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean getProxySettingDetails(Context context) {
        String proxyAddress = "";
        String portValue = "";
        int port;
        boolean proxySettingEnable = false;
        try {
            if (preICS()) {
                try {
                    proxyAddress = android.net.Proxy.getHost(context);
                    port = android.net.Proxy.getPort(context);

                    if ((proxyAddress == null) || port == 0) {
                        proxySettingEnable = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    proxyAddress = System.getProperty("http.proxyHost");
                    portValue = System.getProperty("http.proxyPort");
                    if (proxyAddress == null || portValue == null || portValue.equals("0")) {
                        proxySettingEnable = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            //ex.pr
            ex.printStackTrace();
        }
        return proxySettingEnable;
    }

    private static boolean preICS() {
        return (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    static String getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    static String getDeviceId(Context context) {
        if (deviceId.equals("unknown") && context != null) {
            //String product = Build.PRODUCT;
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            //if (product != null && androidId != null) {
            //  deviceId = product + "_" + androidId;
            if (androidId != null) {
                deviceId = androidId;
            }
        }
        return deviceId;
    }

    static String getSessionId() {
        return sessionId;
    }

    static void setSessionId(String sessionid) {
        sessionId = sessionid;
    }

    static String getAuthorizationKey() {
        return authorizationKey;
    }

    static void setAuthorizationKey(String authKey) {
        authorizationKey = authKey;
    }


    static void LogDebug(String log) {
        if (LOG_ENABLED)
            Log.d("JetAnalytics== ", log);
    }

    static void Log(String log) {
        Log.d("JetAnalytics", log);
    }
}
