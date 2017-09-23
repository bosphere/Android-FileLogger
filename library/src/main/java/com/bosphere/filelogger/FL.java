package com.bosphere.filelogger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by yangbo on 22/9/17.
 */

public class FL {

    private enum Level {
        V, D, I, W, E
    }

    private volatile static boolean sEnabled;
    private volatile static FLConfig sConfig;

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static void init(Context context) {
        init(new FLConfig.Builder(context).build());
    }

    public static void init(@NonNull FLConfig config) {
        sConfig = config;
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

    private static void log(Level level, String tag, String log) {
        if (!sEnabled) {
            return;
        }

        ensureStatus();

        FLConfig config = sConfig;
        if (TextUtils.isEmpty(tag)) {
            tag = config.b.defaultTag;
        }

        Loggable logger = config.b.logger;
        if (logger != null) {
            switch (level) {
                case V:
                    logger.v(tag, log);
                    break;
                case D:
                    logger.d(tag, log);
                    break;
                case I:
                    logger.i(tag, log);
                    break;
                case W:
                    logger.w(tag, log);
                    break;
                case E:
                    logger.e(tag, log);
                    break;
            }
        }

        if (config.b.logToFile && !TextUtils.isEmpty(config.b.dirPath)) {
            long timeMs = System.currentTimeMillis();
            String fileName = config.b.formatter.formatFileName(timeMs);
            String line = config.b.formatter.formatLine(timeMs, level.name(), tag, log);
            FileLoggerService.logFile(config.b.context, fileName, config.b.dirPath, line);
        }
    }

    private static void ensureStatus() {
        if (sConfig == null) {
            throw new IllegalStateException(
                    "FileLogger is not initialized. Forgot to call FL.init()?");
        }
    }
}
