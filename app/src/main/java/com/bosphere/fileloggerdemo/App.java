package com.bosphere.fileloggerdemo;

import android.app.Application;
import android.os.Environment;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;

import java.io.File;

/**
 * Created by yangbo on 22/9/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FL.init(new FLConfig.Builder(this)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), "file_logger_demo"))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);
    }
}
