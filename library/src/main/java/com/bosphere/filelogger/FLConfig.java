package com.bosphere.filelogger;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yangbo on 22/9/17.
 */

public class FLConfig {

    Builder b;

    private FLConfig(Builder b) {
        this.b = b;
    }

    public static class Builder {

        final Context context;
        Loggable logger = new DefaultLog();
        FileFormatter formatter;
        String dirPath;
        String defaultTag;
        boolean logToFile;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder logger(Loggable logger) {
            this.logger = logger;
            return this;
        }

        public Builder formatter(FileFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder dir(File dir) {
            if (dir != null) {
                dirPath = dir.getAbsolutePath();
            }
            return this;
        }

        public Builder defaultTag(String tag) {
            this.defaultTag = tag;
            return this;
        }

        public Builder logToFile(boolean logToFile) {
            this.logToFile = logToFile;
            return this;
        }

        public FLConfig build() {
            if (TextUtils.isEmpty(defaultTag)) {
                defaultTag = FLUtil.getAppName(context);
            }
            if (logToFile) {
                if (formatter == null) {
                    formatter = new DefaultFormatter();
                }

                if (TextUtils.isEmpty(dirPath)) {
                    File dir = context.getExternalFilesDir("log");
                    if (dir != null) {
                        dirPath = dir.getAbsolutePath();
                    } else {
                        Log.e("FileLogger", "failed to resolve default log file directory");
                    }
                }
            }
            return new FLConfig(this);
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

        private final ThreadLocal<SimpleDateFormat> mTimeFmt = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
            }
        };

        private final ThreadLocal<SimpleDateFormat> mFileNameFmt = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("MM_dd_HH", Locale.ENGLISH);
            }
        };

        private final ThreadLocal<Date> mDate = new ThreadLocal<Date>() {
            @Override
            protected Date initialValue() {
                return new Date();
            }
        };

        // 09-23 12:31:53.839 THREAD_ID LEVEL/TAG: LOG
        private final String mLineFmt = "%s %d %s/%s: %s";

        @Override
        public String formatLine(long timeInMillis, String level, String tag, String log) {
            mDate.get().setTime(timeInMillis);
            String timestamp = mTimeFmt.get().format(mDate.get());
            long threadId = Thread.currentThread().getId();
            return String.format(Locale.ENGLISH, mLineFmt, timestamp, threadId, level, tag, log);
        }

        @Override
        public String formatFileName(long timeInMillis) {
            mDate.get().setTime(timeInMillis);
            return mFileNameFmt.get().format(mDate.get()) + "_00.txt";
        }
    }
}
