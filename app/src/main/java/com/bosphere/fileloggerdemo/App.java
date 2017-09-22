package com.bosphere.fileloggerdemo;

import android.app.Application;

import com.bosphere.filelogger.FL;

/**
 * Created by yangbo on 22/9/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FL.init(this);
        FL.setEnabled(true);
    }
}
