<font color="#5b9bd5"><font face="Calibri Light, serif"><font style="font-size: 26pt" size="6">Alarm Link</font></font></font>

<font color="#595959"><font style="font-size: 10pt" size="2">dslink-java-alarm</font></font>

# <a name="_Toc456677160"></a>Overview

The alarm link records details about interesting events so they may be considered by humans. The underlying model is influenced by the BACnet alarming design.

This link was designed as a framework upon which alarm links for different data stores can be developed. There are also two implementations: org.dsa.iot.alarm.inMemory.Main and org.dsa.iot.alarm.jdbc.Main.

## <a name="_Toc456677161"></a>Link Structure

The nodes of this link follow this hierarchy. Descriptions of each can be found in the Component Guide.

*   Alarm Service – The single root node of the link.

    *   Alarm Class – There can be many alarm classes in a link, each representing some sort of grouping criteria such as location, system or users.

        *   Alarm Algorithm – There can be many algorithms per alarm class. Each algorithm has its own logic for determining when an alarm condition exists.

            *   Alarm Watch – Path and meta-data about an entity being watched by the parent algorithm.

## <a name="_Toc456677162"></a>Link Usage

The purpose of this link is to create and manage alarms.

### <a name="_Toc456677163"></a>Creating Alarms

*   Action invocation – Alarm sources can invoke an action on an alarm class to create an alarm.

*   Alarm algorithms – Algorithms, such as “out of range” can subscribe to data sources and monitor their condition.

### <a name="_Toc456677164"></a>Receiving Alarms

*   Viewing – The alarm class has actions for retrieving alarm records.

*   Notifications – The alarm class has actions for receiving streams of new alarms, state changes as well as escalations.

### <a name="_Toc456677165"></a>Managing Alarms

*   Acknowledgement – Fault and offnormal alarms require acknowledgement. Acknowledgement can be achieved with an action on the Alarm Service.

*   Return to normal – All alarms must return to normal before they can be closed, this can be achieved with an action on the Alarm Service or an alarm algorithm automatically detecting it.

*   Closing – An alarm is considered closed when normal and acknowledged (unless it is an alert which does not require acknowledgement).

## <a name="_Toc456677166"></a>Alarm States

An alarm source is an entity that can be in an alarm condition. There are four possible states a source can be in.

### <a name="_Toc456677167"></a>Alert

An alert is informational, it does not require acknowledgement. Once an alarm source in alert returns to normal, an operator would not see the alert on their console unless explicitly queried.

### <a name="_Toc456677168"></a>Fault

Faults represent a malfunction or failure within the system. To close a fault, it must return to the normal state and be acknowledged.

### <a name="_Toc456677169"></a>Normal

Normal is healthy, and none of the other states.

### <a name="_Toc456677170"></a>Offnormal

Offnormal represents an unexpected condition, or something outside the bounds of normal operation. To close an offnormal alarm, it must return to the normal state and be acknowledged.

# <a name="_Toc456677171"></a>Component Guide

This section documents the major components of the link.

## <a name="_Toc456677172"></a>Alarm Service

This is the visible root node of the link. Its purpose is to create alarm classes and manage alarm records independent of alarm class.

### <a name="_Toc456677173"></a>Properties

*   Enabled - When false, no new alarms will be created.

### <a name="_Toc456677174"></a>Actions

*   Acknowledge – This updates an open alarm record.

    *   Parameters

        *   UUID – Required unique alarm ID.

        *   User – Entity performing the update.

*   Add Alarm Class – Add a new Alarm Class.

    *   Parameters

        *   Name – The alarm class name.

*   Add Note – Add a note to an existing alarm.

    *   Parameters

        *   UUID – Alarm record.

        *   User – User name.

        *   Note – Test message.

*   Delete All Records – Deletes all records from the database.

*   Delete Record – Deletes all records for the given UUIDs.

    *   Parameters

        *   UUID – Specific alarm id

*   Get Alarm – Return a single row table representing the alarm record for the give UUID.

    *   Parameters

        *   UUID – Specific alarm id

    *   Return – a single record. See the alarm record section of this document.

*   Get Notes – This returns a table of notes for a specific alarm. The columns are:

    *   Parameters

        *   UUID – Specific alarm id

    *   Return – a table with the following columns:

        *   Timestamp – The time of the alarm.

        *   User – The entity providing the note.

        *   Note – The text of the note.

*   Return To Normal – This returns an alarm record to its normal state. It has no effect on records that are already normal.

    *   Parameters

        *   UUID – The record to return to the normal state.

## <a name="_Toc456677175"></a>Alarm Class

An alarm class represents a group of alarms that are related in some way. Alarms can only be created with an alarm class but other alarm lifecycle operations are handled on the service.

The alarm class offers many streams (as actions) for monitoring various states of alarms including escalation. Escalation happens when alarm goes unacknowledged for a certain period of time and can be used to notify backup or higher seniority staff.

### <a name="_Toc456677176"></a>Properties

*   Enabled – When false, no new alarms will be created.

