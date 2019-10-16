package com.jetsynthesys.janalytics;

import android.app.Application;

import com.jetsynthesys.jetanalytics.JetAnalytics;

public class AnalyticsApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        JetAnalytics.getInstance().init(this,
                "key=staging935589e7684d343ab2bc2e1739a611fb",
                "2.1",
                "imeisend3635165");
    }
}
