package com.example.simpleui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class OrderDetailActivity extends AppCompatActivity {

    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        String note = getIntent().getStringExtra("note");
        String storeInfo = getIntent().getStringExtra("storeInfo");

        address = storeInfo.split(",")[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Utils.getGeoCodingUrl(address);
                byte[] bytes = Utils.urlToBytes(url);
                String result = new String(bytes);
                double[] latLng = Utils.getLatLngFromJsonString(result);
                Log.d("debug", result);
                Log.d("debug", latLng[0] + "," + latLng[1]);

            }
        });
        thread.start();
    }
}
