DSLINK-JAVA-ALARM
=================

* Date: Aug 5, 2016
* Version: 0.0.4


Overview
--------

This is a [DSA link](https://github.com/IOT-DSA).  It records details about interesting 
events so they may be considered by humans.  The underlying model is influenced by the 
BACnet alarming design.  

This link was designed as a framework upon which alarm links for different data stores 
can be developed.  It includes two functional implementations:  
* org.dsa.iot.alarm.inMemory.MainA - Uses java collections for transient storage.
* org.dsa.iot.alarm.jdbc.Main - Uses JDBC for persistence.

There is also a separate implementation that uses an embedded instance
of H2: [dslink-java-alarm-h2](https://github.com/IOT-DSA/dslink-java-alarm-h2).

To understand link usage, view the [Alarm Link User Guide](https://github.com/IOT-DSA/dslink-java-alarm/raw/master/Alarm-Link-User-Guide.pdf).


Creating Custom Alarm Links
---------------------------

Creating a custom alarm link primarily requires implementing a single interface.  After 
that, plenty of hooks exist if customization of other types are required.

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


History
-------
_0.0.4 - 2016-8-5_
  - Many bug fixes while implementing [dslink-java-alarm-h2](https://github.com/IOT-DSA/dslink-java-alarm-h2).
  - Added acknowledge all actions to both alarm class and alarm service.
  - Added get alarms and get open alarms actions to the alarm service.
  - Escalation 2 is no relative to escalation 1.
  - Added a setLogLevel action to the service.
  - More code cleanup.
  
_0.0.3 - 2016-7-26_
  - Replaced most configs with value nodes.
  - Bug fixes

_0.0.2 - 2016-7-20_
  - Hello World
