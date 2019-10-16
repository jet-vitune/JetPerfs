package com.jetsynthesys.jetanalytics;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by siddhartho.gosh on 14-11-2017.
 */

class JetxDataFetcher  {

    private Context context;
    private SharedPreferences pref;
    private JSONObject requestObject;

    private String GA_ADV_ID = "ga_adv_id";


    public JetxDataFetcher(Context context) {
        this.context = context.getApplicationContext();
        pref = context.getSharedPreferences("JetAnalytics", Context.MODE_PRIVATE);
    }

    public String getGAId() {
        return pref.getString(GA_ADV_ID, "");
    }

    public void setGAId(String gaId) {
        pref.edit().putString(GA_ADV_ID, gaId).apply();
    }
}

