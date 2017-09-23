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

/**
 * Created by bo on 23/9/17.
 */

public class FileLoggerService extends IntentService {

    private static final String TAG = "file-logger";
    private static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    private static final String EXTRA_DIR_PATH = "EXTRA_DIR_PATH";
    private static final String EXTRA_LINE = "EXTRA_LINE";

    private BufferedWriter mWriter;
    private String mPath;

    static void logFile(Context context, String fileName, String dirPath, String line) {
        Intent intent = new Intent(context, FileLoggerService.class);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_DIR_PATH, dirPath);
        intent.putExtra(EXTRA_LINE, line);
        context.startService(intent);
    }

    public FileLoggerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
        String dirPath = intent.getStringExtra(EXTRA_DIR_PATH);
        String line = intent.getStringExtra(EXTRA_LINE);

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
                FL.e(TAG, e);
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
                FL.e(TAG, e);
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
    }

    private void closeWriter() {
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException e) {
                FL.e(TAG, e);
            }
        }
    }
}
