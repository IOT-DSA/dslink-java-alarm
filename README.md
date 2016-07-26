DSLINK-JAVA-ALARM
=================

Date: July 26, 2016
Version: 0.0.2


Overview
--------

This is a [DSA link](https://github.com/IOT-DSA).  It records details about interesting events so they may be considered by humans.  The underlying model is influenced by the BACnet alarming design.  

This link was designed as a framework upon which alarm links for different data stores can be developed.  This module includes two example implementations:  org.dsa.iot.alarm.inMemory.Main and org.dsa.iot.alarm.jdbc.Main.  

To understand link usage, view the [Alarm Link User Guide](Alarm-Link-User-Guide.pdf).


Creating Custom Alarm Links
---------------------------

Creating a custom link primarily requires implementing a single interface.  After that, plenty of hooks exist if customizations of other types is required.

1.	Create an implementation of org.iot.dsa.alarm.Alarming.Provider.
2.	Create a “main” class that subclasses org.iot.dsa.alarm.AlarmLinkHandler.
3.	In the main method of the Main class:
..1.	Call Alarming.setProvider with an instance of your provider.
..2.	Call DSLinkFactory.start with an instance of your main class.

For example:

```java
public static void main(String[] args) 
{
  Alarming.setProvider(new RemoteJdbcProvider());
  DSLinkFactory.start(args, new Main());
}
```


History
-------
0.0.3 - 2016-7-26
  - Replaced most configs with value nodes.
  - Bug fixes

0.0.2 - 2016-7-20
  - First checkin
