<div class="WordSection1">

Alarm Link

dslink-java-alarm

<div style="border:solid #5B9BD5 3.0pt;padding:0in 0in 0in 0in;background:#5B9BD5">

Contents

</div>

<span class="MsoHyperlink">[Overview<span style="color:windowtext;display:none;text-decoration:none">..</span> <span style="color:windowtext;display:none;text-decoration:none">3</span>](#_Toc456677160)</span>

<span class="MsoHyperlink">[Link Structure<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">3</span>](#_Toc456677161)</span>

<span class="MsoHyperlink">[Link Usage<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">3</span>](#_Toc456677162)</span>

<span class="MsoHyperlink">[Creating Alarms<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">3</span>](#_Toc456677163)</span>

<span class="MsoHyperlink">[Receiving Alarms<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">3</span>](#_Toc456677164)</span>

<span class="MsoHyperlink">[Managing Alarms<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">3</span>](#_Toc456677165)</span>

<span class="MsoHyperlink">[Alarm States<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">4</span>](#_Toc456677166)</span>

<span class="MsoHyperlink">[Alert <span style="color:windowtext;display:none;text-decoration:none"></span> <span style="color:windowtext;display:none;text-decoration:none">4</span>](#_Toc456677167)</span>

<span class="MsoHyperlink">[Fault <span style="color:windowtext;display:none;text-decoration:none"></span> <span style="color:windowtext;display:none;text-decoration:none">4</span>](#_Toc456677168)</span>

<span class="MsoHyperlink">[Normal <span style="color:windowtext;display:none;text-decoration:none"></span> <span style="color:windowtext;display:none;text-decoration:none">4</span>](#_Toc456677169)</span>

<span class="MsoHyperlink">[Offnormal <span style="color:windowtext;display:none;text-decoration:none"></span> <span style="color:windowtext;display:none;text-decoration:none">4</span>](#_Toc456677170)</span>

<span class="MsoHyperlink">[Component Guide<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">5</span>](#_Toc456677171)</span>

<span class="MsoHyperlink">[Alarm Service<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">5</span>](#_Toc456677172)</span>

<span class="MsoHyperlink">[Properties<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">5</span>](#_Toc456677173)</span>

<span class="MsoHyperlink">[Actions<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">5</span>](#_Toc456677174)</span>

<span class="MsoHyperlink">[Alarm Class<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">7</span>](#_Toc456677175)</span>

<span class="MsoHyperlink">[Properties<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">7</span>](#_Toc456677176)</span>

<span class="MsoHyperlink">[Actions<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">7</span>](#_Toc456677177)</span>

<span class="MsoHyperlink">[Alarm Algorithms<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">9</span>](#_Toc456677178)</span>

<span class="MsoHyperlink">[Properties<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">9</span>](#_Toc456677179)</span>

<span class="MsoHyperlink">[Actions<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">9</span>](#_Toc456677180)</span>

<span class="MsoHyperlink">[Alarm Watch<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">10</span>](#_Toc456677181)</span>

<span class="MsoHyperlink">[Properties<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">10</span>](#_Toc456677182)</span>

<span class="MsoHyperlink">[Actions<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">10</span>](#_Toc456677183)</span>

<span class="MsoHyperlink">[Alarm Record<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">10</span>](#_Toc456677184)</span>

<span class="MsoHyperlink">[PROPERTIES<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">10</span>](#_Toc456677185)</span>

<span class="MsoHyperlink">[Boolean Algorithm<span style="color:windowtext;display:none;text-decoration:none">..</span> <span style="color:windowtext;display:none;text-decoration:none">11</span>](#_Toc456677186)</span>

<span class="MsoHyperlink">[Properties<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">11</span>](#_Toc456677187)</span>

<span class="MsoHyperlink">[Actions<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">11</span>](#_Toc456677188)</span>

<span class="MsoHyperlink">[<span style="text-transform:uppercase;letter-spacing:.75pt">Out Of Range Algorithm</span><span style="color:windowtext;display:none;text-decoration:none">...</span> <span style="color:windowtext;display:none;text-decoration:none">12</span>](#_Toc456677189)</span>

<span class="MsoHyperlink">[<span style="text-transform:uppercase;letter-spacing:.75pt">Properties</span><span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">12</span>](#_Toc456677190)</span>

<span class="MsoHyperlink">[<span style="text-transform:uppercase;letter-spacing:.75pt">Actions</span><span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">12</span>](#_Toc456677191)</span>

<span class="MsoHyperlink">[<span style="text-transform:uppercase;letter-spacing:.75pt">Remote JDBC Service</span><span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">13</span>](#_Toc456677192)</span>

<span class="MsoHyperlink">[<span style="text-transform:uppercase;letter-spacing:.75pt">Properties</span><span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">13</span>](#_Toc456677193)</span>

<span class="MsoHyperlink">[Stale Algorithm<span style="color:windowtext;display:none;text-decoration:none">..</span> <span style="color:windowtext;display:none;text-decoration:none">13</span>](#_Toc456677194)</span>

<span class="MsoHyperlink">[Properties<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">13</span>](#_Toc456677195)</span>

<span class="MsoHyperlink">[Actions<span style="color:windowtext;display:none;text-decoration:none">.</span> <span style="color:windowtext;display:none;text-decoration:none">13</span>](#_Toc456677196)</span>

<span class="MsoHyperlink">[Creating a Custom Alarm Link<span style="color:windowtext;display:none;text-decoration:
none">.</span> <span style="color:windowtext;display:none;text-decoration:none">14</span>](#_Toc456677197)</span>

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif">
</span>

<span style="font-size:11.0pt;line-height:115%;color:white;
text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #5B9BD5 3.0pt;padding:0in 0in 0in 0in;background:#5B9BD5">

# <a name="_Toc456677160">Overview</a>

</div>

The alarm link records details about interesting events so they may be considered by humans.  The underlying model is influenced by the BACnet alarming design.

This link was designed as a framework upon which alarm links for different data stores can be developed.  There are also two implementations:  org.dsa.iot.alarm.inMemory.Main and org.dsa.iot.alarm.jdbc.Main.

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677161">Link Structure</a>

</div>

The nodes of this link follow this hierarchy.  Descriptions of each can be found in the Component Guide.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Service – The single root node of the link.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Alarm Class – There can be many alarm classes in a link, each representing some sort of grouping criteria such as location, system or users.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Alarm Algorithm – There can be many algorithms per alarm class.  Each algorithm has its own logic for determining when an alarm condition exists.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Watch – Path and meta-data about an entity being watched by the parent algorithm.

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677162">Link Usage</a>

</div>

The purpose of this link is to create and manage alarms.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677163">Creating Alarms</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Action invocation – Alarm sources can invoke an action on an alarm class to create an alarm.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm algorithms – Algorithms, such as “out of range” can subscribe to data sources and monitor their condition.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677164">Receiving Alarms</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Viewing – The alarm class has actions for retrieving alarm records.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Notifications – The alarm class has actions for receiving streams of new alarms, state changes as well as escalations.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677165">Managing Alarms</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Acknowledgement – Fault and offnormal alarms require acknowledgement.  Acknowledgement can be achieved with an action on the Alarm Service.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Return to normal – All alarms must return to normal before they can be closed, this can be achieved with an action on the Alarm Service or an alarm algorithm automatically detecting it.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Closing – An alarm is considered closed when normal and acknowledged (unless it is an alert which does not require acknowledgement).

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677166">Alarm States</a>

</div>

An alarm source is an entity that can be in an alarm condition.  There are four possible states a source can be in.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677167">Alert</a>

</div>

An alert is informational, it does not require acknowledgement.  Once an alarm source in alert returns to normal, an operator would not see the alert on their console unless explicitly queried.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677168">Fault</a>

</div>

Faults represent a malfunction or failure within the system.  To close a fault, it must return to the normal state and be acknowledged.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677169">Normal</a>

</div>

Normal is healthy, and none of the other states.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677170">Offnormal</a>

</div>

Offnormal represents an unexpected condition, or something outside the bounds of normal operation.  To close an offnormal alarm, it must return to the normal state and be acknowledged.

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif">
</span>

<span style="font-size:11.0pt;line-height:115%;color:white;
text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #5B9BD5 3.0pt;padding:0in 0in 0in 0in;background:#5B9BD5">

# <a name="_Toc456677171">Component Guide</a>

</div>

This section documents the major components of the link.

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677172">Alarm Service</a>

</div>

This is the visible root node of the link.  Its purpose is to create alarm classes and manage alarm records independent of alarm class.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677173">Properties</a>

</div>

<span style="font-family:
Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Enabled - When false, no new alarms will be created.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677174">Actions</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Acknowledge – This updates an open alarm record.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>UUID – Required unique alarm ID.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>User – Entity performing the update.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Alarm Class – Add a new Alarm Class.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Name – The alarm class name.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Note – Add a note to an existing alarm.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>UUID – Alarm record.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>User – User name.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Note – Test message.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete All Records – Deletes all records from the database.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Record – Deletes all records for the given UUIDs.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>UUID – Specific alarm id

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Get Alarm – Return a single row table representing the alarm record for the give UUID.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>UUID – Specific alarm id

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Return – a single record.  See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Get Notes – This returns a table of notes for a specific alarm.  The columns are:

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>UUID – Specific alarm id

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Return – a table with the following columns:

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Timestamp – The time of the alarm.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>User – The entity providing the note.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Note – The text of the note.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Return To Normal – This returns an alarm record to its normal state.  It has no effect on records that are already normal.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>UUID – The record to return to the normal state.

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif">
</span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677175">Alarm Class</a>

</div>

An alarm class represents a group of alarms that are related in some way.  Alarms can only be created with an alarm class but other alarm lifecycle operations are handled on the service.

The alarm class offers many streams (as actions) for monitoring various states of alarms including escalation.  Escalation happens when alarm goes unacknowledged for a certain period of time and can be used to notify backup or higher seniority staff.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677176">Properties</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Enabled – When false, no new alarms will be created.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Escalation 1 Days – The number of days to add to the escalation duration.  If the total duration is zero or less, the escalation level will be disabled.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Escalation 1 Hours – The number of hours to add to the escalation duration.  If the total duration is zero or less, escalation level will be disabled.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Escalation 1 Minutes – The number of minutes to add to the escalation duration.  If the total duration is zero or less, escalation level will be disabled.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Escalation 2 Days – The number of days to add to the escalation duration.  If the total duration is zero or less, escalation level will be disabled.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Escalation 2 Hours – The number of hours to add to the escalation duration.  If the total duration is zero or less, escalation level will be disabled.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Escalation 2 Minutes – The number of minutes to add to the escalation duration.  If the total duration is zero or less, escalation level will be disabled.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677177">Actions</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Algorithm – This adds an algorithm for generating alarms.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Name – The alarm class name.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Type – The specific alarm class desired.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Create Alarm – Creates a new alarm record.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Source Path – Path to the alarm source.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Create State – Alert, Fault or Offnormal

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Message – Short text description.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Returns – a one row table representing the alarm record.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Alarm Class – Removes the alarm class and its child nodes.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Get Alarms – Returns a table of alarm for this alarm class.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Time Range – A DSA time range.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Returns – a table of alarm records.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Get Open Alarms – This returns a table of open alarm alarms for this alarm class.  The table stream can remain open and any updates as well as new records will be passed along.  The primary intent of this is for an alarm console.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Returns – a stream of table rows representing the alarm records.  The stream state will switch to open once the initial set of open alarms is sent.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Stream Escalation 1 – Returns a stream of alarm records as they escalate in real time.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Returns – table rows representing the alarm records.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Stream Escalation 2 – Returns a stream of alarm records as they escalate in real time.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Returns – table rows representing the alarm records.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Stream New Alarms– This returns a stream of new alarm records for this alarm class.  The table stream will remain open and any updates and new records will be passed along.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Returns – table rows representing the alarm records.

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>See the alarm record section of this document.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Rename Alarm Class – Changes the name of the alarm class, all records will reflect the change.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Parameters

<span style="font-family:Wingdings">§<span style="font:7.0pt &quot;Times New Roman&quot;"> </span> </span>Name – The new name.

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif">
</span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677178">Alarm Algorithms</a>

</div>

Alarm algorithms evaluates the state of Alarm Watch objects, and generate alarms for each when the conditions of the algorithm are met.  This describes the common functionality of all alarm algorithms, individual algorithms will be described in a separately.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677179">Properties</a>

</div>

The properties of an algorithm will be specific to its type.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Enabled – When false, no new records will be created.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Type – What type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval.  An auto interval should be used if using inhibits.  Watches will always update themselves they detect a change of value on the source.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677180">Actions</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Watch – Takes path for subscription in the parent broker.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Auto Update Interval – Sets auto update interval property in seconds.  Use zero to disable.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Algorithm – Remove the algorithm from the parent alarm class.

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif">
</span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677181">Alarm Watch</a>

</div>

Represents an alarm source that an algorithm will monitor for alarm conditions. There is a primary alarm source, but other paths may be used by subclasses for determining more complex conditions.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677182">Properties</a>

</div>

The properties of an algorithm will be specific to its type.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Enabled – When false, no new records will be created.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Source Path – The path to the primary alarmable entity.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm State – The current state of the source.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm State Time – The best known time that the source transitioned to the alarm state.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Last Alarm Record www– The UUID of the last related alarm record.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Last Cov – The timestamp of the last change of value of the source.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677183">Actions</a>

</div>

<span style="font-family:
Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Watch – Remove the watch from the parent algorithm.

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677184">Alarm Record</a>

</div>

An alarm record represents details about an alarm.   This is an abstract description of the Java class as well table columns in the DSA protocol.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677185">PROPERTIES</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>UUID – Unique ID, generated by the link.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Source – Path to the alarm source.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Class – The name of the alarm class the record was created in.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Created Time – Timestamp of creation.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Created State – The state of the source at creation.  Possible values are:

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Alert – Informational, acknowledge not required.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Fault – A malfunction representing a failure within the system.

<span style="font-family:&quot;Courier New&quot;">o<span style="font:7.0pt &quot;Times New Roman&quot;">   </span> </span>Offnormal – An unexpected condition, or outside the bounds of normal operation.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Normal Time – If not null, the timestamp that the source returned to normal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Ack Time – If not null, the timestamp of acknowledgement.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Ack User – The entity that acknowledged the alarm.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Message – Text describing the alarm at the time of creation.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Has Notes – Whether or not the alarm has any notes associated with it.

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif;
text-transform:uppercase;letter-spacing:.75pt">
</span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677186">Boolean Algorithm</a>

</div>

This algorithm creates alarms when boolean sources turn true or false.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677187">Properties</a>

</div>

The properties of an algorithm will be specific to its type.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Enabled – When false, no new records will be created.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Type – What type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval.  Will always update when the watch detects a change of value on the source.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Value – What to alarm on, true or false.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677188">Actions</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Watch – Takes path for subscription in the parent broker.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Auto Update Interval – Sets auto update interval property in seconds.  Use zero to disable.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Update All – Re-evaluate all child watches.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Algorithm – Remove the algorithm from the parent alarm class.

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif;
text-transform:uppercase;letter-spacing:.75pt">
</span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

<a name="_Toc456677189"><span style="text-transform:uppercase;letter-spacing:.75pt">Out Of Range Algorithm</span></a>

</div>

This algorithm creates alarms for sources whose numeric value is less than a minimum value, or greater than a maximum value. 

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

<a name="_Toc456677190"><span style="color:#1F4D78;text-transform:uppercase;
letter-spacing:.75pt">Properties</span></a>

</div>

The properties of an algorithm will be specific to its type.

*   Enabled – When false, no new records will be created.
*   Alarm Type – Enum indicating whether records should be alert, fault or offnormal.
*   Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval.  Will always update when the watch detects a change of value on the source.
*   To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.
*   To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.
*   Min Value – Value to use if Use Node Range is false, or the target node does not define a min value.
*   Max Value – Value to use if Use Node Range is false, or the target node does not define a max value.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

<a name="_Toc456677191"><span style="color:#1F4D78;text-transform:uppercase;
letter-spacing:.75pt">Actions</span></a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Watch – Takes path for subscription in the parent broker.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Auto Update Interval – Sets auto update interval property in seconds.  Use zero to disable.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Update All – Re-evaluate all child watches.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Algorithm – Remove the algorithm from the parent alarm class.

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<span style="font-size:10.0pt;line-height:115%;font-family:&quot;Calibri&quot;,sans-serif;
text-transform:uppercase;letter-spacing:.75pt">
</span>

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

<a name="_Toc456677192"><span style="text-transform:uppercase;letter-spacing:.75pt">Remote JDBC Service</span></a>

</div>

This is an alarm service with a connection to a remote database.  All properties and actions are inherited from the base Alarm Service. 

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

<a name="_Toc456677193"><span style="color:#1F4D78;text-transform:uppercase;
letter-spacing:.75pt">Properties</span></a>

</div>

The following are unique to this type.

*   JDBC Driver – Class name of the driver.
*   Database URL – Enum indicating whether records should be alert, fault or offnormal.
*   Database Name – The database will be created if it does not already exist.
*   Database User – Credentials to access the database base.  If blank, will only attempt to acquire a connection using the URL.
*   Database Pass – Password for the database user.

<span style="text-transform:uppercase;letter-spacing:.75pt"> </span>

<div style="border:solid #DEEAF6 3.0pt;padding:0in 0in 0in 0in;background:#DEEAF6">

## <a name="_Toc456677194">Stale Algorithm</a>

</div>

This algorithm creates alarms for sources whose value does not change after a certain period of time. This can be useful for detecting sensor failure.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677195">Properties</a>

</div>

The properties of an algorithm will be specific to its type.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Enabled – When false, no new records will be created.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Alarm Type – What type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Auto Update Interval – If greater than zero, will automatically re-evaluate the alarm state of each watch on this interval.  Will always update when the watch detects a change of value on the source.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>To Alarm Inhibit – How long (in seconds) to delay going into alarm after the alarm condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>To Normal Inhibit – How long (in seconds) to delay a return to normal after the normal condition is first detected.  This can help minimize alarm creation.  Use zero to disable, otherwise you should have a positive auto update interval.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Stale Days – The number of days to add to the stale duration.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Stale Hours – The number of hours to add to the stale duration.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Stale Minutes – The number of minutes to add to the stale duration.

<div style="border:none;border-top:solid #5B9BD5 1.0pt;padding:2.0pt 0in 0in 0in">

### <a name="_Toc456677196">Actions</a>

</div>

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Add Watch – Takes path for subscription in the parent broker.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Alarm Type – Determines what type of alarm this algorithm creates: alert, fault or offnormal.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Set Auto Update Interval – Sets auto update interval property in seconds.  Use zero to disable.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Update All – Re-evaluate all child watches.

<span style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New Roman&quot;">        </span> </span>Delete Algorithm – Remove the algorithm from the parent alarm class.

<div style="border:solid #5B9BD5 3.0pt;padding:0in 0in 0in 0in;background:#5B9BD5">

# <a name="_Toc456677197">Creating a Custom Alarm Link</a>

</div>

Creating a custom link primarily requires implementing a single interface.  After that, plenty of hooks exist if customizations of other built in types are required.

1.<span style="font:7.0pt &quot;Times New Roman&quot;">      </span> Create an implementation of org.iot.dsa.alarm.Alarming.Provider.

2.<span style="font:7.0pt &quot;Times New Roman&quot;">      </span> Create a “main” class that subclasses org.iot.dsa.alarm.AlarmLinkHandler.

3.<span style="font:7.0pt &quot;Times New Roman&quot;">      </span> In the main method of the Main class:

a.<span style="font:7.0pt &quot;Times New Roman&quot;">       </span> Call Alarming.setProvider with an instance of your provider.

b.<span style="font:7.0pt &quot;Times New Roman&quot;">      </span> Call DSLinkFactory.start with an instance of your main class.

For example:

| 

    public static void main(String[] args) {

        Alarming.setProvider(new RemoteJdbcProvider());

        DSLinkFactory.start(args, new Main());

    }

 |

In the dslink-java-alarm module there are some providers that can be used for reference:

1.<span style="font:7.0pt &quot;Times New Roman&quot;">      </span> org.dsa.iot.alarm.inMemory

2.<span style="font:7.0pt &quot;Times New Roman&quot;">      </span> org.dsa.iot.alarm.jdbc

</div>
