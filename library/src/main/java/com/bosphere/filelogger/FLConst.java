package com.bosphere.filelogger;

/**
 * Created by bo on 23/9/17.
 */

public interface FLConst {

    String TAG = "FileLogger";

    enum Level {
        V, D, I, W, E
    }

    interface RetentionPolicy {
        int NONE = 0;
        int FILE_COUNT = 1;
        int TOTAL_SIZE = 2;
    }

    long DEFAULT_MAX_TOTAL_SIZE = 32 * 1024 * 1024; // 32mb
    int DEFAULT_MAX_FILE_COUNT = 24 * 7; // ~7 days of restless logging
}
