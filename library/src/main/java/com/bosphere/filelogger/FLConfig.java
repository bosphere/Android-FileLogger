package com.bosphere.filelogger;

import java.io.File;

/**
 * Created by yangbo on 22/9/17.
 */

public class FLConfig {

    Builder b;

    private FLConfig(Builder b) {
        this.b = b;
    }

    public static class Builder {

        Loggable logger;
        FileFormatter formatter;
        File dir;
        String defaultTag;

        public Builder logger(Loggable logger) {
            this.logger = logger;
            return this;
        }

        public Builder formatter(FileFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder dir(File dir) {
            this.dir = dir;
            return this;
        }

        public Builder defaultTag(String tag) {
            this.defaultTag = tag;
            return this;
        }

        public FLConfig build() {
            return new FLConfig(this);
        }
    }
}
