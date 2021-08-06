DSLINK-JAVA-ALARM
=================

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