package com.bosphere.filelogger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.util.Locale;

/**
 * Created by yangbo on 22/9/17.
 */

class FLUtil {

    static String format(String fmt, Object... args) {
        if (args == null || args.length == 0) {
            return fmt;
        }

        return String.format(Locale.ENGLISH, fmt, args);
    }

    static String getAppName(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        int stringRes = info.labelRes;
        if (stringRes > 0) {
            return context.getString(stringRes);
        } else if (!TextUtils.isEmpty(info.nonLocalizedLabel)) {
            return info.nonLocalizedLabel.toString();
        } else {
            return "App";
        }
    }

    static void ensureUiThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("UI thread only");
        }
    }

    static boolean ensureDir(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return true;
            }

            if (!dir.delete()) {
                FL.w("failed to delete file that occupies log dir path: [" +
                        dir.getAbsolutePath() + "]");
                return false;
            }
        }

        if (!dir.mkdir()) {
            FL.w("failed to create log dir: [" + dir.getAbsolutePath() + "]");
            return false;
        }

        return true;
    }

    static void ensureFile(File file) {
        if (file.exists() && !file.isFile() && !file.delete()) {
            throw new IllegalStateException(
                    "failed to delete directory that occupies log file path: [" +
                            file.getAbsolutePath() + "]");
        }
    }
}
