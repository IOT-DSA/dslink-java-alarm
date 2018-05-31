DSLINK-JAVA-ALARM
=================

* Date: May 31, 2018
* Version: 1.8.0


Overview
--------

This is a [DSA link](https://github.com/IOT-DSA).  It records details
 about interesting events so they may be considered by humans.  The 
 underlying model is influenced by the BACnet alarming design.  

This link serves two purposes:  

1.  It is a framework upon which alarm links for different data stores can be developed.
2.  It has default implementations that can be deployed as-is.

To understand link usage, view the [Alarm Link User Guide](https://github.com/IOT-DSA/dslink-java-alarm/blob/master/Alarm-Link-User-Guide.pdf).


Requirements
------------

Java 1.7 or higher is required.


Using the Provided Implementations
----------------------------------

To use an implementation, one has the change the handler_class in
dslink.json and/or the main class used to launch your application.

The provided implementations are:
* org.dsa.iot.alarm.jdbc.H2Main - Uses an embedded instance of the H2
database for persistence.  This is default handler_class in dslink.json.
* org.dsa.iot.alarm.jdbc.JdbcMain - Uses remote JDBC connections for 
persistence.  The specific JDBC driver jar file will need to be added to the
deployment.


Creating Custom Alarm Links
---------------------------

Creating a custom alarm link primarily requires implementing a single 
interface.  After that, plenty of hooks exist if customization of other 
types is required.

1.	Create an implementation of org.iot.dsa.alarm.Alarming.Provider.
2.	Create a "main" class that subclasses org.iot.dsa.alarm.AlarmLinkHandler.
3.	In the main Main class:
    1. In a static initializer, call Alarming.setProvider with an instance of your provider.
    2. In the main method, call DSLinkFactory.start with an instance of the main class.

For example:

```java
static {
  Alarming.setProvider(new MyProvider());
}
public static void main(String[] args) {
  DSLinkFactory.start(args, new MyMainClass());
}
```


Acknowledgements
----------------
_H2 Database_

This software contains unmodified binary redistributions for [H2 
database engine](http://www.h2database.com/), which is dual licensed 
and available under the MPL 2.0 (Mozilla Public License) or under the 
EPL 1.0 (Eclipse Public License). An original copy of the license 
agreement can be found at: http://www.h2database.com/html/license.html

_Silk Icons_

This software uses icons from Silk Icons 1.3 created by 
[Mark James](http://www.famfamfam.com/lab/icons/silk/) and licensed 
under a [Creative Commons Attribute 2.5 License](http://creativecommons.org/licenses/by/2.5/).

History
-------
_1.8.0 - 2018-5-21
  - Added Get Alarm Page Count to the service and class objects.

_1.7.0 - 2018-5-25_
  - Added Get Alarm Page to the service and class objects.

_1.6.0 - 2018-4-06
  - Bug fix for watch deletion.
  - Buf fix where alarms were not be acknowledged.
  - Bug fix for purging old alarms.

_1.5.0 - 2017-10-16_
  - Alarm watches can now be disabled.
  
_1.4.2 - 2017-09-21_
  - Counts automatically updated on acks and deletes.
  
_1.4.1 - 2017-09-19_
  - Added ability to run the database in server mode.
  - Added ability to change the database user and pass.
  
_1.3.1 - 2017-09-08_
  - Fixed icons.
  - Added new counts logic to the Alarm Service and Alarm Class.
  - Fixed potential db connection leak in AlarmStreamer.
  - Get open alarms now takes option that determines whether or not to keep an open alarm stream.
  
_1.3.0 - 2017-07-20_
  - Normal and alarm watch counts maintained in algorithms and alarm class.
  - Watches sync'd to database at startup.  Sometimes a link is restarted before the configuration
    database has been saved.

_1.2.0 - 2017-07-12_
  - Alarms streams now use append mode rather than stream.

_1.1.6 - 2017-07-11_
  - Enhance acknowledge alarms with comma-separated UUIDs.

_1.1.4 - 2017-06-13_
  - Null current values become "null" in the String algorithm.
  
_1.1.3 - 2017-06-13_
  - Added deadband to the out of range alg.
  - Out of range algorithm will now convert strings and booleans to numbers.
  - Fix watches not tracking changes to alarm type on the parent algorithm.
  - Fix unable to delete alarm objects with names containing special characters.
  - Fix boolean algorithm when value is a string.
  - Fix inhibits.
  
_1.0.0 - 2017-04-10_
  - Alarm auto purge options on Alarm Class.
  
_0.6.0 - 2016-12-12_
  - There can now be multiple watches of the same path.
  - Added two new columns to the alarm tables: Is_Normal and Is_Acknowledged.
  - All alarm table column names changed by replacing spaces with underscore.
  
_0.5.0 - 2016-12-01_
  - Added Watch Path to alarm tables.
  - Made the alarm message configurable on each algorithm.
  
_0.4.0 - 2016-11-22_
  - Update SDK dependency.
  - Added icons.
  
_0.3.0 - 2016-10-03_
  - Now compatible with JDK 1.7
  - If a stream such as open alarms was closed and another opened
    soon afterwards, the new stream would not receive any updates.
  - Acknowledge all now acks alerts even though they don't require it.
  - Fix property change handlers in algorithms to call super and
    only react while in the steady state.
  
_0.2.0 - 2016-8-26_
  - Added StringAlgorithm.
  - Boolean algorithms will now evaluate numbers and strings.
  - User guide now has Initial Setup.
  
_0.1.3 - 2016-8-25_
  - Fix algorithmic return to normal.
  - Fix inhibit logic.
  
_0.1.2 - 2016-8-25_
  - Boolean Algorithm wasn't working.
  - Last Alarm Record property on watches wasn't being updated.
  - More documentation refinements.
  
_0.1.1 - 2016-8-25_
  - Fixed main class in build.gradle.
  - More documentation cleanup.
  
_0.1.0 - 2016-8-11_
  - Moved the H2 implementation into this project and made it the
  default handler class.
  
_0.0.4 - 2016-8-5_
  - Many bug fixes while implementing [dslink-java-alarm-h2](https://github.com/IOT-DSA/dslink-java-alarm-h2).
  - Added acknowledge all actions to both alarm class and alarm service.
  - Added get alarms and get open alarms actions to the alarm service.
  - Escalation 2 is now relative to escalation 1.
  - Added a setLogLevel action to the service.
  - More code cleanup.
  
_0.0.3 - 2016-7-26_
  - Replaced most configs with value nodes.
  - Bug fixes

_0.0.2 - 2016-7-20_
  - Hello World
