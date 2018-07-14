Change Log
============================

## Version 1.0.6
_2018-07-14_
+ Flush buffer to file immediately when logging at error-level

## Version 1.0.5
_2018-05-05_
+ Support customize minimum logging level

## Version 1.0.4
_2018-03-09_
+ Fix file logging might lead to crashes when file system is unavailable
+ Use Android's Log utility to format throwable

## Version 1.0.3
_2017-12-23_
+ Comply with background execution limit imposed in Android Oreo

## Version 1.0.2
_2017-11-20_
+ Delimit custom message and stack trace with a new line when using `FL.e(String tag, Throwable tr, String fmt, Object... args)`

## Version 1.0.1
_2017-09-24_
+ Initial release