package com.bosphere.filelogger;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by yangbo on 22/9/17.
 */

public class FL {

    private interface Level {
        int V = 1;
        int D = 2;
        int I = 3;
        int W = 4;
        int E = 5;
    }

    private volatile static boolean sEnabled;
    private static Context sAppContext;
    private static Loggable sLogger;
    private static FileFormatter sFormatter;
    private static File sDir;
    private static String sDefaultTag;

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, FLConfig config) {
        FLUtil.ensureUiThread();
        sAppContext = context.getApplicationContext();
        if (config != null) {
            sLogger = config.b.logger;
            sFormatter = config.b.formatter;
            sDir = config.b.dir;
            sDefaultTag = config.b.defaultTag;
        }

        if (sLogger == null) {
            sLogger = new DefaultLog();
        }
        if (sFormatter == null) {
            sFormatter = new DefaultFormatter();
        }
        if (sDir == null) {
            sDir = context.getExternalFilesDir("log");
        }
        if (TextUtils.isEmpty(sDefaultTag)) {
            sDefaultTag = FLUtil.getAppName(context);
        }
    }

    public static void v(String fmt, Object... args) {
        log(Level.V, null, FLUtil.format(fmt, args));
    }

    public static void v(String tag, String fmt, Object... args) {
        log(Level.V, tag, FLUtil.format(fmt, args));
    }

    public static void d(String fmt, Object... args) {
        log(Level.D, null, FLUtil.format(fmt, args));
    }

    public static void d(String tag, String fmt, Object... args) {
        log(Level.D, tag, FLUtil.format(fmt, args));
    }

    public static void i(String fmt, Object... args) {
        log(Level.I, null, FLUtil.format(fmt, args));
    }

    public static void i(String tag, String fmt, Object... args) {
        log(Level.I, tag, FLUtil.format(fmt, args));
    }

    public static void w(String fmt, Object... args) {
        log(Level.W, null, FLUtil.format(fmt, args));
    }

    public static void w(String tag, String fmt, Object... args) {
        log(Level.W, tag, FLUtil.format(fmt, args));
    }

    public static void e(String fmt, Object... args) {
        log(Level.E, null, FLUtil.format(fmt, args));
    }

    public static void e(String tag, String fmt, Object... args) {
        log(Level.E, tag, FLUtil.format(fmt, args));
    }

    public static void e(Throwable tr) {
        e(null, tr, null);
    }

    public static void e(String tag, Throwable tr) {
        e(tag, tr, null);
    }

    public static void e(String tag, Throwable tr, String fmt, Object... args) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(fmt)) {
            sb.append(FLUtil.format(fmt, args));
        }
        if (tr != null) {
            sb.append(FLUtil.formatThrowable(tr));
        }
        log(Level.E, tag, sb.toString());
    }

    private static void log(int level, String tag, String log) {
        if (!sEnabled) {
            return;
        }

        ensureStatus();

        if (TextUtils.isEmpty(tag)) {
            tag = sDefaultTag;
        }
        switch (level) {
            case Level.V:
                sLogger.v(tag, log);
                break;
            case Level.D:
                sLogger.d(tag, log);
                break;
            case Level.I:
                sLogger.i(tag, log);
                break;
            case Level.W:
                sLogger.w(tag, log);
                break;
            case Level.E:
                sLogger.e(tag, log);
                break;
        }
    }

    private static void ensureStatus() {
        if (sAppContext == null || sLogger == null) {
            throw new IllegalStateException(
                    "FileLogger is not initialized. Forgot to call init()?");
        }
    }

    public static class DefaultLog implements Loggable {

        @Override
        public void v(String tag, String log) {
            Log.v(tag, log);
        }

        @Override
        public void d(String tag, String log) {
            Log.d(tag, log);
        }

        @Override
        public void i(String tag, String log) {
            Log.i(tag, log);
        }

        @Override
        public void w(String tag, String log) {
            Log.w(tag, log);
        }

        @Override
        public void e(String tag, String log) {
            Log.e(tag, log);
        }

        @Override
        public void e(String tag, String log, Throwable tr) {
            Log.e(tag, log, tr);
        }
    }

    public static class DefaultFormatter implements FileFormatter {

        @Override
        public String formatLine(long timeInMillis, String tag, String content) {
            return null;
        }
    }
}
