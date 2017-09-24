package com.bosphere.fileloggerdemo;

import android.app.Application;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;

/**
 * Created by yangbo on 22/9/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FL.init(new FLConfig.Builder(this)
                .logToFile(true)
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);
    }
}
