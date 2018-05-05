package com.bosphere.filelogger;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.bosphere.filelogger.FLConst.RetentionPolicy.FILE_COUNT;
import static com.bosphere.filelogger.FLConst.RetentionPolicy.TOTAL_SIZE;

/**
 * Created by yangbo on 22/9/17.
 */

public class FLConfig {

    final Builder b;

    private FLConfig(Builder b) {
        this.b = b;
    }

    public static class Builder {

        final Context context;
        Loggable logger = new DefaultLog();
        FileFormatter formatter;
        String dirPath;
        String defaultTag;
        int minLevel = FLConst.Level.V;
        boolean logToFile;
        int retentionPolicy = FILE_COUNT;
        int maxFileCount = FLConst.DEFAULT_MAX_FILE_COUNT;
        long maxSize = FLConst.DEFAULT_MAX_TOTAL_SIZE;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * Defines how to output to logcat. {@link DefaultLog} is used by default. Pass {@code NULL} to disable output to logcat.
         *
         * @param logger
         * @return
         */
        public Builder logger(Loggable logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Defines how each log looks in file, as well as how log files are named.
         *
         * @param formatter
         * @return
         */
        public Builder formatter(FileFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        /**
         * Defines the default log file directory.
         *
         * @param dir
         * @return
         */
        public Builder dir(File dir) {
            if (dir != null) {
                dirPath = dir.getAbsolutePath();
            }
            return this;
        }

        /**
         * Defines the default tag to use.
         *
         * @param tag
         * @return
         */
        public Builder defaultTag(String tag) {
            this.defaultTag = tag;
            return this;
        }

        /**
         * Defines the minimum logging level. Default is {@link com.bosphere.filelogger.FLConst.Level#V}.
         *
         * @param level
         * @return
         */
        public Builder minLevel(int level) {
            this.minLevel = level;
            return this;
        }

        /**
         * Defines whether to enable logging to files.
         *
         * @param logToFile
         * @return
         */
        public Builder logToFile(boolean logToFile) {
            this.logToFile = logToFile;
            return this;
        }

        /**
         * Defines how log files are managed when exceeding limit. Currently supports limit by file count or total size.
         *
         * @param retentionPolicy For possible values refer to {@link com.bosphere.filelogger.FLConst.RetentionPolicy}
         * @return
         */
        public Builder retentionPolicy(int retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
            return this;
        }

        /**
         * Defines at maximum how many log files are allowed to be retained.
         *
         * @param maxFileCount
         * @return
         */
        public Builder maxFileCount(int maxFileCount) {
            this.maxFileCount = maxFileCount;
            return this;
        }

        /**
         * Defines at maximum how much space log files can occupy before trimming.
         *
         * @param maxSize
         * @return
         */
        public Builder maxTotalSize(long maxSize) {
            this.maxSize = maxSize;
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
                        Log.e(FLConst.TAG, "failed to resolve default log file directory");
                    }
                }

                if (retentionPolicy < 0) {
                    throw new IllegalArgumentException("invalid retention policy: " + retentionPolicy);
                }

                switch (retentionPolicy) {
                    case FILE_COUNT:
                        if (maxFileCount <= 0) {
                            throw new IllegalArgumentException("max file count must be > 0");
                        }
                        break;
                    case TOTAL_SIZE:
                        if (maxSize <= 0) {
                            throw new IllegalArgumentException("max total size must be > 0");
                        }
                        break;
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

        // 09-23 12:31:53.839 PROCESS_ID-THREAD_ID LEVEL/TAG: LOG
        private final String mLineFmt = "%s %d-%d %s/%s: %s";

        @Override
        public String formatLine(long timeInMillis, String level, String tag, String log) {
            mDate.get().setTime(timeInMillis);
            String timestamp = mTimeFmt.get().format(mDate.get());
            int processId = Process.myPid();
            int threadId = Process.myTid();
            return String.format(Locale.ENGLISH, mLineFmt, timestamp, processId, threadId, level,
                    tag, log);
        }

        @Override
        public String formatFileName(long timeInMillis) {
            mDate.get().setTime(timeInMillis);
            return mFileNameFmt.get().format(mDate.get()) + "_00.txt";
        }
    }
}
