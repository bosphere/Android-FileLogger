package com.bosphere.filelogger;

/**
 * Created by yangbo on 22/9/17.
 */

public interface FileFormatter {
    String formatLine(long timeInMillis, String tag, String content);
}
