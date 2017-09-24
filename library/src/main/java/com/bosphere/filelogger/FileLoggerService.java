package com.bosphere.filelogger;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by bo on 23/9/17.
 */

public class FileLoggerService extends IntentService {

    private static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    private static final String EXTRA_DIR_PATH = "EXTRA_DIR_PATH";
    private static final String EXTRA_LINE = "EXTRA_LINE";
    private static final String EXTRA_RETENTION_POLICY = "EXTRA_RETENTION_POLICY";
    private static final String EXTRA_MAX_FILE_COUNT = "EXTRA_MAX_FILE_COUNT";
    private static final String EXTRA_MAX_SIZE = "EXTRA_MAX_SIZE";
    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            long lm1 = o1.lastModified();
            long lm2 = o2.lastModified();
            return lm1 < lm2 ? -1 : lm1 == lm2 ? 0 : 1;
        }
    };

    private int mRetentionPolicy;
    private int mMaxFileCount;
    private long mMaxSize;
    private BufferedWriter mWriter;
    private String mPath;

    static void logFile(Context context, String fileName, String dirPath, String line,
            int retentionPolicy, int maxFileCount, long maxTotalSize) {
        Intent intent = new Intent(context, FileLoggerService.class);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_DIR_PATH, dirPath);
        intent.putExtra(EXTRA_LINE, line);
        intent.putExtra(EXTRA_RETENTION_POLICY, retentionPolicy);
        intent.putExtra(EXTRA_MAX_FILE_COUNT, maxFileCount);
        intent.putExtra(EXTRA_MAX_SIZE, maxTotalSize);
        context.startService(intent);
    }

    public FileLoggerService() {
        super("file-logger");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
        String dirPath = intent.getStringExtra(EXTRA_DIR_PATH);
        String line = intent.getStringExtra(EXTRA_LINE);
        mRetentionPolicy = intent.getIntExtra(EXTRA_RETENTION_POLICY, FLConst.RetentionPolicy.NONE);
        mMaxFileCount = intent.getIntExtra(EXTRA_MAX_FILE_COUNT, 0);
        mMaxSize = intent.getLongExtra(EXTRA_MAX_SIZE, 0L);

        if (TextUtils.isEmpty(fileName)) {
            throw new IllegalStateException("invalid file name: [" + fileName + "]ø");
        }

        if (TextUtils.isEmpty(dirPath)) {
            throw new IllegalStateException("invalid directory path: [" + dirPath + "]");
        }

        if (TextUtils.isEmpty(line)) {
            return;
        }

        File dir = new File(dirPath);
        FLUtil.ensureDir(dir);
        logLine(dir, fileName, line);
    }

    private void logLine(File dir, String fileName, String line) {
        File f = new File(dir, fileName);
        String path = f.getAbsolutePath();
        if (mWriter != null && path.equals(mPath)) {
            try {
                mWriter.write(line);
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

                mWriter.write(line);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeWriter();
        startHouseKeeping();
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
        }
    }
}
