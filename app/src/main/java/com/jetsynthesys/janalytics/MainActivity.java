package com.jetsynthesys.janalytics;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jetsynthesys.jetanalytics.JetAnalytics;
//import com.jetsynthesys.jetanalytics.api.JetxCrypt;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    JetAnalytics jj;
    String str = "[{ \"payOpt\": \"OPTCRDC\", \"OPTCRDC\": \"[{\"cardName\":\"MasterCard\",\"cardType\":\"CRDC\",\"payOptType\":\"OPTCRDC\",\"dataAcceptedAt\":\"CCAvenue\",\"status\":\"ACTI\",\"statusMessage\":\"\"},{\"cardName\":\"Visa\",\"cardType\":\"CRDC\",\"payOptType\":\"OPTCRDC\",\"dataAcceptedAt\":\"CCAvenue\",\"status\":\"ACTI\",\"statusMessage\":\"\"}]\" }, { \"payOpt\": \"OPTDBCRD\", \"OPTDBCRD\": \"[{\"cardName\":\"MasterCard Debit Card\",\"cardType\":\"DBCRD\",\"payOptType\":\"OPTDBCRD\",\"dataAcceptedAt\":\"CCAvenue\",\"status\":\"ACTI\"},{\"cardName\":\"Visa Debit Card\",\"cardType\":\"DBCRD\",\"payOptType\":\"OPTDBCRD\",\"dataAcceptedAt\":\"CCAvenue\",\"status\":\"ACTI\",\"statusMessage\":\"\"}]\" }, { \"payOpt\": \"OPTNBK\", \"OPTNBK\": \"[{\"cardName\":\"AvenuesTest\",\"cardType\":\"NBK\",\"payOptType\":\"OPTNBK\",\"dataAcceptedAt\":\"Service Provider\",\"status\":\"ACTI\",\"statusMessage\":\"\"}]\" }]";

    Button send_event;
    Button create_session;

    /////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;


        send_event = (Button)findViewById(R.id.send_event);
        create_session = (Button)findViewById(R.id.create_session);

        jj = JetAnalytics.getInstance();

        jj.sendEvent("SAMPLE_EVENT", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1", "imeisend3635165", "65655", "samsung", "ABCD", "XYZ");


        create_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*using key of Vi-Tune for testing*/
                jj.init(MainActivity.this, "staging935589e7684d343ab2bc2e1739a611fb", "2.1", "imeisend3635165");
            }
        });


        send_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                jj.sendEvent(MainActivity.this,"SAMPLE_EVENT", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1", "imeisend3635165", "65655", "samsung", "ABCD", "XYZ");
                jj.sendEvent(MainActivity.this,"SAMPLE_EVENT1", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1");
                jj.sendEvent(MainActivity.this,"SAMPLE_EVENT2", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1");
                jj.sendEvent(MainActivity.this,"SAMPLE_EVENT3", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1");
                jj.sendEvent(MainActivity.this,"SAMPLE_EVENT4", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1");
                jj.sendEvent(MainActivity.this,"SAMPLE_EVENT5", "Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1");

                jj.sendPriorityEvent("SAMPLE_EVENT6","Key=Jeta", "bcdefg", "hijkl", "mnopqrs", "tuvw", "xyz0", "1234", "567", "89", "2.1");

            }
        });


        //jj.sendPriorityEvent("SAMPLE_EVENT", "Key=Jeta","bcdefg","hijkl","mnopqrs","tuvw","xyz0","1234","567","89", "2.1");


        /**********************************************************************************************/
        //send events


        /**********************************************************************************************/
        //send all contacts
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 55);



        /**********************************************************************************************/
        //crash dump


        /**********************************************************************************************/
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
