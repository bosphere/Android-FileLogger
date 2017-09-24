
Android-FileLogger
============================

A general-purpose logging library with built-in support to save logs to file.

Usage
-----
```gradle
dependencies {
    compile 'com.github.bosphere.android-filelogger:filelogger:1.0.0'
}
```

```java
FL.init(new FLConfig.Builder(this)
        .logger()          // customise how to hook up with logcat
        .defaultTag()      // customise default tag
        .logToFile(true)   // enable logging to file
        .dir()             // customise directory to hold log files
        .retentionPolicy() // customise retention strategy 
        .build());
        
        
// overall toggle to enable/disable logging
FL.setEnabled(true);
```

Compatibility
-------------

API 9 (Android 2.3) and up

License
-------

Copyright 2017 Yang Bo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.