*   Escalation 1 Days – The number of days to add to the escalation duration. If the total duration is zero or less, the escalation level will be disabled.

*   Escalation 1 Hours – The number of hours to add to the escalation duration. If the total duration is zero or less, escalation level will be disabled.

*   Escalation 1 Minutes – The number of minutes to add to the escalation duration. If the total duration is zero or less, escalation level will be disabled.

*   Escalation 2 Days – The number of days to add to the escalation duration. If the total duration is zero or less, escalation level will be disabled.

*   Escalation 2 Hours – The number of hours to add to the escalation duration. If the total duration is zero or less, escalation level will be disabled.

*   Escalation 2 Minutes – The number of minutes to add to the escalation duration. If the total duration is zero or less, escalation level will be disabled.

### <a name="_Toc456677177"></a>Actions

*   Add Algorithm – This adds an algorithm for generating alarms.

    *   Parameters

        *   Name – The alarm class name.

        *   Type – The specific alarm class desired.

*   Create Alarm – Creates a new alarm record.

    *   Parameters

        *   Source Path – Path to the alarm source.

        *   Create State – Alert, Fault or Offnormal

        *   Message – Short text description.

    *   Returns – a one row table representing the alarm record.

        *   See the alarm record section of this document.

*   Delete Alarm Class – Removes the alarm class and its child nodes.

*   Get Alarms – Returns a table of alarm for this alarm class.

    *   Parameters

        *   Time Range – A DSA time range.

    *   Returns – a table of alarm records.

        *   See the alarm record section of this document.

*   Get Open Alarms – This returns a table of open alarm alarms for this alarm class. The table stream can remain open and any updates as well as new records will be passed along. The primary intent of this is for an alarm console.

    *   Returns – a stream of table rows representing the alarm records. The stream state will switch to open once the initial set of open alarms is sent.

        *   See the alarm record section of this document.

*   Stream Escalation 1 – Returns a stream of alarm records as they escalate in real time.

    *   Returns – table rows representing the alarm records.

        *   See the alarm record section of this document.

*   Stream Escalation 2 – Returns a stream of alarm records as they escalate in real time.

    *   Returns – table rows representing the alarm records.

        *   See the alarm record section of this document.

*   Stream New Alarms– This returns a stream of new alarm records for this alarm class. The table stream will remain open and any updates and new records will be passed along.

    *   Returns – table rows representing the alarm records.

        *   See the alarm record section of this document.

*   Rename Alarm Class – Changes the name of the alarm class, all records will reflect the change.

    *   Parameters

        *   Name – The new name.

## <a name="_Toc456677178"></a>Alarm Algorithms

Alarm algorithms evaluates the state of Alarm Watch objects, and generate alarms for each when the conditions of the algorithm are met. This describes the common functionality of all alarm algorithms, individual algorithms will be described in a separately.

### <a name="_Toc456677179"></a>Properties

The properties of an algorithm will be specific to its type.

*   Enabled – When false, no new records will be created.

*   Alarm Type – What type of alarm this algorithm creates: alert, fault or offnormal.

*   Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval. An auto interval should be used if using inhibits. Watches will always update themselves they detect a change of value on the source.

*   To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

### <a name="_Toc456677180"></a>Actions

*   Add Watch – Takes path for subscription in the parent broker.

*   Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

*   Set Auto Update Interval – Sets auto update interval property in seconds. Use zero to disable.

*   Delete Algorithm – Remove the algorithm from the parent alarm class.

## <a name="_Toc456677181"></a>Alarm Watch

Represents an alarm source that an algorithm will monitor for alarm conditions. There is a primary alarm source, but other paths may be used by subclasses for determining more complex conditions.

### <a name="_Toc456677182"></a>Properties

The properties of an algorithm will be specific to its type.

*   Enabled – When false, no new records will be created.

*   Source Path – The path to the primary alarmable entity.

*   Alarm State – The current state of the source.

*   Alarm State Time – The best known time that the source transitioned to the alarm state.

*   Last Alarm Record www– The UUID of the last related alarm record.

*   Last Cov – The timestamp of the last change of value of the source.

### <a name="_Toc456677183"></a>Actions

*   Delete Watch – Remove the watch from the parent algorithm.

## <a name="_Toc456677184"></a>Alarm Record

An alarm record represents details about an alarm. This is an abstract description of the Java class as well table columns in the DSA protocol.

### <a name="_Toc456677185"></a>PROPERTIES

*   UUID – Unique ID, generated by the link.

*   Source – Path to the alarm source.

*   Alarm Class – The name of the alarm class the record was created in.

*   Created Time – Timestamp of creation.

*   Created State – The state of the source at creation. Possible values are:

    *   Alert – Informational, acknowledge not required.

    *   Fault – A malfunction representing a failure within the system.

    *   Offnormal – An unexpected condition, or outside the bounds of normal operation.

*   Normal Time – If not null, the timestamp that the source returned to normal.

*   Ack Time – If not null, the timestamp of acknowledgement.

*   Ack User – The entity that acknowledged the alarm.

*   Message – Text describing the alarm at the time of creation.

