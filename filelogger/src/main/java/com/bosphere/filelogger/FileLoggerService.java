package com.bosphere.filelogger;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by bo on 23/9/17.
 */

public class FileLoggerService {

    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            long lm1 = o1.lastModified();
            long lm2 = o2.lastModified();
            return lm1 < lm2 ? -1 : lm1 == lm2 ? 0 : 1;
        }
    };

    public static FileLoggerService instance() {
        return InstanceHolder.INSTANCE;
    }

    static class InstanceHolder {
        static final FileLoggerService INSTANCE = new FileLoggerService();
    }

    private final BlockingQueue<LogData> mQueue;
    private volatile boolean mIsRunning;

    FileLoggerService() {
        mQueue = new LinkedBlockingDeque<>();
    }

    public void logFile(Context context, String fileName, String dirPath, String line,
            int retentionPolicy, int maxFileCount, long maxTotalSize) {
        ensureThread();
        boolean addResult = mQueue.offer(new LogData.Builder().context(context)
                .fileName(fileName)
                .dirPath(dirPath)
                .line(line)
                .retentionPolicy(retentionPolicy)
                .maxFileCount(maxFileCount)
                .maxSize(maxTotalSize)
                .build());
        if (!addResult) {
            FL.w("failed to add to file logger service queue");
        }
    }

    private void ensureThread() {
        if (!mIsRunning) {
            synchronized (this) {
                if (!mIsRunning) {
                    mIsRunning = true;
                    FL.d("start file logger service thread");
                    new LogFileThread().start();
                }
            }
        }
    }

    private class LogFileThread extends Thread {

        private BufferedWriter mWriter;
        private String mPath;
        private int mRetentionPolicy;
        private int mMaxFileCount;
        private long mMaxSize;

        @Override
        public void run() {
            super.run();
            Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    throwable.printStackTrace();
                    mIsRunning = false;
                }
            });

            try {
                for (;;) {
                    LogData log = mQueue.take();
                    logLine(log);
                    collectParams(log);
                    while ((log = mQueue.poll(2, TimeUnit.SECONDS)) != null) {
                        logLine(log);
                        collectParams(log);
                    }

                    closeWriter();
                    startHouseKeeping();
                }
            } catch (InterruptedException e) {
                FL.e(e, "file logger service thread is interrupted");
            }

            FL.d("file logger service thread stopped");
            mIsRunning = false;
        }

        private void collectParams(LogData log) {
            mRetentionPolicy = log.retentionPolicy;
            mMaxFileCount = log.maxFileCount;
            mMaxSize = log.maxTotalSize;
        }

        private void logLine(LogData log) {
            if (TextUtils.isEmpty(log.fileName)) {
                throw new IllegalStateException("invalid file name: [" + log.fileName + "]");
            }

            if (TextUtils.isEmpty(log.dirPath)) {
                throw new IllegalStateException("invalid directory path: [" + log.dirPath + "]");
            }

            if (TextUtils.isEmpty(log.line)) {
                return;
            }

            File dir = new File(log.dirPath);
            if (!FLUtil.ensureDir(dir)) {
                return;
            }

            File f = new File(log.dirPath, log.fileName);
            String path = f.getAbsolutePath();
            if (mWriter != null && path.equals(mPath)) {
                try {
                    mWriter.write(log.line);
                    mWriter.write("\n");
                } catch (IOException e) {
                    FL.e(FLConst.TAG, e);
                }
            } else {
                closeWriter();
                FLUtil.ensureFile(f);
                try {
                    mWriter = createWriter(f);
                    mPath = f.getAbsolutePath();

                    mWriter.write(log.line);
                    mWriter.write("\n");
                } catch (IOException e) {
                    FL.e(FLConst.TAG, e);
                }
            }
        }

        private BufferedWriter createWriter(File file) throws IOException {
            // one line ~100 characters = ~100-400 bytes
            // use default buf size ~8k = ~20-80 lines
            return new BufferedWriter(new FileWriter(file, true));
        }

        private void startHouseKeeping() {
            if (TextUtils.isEmpty(mPath)) {
                return;
            }

            if (mRetentionPolicy == FLConst.RetentionPolicy.FILE_COUNT) {
                houseKeepByCount(mMaxFileCount);
            } else if (mRetentionPolicy == FLConst.RetentionPolicy.TOTAL_SIZE) {
                houseKeepBySize(mMaxSize);
            }
        }

        private void houseKeepByCount(int maxCount) {
            if (maxCount <= 0) {
                throw new IllegalStateException("invalid max file count: " + maxCount);
            }

            File file = new File(mPath);
            File dir = file.getParentFile();
            if (dir == null) {
                return;
            }

            File[] files = dir.listFiles();
            if (files == null || files.length <= maxCount) {
                return;
            }

            Arrays.sort(files, FILE_COMPARATOR);
            int deleteCount = files.length - maxCount;
            int successCount = 0;
            for (int i = 0; i < deleteCount; i++) {
                if (files[i].delete()) {
                    successCount++;
                }
            }
            FL.d(FLConst.TAG, "house keeping complete: file count [%d -> %d]", files.length,
                    files.length - successCount);
        }

        private void houseKeepBySize(long maxSize) {
            if (maxSize <= 0) {
                throw new IllegalStateException("invalid max total size: " + maxSize);
            }

            File file = new File(mPath);
            File dir = file.getParentFile();
            if (dir == null) {
                return;
            }

            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            long totalSize = 0;
            for (File f : files) {
                totalSize += f.length();
            }

            if (totalSize <= maxSize) {
                return;
            }

            Arrays.sort(files, FILE_COMPARATOR);
            long newSize = totalSize;
            for (File f : files) {
                long size = f.length();
                if (f.delete()) {
                    newSize -= size;
                    if (newSize <= maxSize) {
                        break;
                    }
                }
            }
            FL.d(FLConst.TAG, "house keeping complete: total size [%d -> %d]", totalSize, newSize);
        }

        private void closeWriter() {
            if (mWriter != null) {
                try {
                    mWriter.close();
                } catch (IOException e) {
                    FL.e(FLConst.TAG, e);
                }
                mWriter = null;
            }
        }
    }

    static class LogData {
        final Context context;
        final String fileName, dirPath, line;
        final int retentionPolicy, maxFileCount;
        long maxTotalSize;

        LogData(Builder b) {
            context = b.context;
            fileName = b.fileName;
            dirPath = b.dirPath;
            line = b.line;
            retentionPolicy = b.retentionPolicy;
            maxFileCount = b.maxFileCount;
            maxTotalSize = b.maxTotalSize;
        }

        static class Builder {
            Context context;
            String fileName, dirPath, line;
            int retentionPolicy, maxFileCount;
            long maxTotalSize;

            Builder context(Context context) {
                this.context = context;
                return this;
            }

            Builder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            Builder dirPath(String dirPath) {
                this.dirPath = dirPath;
                return this;
            }

            Builder line(String line) {
                this.line = line;
                return this;
            }

            Builder retentionPolicy(int retentionPolicy) {
                this.retentionPolicy = retentionPolicy;
                return this;
            }

            Builder maxFileCount(int maxFileCount) {
                this.maxFileCount = maxFileCount;
                return this;
            }

            Builder maxSize(long maxSize) {
                this.maxTotalSize = maxSize;
                return this;
            }

            LogData build() {
                return new LogData(this);
            }
        }
    }
}
