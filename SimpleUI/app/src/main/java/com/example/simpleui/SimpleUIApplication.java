package com.example.simpleui;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;

/**
 * Created by ggm on 12/14/15.
 */
public class SimpleUIApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