*   Has Notes – Whether or not the alarm has any notes associated with it.

## <a name="_Toc456677186"></a>Boolean Algorithm

This algorithm creates alarms when boolean sources turn true or false.

### <a name="_Toc456677187"></a>Properties

The properties of an algorithm will be specific to its type.

*   Enabled – When false, no new records will be created.

*   Alarm Type – What type of alarm this algorithm creates: alert, fault or offnormal.

*   Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval. Will always update when the watch detects a change of value on the source.

*   To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   Alarm Value – What to alarm on, true or false.

### <a name="_Toc456677188"></a>Actions

*   Add Watch – Takes path for subscription in the parent broker.

*   Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

*   Set Auto Update Interval – Sets auto update interval property in seconds. Use zero to disable.

*   Update All – Re-evaluate all child watches.

*   Delete Algorithm – Remove the algorithm from the parent alarm class.

<a name="_Toc456677189"></a><span style="text-transform: uppercase">Out Of Range Algorithm</span>

This algorithm creates alarms for sources whose numeric value is less than a minimum value, or greater than a maximum value.

<a name="_Toc456677190"></a><span style="text-transform: uppercase"><font color="#1f4d78">Properties</font></span>

The properties of an algorithm will be specific to its type.

*   Enabled – When false, no new records will be created.

*   Alarm Type – Enum indicating whether records should be alert, fault or offnormal.

*   Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval. Will always update when the watch detects a change of value on the source.

*   To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   Min Value – Value to use if Use Node Range is false, or the target node does not define a min value.

*   Max Value – Value to use if Use Node Range is false, or the target node does not define a max value.

<a name="_Toc456677191"></a><span style="text-transform: uppercase"><font color="#1f4d78">Actions</font></span>

*   Add Watch – Takes path for subscription in the parent broker.

*   Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

*   Set Auto Update Interval – Sets auto update interval property in seconds. Use zero to disable.

*   Update All – Re-evaluate all child watches.

*   Delete Algorithm – Remove the algorithm from the parent alarm class.

<a name="_Toc456677192"></a><span style="text-transform: uppercase">Remote JDBC Service</span>

This is an alarm service with a connection to a remote database. All properties and actions are inherited from the base Alarm Service.

<a name="_Toc456677193"></a><span style="text-transform: uppercase"><font color="#1f4d78">Properties</font></span>

The following are unique to this type.

*   JDBC Driver – Class name of the driver.

*   Database URL – Enum indicating whether records should be alert, fault or offnormal.

*   Database Name – The database will be created if it does not already exist.

*   Database User – Credentials to access the database base. If blank, will only attempt to acquire a connection using the URL.

*   Database Pass – Password for the database user.

## <a name="_Toc456677194"></a>Stale Algorithm

This algorithm creates alarms for sources whose value does not change after a certain period of time. This can be useful for detecting sensor failure.

### <a name="_Toc456677195"></a>Properties

The properties of an algorithm will be specific to its type.

*   Enabled – When false, no new records will be created.

*   Alarm Type – What type of alarm this algorithm creates: alert, fault or offnormal.

*   Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval. Will always update when the watch detects a change of value on the source.

*   To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected. This can help minimize alarm creation. Use zero to disable, otherwise you should have a positive auto update interval.

*   Stale Days – The number of days to add to the stale duration.

*   Stale Hours – The number of hours to add to the stale duration.

*   Stale Minutes – The number of minutes to add to the stale duration.

### <a name="_Toc456677196"></a>Actions

*   Add Watch – Takes path for subscription in the parent broker.

*   Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

*   Set Auto Update Interval – Sets auto update interval property in seconds. Use zero to disable.

*   Update All – Re-evaluate all child watches.

*   Delete Algorithm – Remove the algorithm from the parent alarm class.

# <a name="_Toc456677197"></a>Creating a Custom Alarm Link

Creating a custom link primarily requires implementing a single interface. After that, plenty of hooks exist if customizations of other built in types are required.

1.  Create an implementation of org.iot.dsa.alarm.Alarming.Provider.

2.  Create a “main” class that subclasses org.iot.dsa.alarm.AlarmLinkHandler.

3.  In the main method of the Main class:

    1.  Call Alarming.setProvider with an instance of your provider.

    2.  Call DSLinkFactory.start with an instance of your main class.

For example:

<table bgcolor="#f2f2f2" cellpadding="7" cellspacing="0" width="623"><colgroup><col width="607"></colgroup>

<tbody>

<tr>

<td style="border: 1px solid #00000a; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0.08in" bgcolor="#f2f2f2" valign="TOP" width="607">

public static void main(String[] args) {

Alarming.setProvider(new RemoteJdbcProvider());

DSLinkFactory.start(args, new Main());

}

</td>

</tr>

</tbody>

</table>

In the dslink-java-alarm module there are some providers that can be used for reference:

1.  org.dsa.iot.alarm.inMemory

2.  org.dsa.iot.alarm.jdbc

<div type="FOOTER">

<a name="_GoBack"></a>Version 0.0.1 July 19, 2016

</div>
