package com.bosphere.filelogger;

import android.util.SparseArray;

/**
 * Created by bo on 23/9/17.
 */

public interface FLConst {

    String TAG = "FileLogger";

    interface Level {
        int V = 0;
        int D = 1;
        int I = 2;
        int W = 3;
        int E = 4;
    }

    SparseArray<String> LevelName = new SparseArray<String>(5) {{
        append(Level.V, "V");
        append(Level.D, "D");
        append(Level.I, "I");
        append(Level.W, "W");
        append(Level.E, "E");
    }};

    interface RetentionPolicy {
        int NONE = 0;
        int FILE_COUNT = 1;
        int TOTAL_SIZE = 2;
    }

    long DEFAULT_MAX_TOTAL_SIZE = 32 * 1024 * 1024; // 32mb
    int DEFAULT_MAX_FILE_COUNT = 24 * 7; // ~7 days of restless logging
}
