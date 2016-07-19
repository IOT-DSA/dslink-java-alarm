/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.methods.*;
import org.dsa.iot.dslink.node.*;
import org.dsa.iot.dslink.node.actions.*;
import org.dsa.iot.dslink.node.actions.table.*;
import org.dsa.iot.dslink.node.value.*;
import org.dsa.iot.dslink.util.*;
import org.slf4j.*;
import java.util.*;

/**
 * An alarm class represents a group of alarms that are related in some way.
 * Alarms can only be created with an alarm class but other alarm lifecycle
 * operations are handled on the service.
 * <p>
 * The alarm class offers many streams (as actions) for monitoring various states of
 * alarms including escalation.  Escalation happens when alarm goes unacknowledged for
 * a certain period of time and can be used to notify backup or higher seniority staff.
 *
 * @author Aaron Hansen
 */
public class AlarmClass extends AbstractAlarmObject implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String ESCALATION1_DYS = "Escalation 1 Days";
    private static final String ESCALATION1_HRS = "Escalation 1 Hours";
    private static final String ESCALATION1_MNS = "Escalation 1 Minutes";

    private static final String ESCALATION2_DYS = "Escalation 1 Days";
    private static final String ESCALATION2_HRS = "Escalation 1 Hours";
    private static final String ESCALATION2_MNS = "Escalation 1 Minutes";

    private static final String TIME_RANGE = "Time Range";

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmClass.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private HashSet<AlarmStreamer> allUpdatesListeners = new HashSet<>();
    private ArrayList<AlarmStreamer> allUpdatesListenerCache = new ArrayList<>();
    private HashSet<AlarmStreamer> escalation1Listeners = new HashSet<>();
    private ArrayList<AlarmStreamer> escalation1ListenerCache = new ArrayList<>();
    private HashSet<AlarmStreamer> escalation2Listeners = new HashSet<>();
    private ArrayList<AlarmStreamer> escalation2ListenerCache = new ArrayList<>();
    private long lastEscalationCheck = System.currentTimeMillis();
    private HashSet<AlarmStreamer> newAlarmListeners = new HashSet<>();
    private ArrayList<AlarmStreamer> newAlarmListenerCache = new ArrayList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public AlarmClass() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Action handler for adding child nodes representing algorithms.
     */
    private void addAlgorithm(final ActionResult event) {
        try {
            Value name = event.getParameter(NAME);
            if (name == null) {
                throw new NullPointerException("Missing name");
            }
            String nameString = name.getString();
            Node parent = event.getNode().getParent();
            Node alarmClassNode = parent.getChild(nameString);
            if (alarmClassNode != null) {
                throw new IllegalArgumentException(
                        "Name already in use: " + name.getString());
            }
            Value type = event.getParameter(TYPE);
            if (type == null) {
                throw new NullPointerException("Missing type");
            }
            String typeString = type.getString();
            Class typeClass = Alarming.getProvider().getAlarmAlgorithms().get(typeString);
            newChild(nameString, typeClass);
            LOGGER.info("Added alarm class: " + name.getString());
        } catch (Exception x) {
            LOGGER.error("addAlarmClass", x);
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * Iterates the open alarms looking for escalations.
     */
    private void checkEscalations() {
        long now = System.currentTimeMillis();
        try {
            //Bail if no listeners.
            if ((escalation1Listeners.size() == 0) && (escalation2Listeners.size()
                    == 0)) {
                return;
            }
            int e1dys = getProperty(ESCALATION1_DYS).getNumber().intValue();
            int e1hrs = getProperty(ESCALATION1_HRS).getNumber().intValue();
            int e1mns = getProperty(ESCALATION1_MNS).getNumber().intValue();
            int e2dys = getProperty(ESCALATION1_DYS).getNumber().intValue();
            int e2hrs = getProperty(ESCALATION1_HRS).getNumber().intValue();
            int e2mns = getProperty(ESCALATION1_MNS).getNumber().intValue();
            //Bail if no escalation configured.
            if ((e1dys <= 0) && (e1hrs <= 0) && (e1mns <= 0) &&
                    (e2dys <= 0) && (e2hrs <= 0) && (e2mns <= 0)) {
                return;
            }
            Calendar calendar = Calendar.getInstance();
            AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
            while (cursor.next()) {
                if (!cursor.isAcknowledged()) {
                    calendar.setTimeInMillis(cursor.getCreatedTime());
                    if (shouldEscalate(calendar, e1dys, e1hrs, e1mns, now)) {
                        notifyEscalation1(cursor.newCopy());
                    } else if (shouldEscalate(calendar, e2dys, e2hrs, e2mns, now)) {
                        notifyEscalation2(cursor.newCopy());
                    }
                }
            }
        } catch (Exception x) {
            LOGGER.error("execute", x);
        } finally {
            lastEscalationCheck = now;
        }
    }

    /**
     * Action handler for adding child nodes representing algorithms.
     */
    private void createAlarm(final ActionResult event) {
        if (!isEnabled() || !getService().isEnabled()) {
            return;
        }
        Value sourcePath = event.getParameter(SOURCE_PATH);
        Value createState = event.getParameter(CREATE_STATE);
        Value message = event.getParameter(MESSAGE, new Value(""));
        AlarmState alarmState = AlarmState.decode(createState.getString());
        AlarmRecord alarmRecord = ((AlarmService) getParent())
                .createAlarm(this, sourcePath.getString(), alarmState,
                             message.toString());
        event.setStreamState(StreamState.INITIALIZED);
        Table table = event.getTable();
        table.setMode(Table.Mode.APPEND);
        AlarmUtil.encodeAlarm(alarmRecord, table, null, null);
        event.setStreamState(StreamState.CLOSED);
        notifyAllUpdates(alarmRecord);
        notifyNewRecord(alarmRecord);
    }

    /**
     * Calls execute on all child algorithms, then checks for escalations.
     */
    protected void execute() {
        if (!isEnabled() || !getService().isEnabled()) {
            return;
        }
        AlarmObject kid;
        for (int i = 0, len = childCount(); i < len; i++) {
            kid = getChild(i);
            if (kid instanceof AlarmAlgorithm) {
                ((AlarmAlgorithm) kid).execute();
            }
        }
        checkEscalations();
    }

    /**
     * Action handler for getting alarms for a time range.
     */
    private void getAlarms(final ActionResult event) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        Value timeRange = event.getParameter(TIME_RANGE);
        if (timeRange != null) {
            //just fail fast if invalid time range
            String[] parts = timeRange.getString().split("/");
            TimeUtils.decode(parts[0], from);
            TimeUtils.decode(parts[1], to);
        } else {
            //Default to today.
            TimeUtils.alignDay(from);
            TimeUtils.addDays(1, to);
            TimeUtils.alignDay(to);
        }
        final AlarmCursor cursor = Alarming.getProvider().queryAlarms(this, from, to);
        event.setStreamState(StreamState.INITIALIZED);
        final Table table = event.getTable();
        table.setMode(Table.Mode.APPEND);
        table.sendReady();
        AlarmActionHandler runnable = new AlarmActionHandler() {
            public void run() {
                table.waitForStream(10000, true);
                int count = 0;
                while (isOpen() && cursor.next()) {
                    AlarmUtil.encodeAlarm(cursor, table, null, null);
                    if ((++count % ALARM_RECORD_CHUNK) == 0) {
                        table.sendReady();
                    }
                }
                if (isOpen()) {
                    event.setStreamState(StreamState.CLOSED);
                }
            }
        };
        event.setCloseHandler(runnable);
        AlarmUtil.enqueue(runnable);
    }

    /**
     * Action handler for getting all open alarms followed by a stream of allUpdatesListeners.
     */
    private void getOpenAlarms(final ActionResult event) {
        final AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
        event.setStreamState(StreamState.INITIALIZED);
        final Table table = event.getTable();
        table.setMode(Table.Mode.STREAM);
        table.sendReady();
        AlarmStreamer streamer = new AlarmStreamer(allUpdatesListeners, event, cursor);
        AlarmUtil.run(streamer,"Open Alarms");
    }

    /**
     * A convenience that casts the parent.
     */
    protected AlarmService getService() {
        return (AlarmService) getParent();
    }

    @Override protected void initActions() {
        //Add Algorithm
        Node node = getNode();
        Action action = new Action(Permission.WRITE, this::addAlgorithm);
        action.addParameter(
                new Parameter(NAME, ValueType.STRING, new Value("Unique Name")));
        Set<String> algos = Alarming.getProvider().getAlarmAlgorithms().keySet();
        action.addParameter(new Parameter(TYPE, ValueType.makeEnum(algos),
                new Value(algos.iterator().next())));
        node.createChild("Add Algorithm").setSerializable(false).setAction(action)
                .build();
        //Create Alarm action
        action = new Action(Permission.WRITE, this::createAlarm);
        action.setResultType(ResultType.TABLE);
        action.addParameter(new Parameter(SOURCE_PATH, ValueType.STRING,
                new Value("/path/in/broker")));
        action.addParameter(
                new Parameter(CREATE_STATE, ENUM_ALARM_TYPE, new Value(ALERT)));
        action.addParameter(
                new Parameter(MESSAGE, ValueType.STRING, new Value("Max 80 Chars")));
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild(CREATE_ALARM).setSerializable(false).setAction(action).build();
        //Get Alarms
        action = new Action(Permission.READ, this::getAlarms);
        action.addParameter(
                new Parameter(TIME_RANGE, ValueType.STRING, new Value("today"))
                        .setEditorType(EditorType.DATE_RANGE));
        action.setResultType(ResultType.TABLE);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Get Alarms").setSerializable(false).setAction(action).build();
        //Get Open Alarms
        action = new Action(Permission.READ, this::getOpenAlarms);
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Get Open Alarms").setSerializable(false).setAction(action)
                .build();
        //Stream Escalation 1
        action = new Action(Permission.READ, this::streamEscalation1);
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Stream Escalation 1").setSerializable(false).setAction(action)
                .build();
        //Stream Escalation 2
        action = new Action(Permission.READ, this::streamEscalation2);
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Stream Escalation 2").setSerializable(false).setAction(action)
                .build();
        //Stream New Alarms
        action = new Action(Permission.READ, this::streamNewAlarms);
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Stream New Alarms").setSerializable(false).setAction(action)
                .build();
        addDeleteAction("Delete Alarm Class");
    }

    @Override protected void initProperties() {
        Node node = getNode();
        if (node.getConfig(ENABLED) == null) {
            node.setConfig(ENABLED, new Value(false));
        }
        if (node.getConfig(ESCALATION1_DYS) == null) {
            node.setConfig(ESCALATION1_DYS, new Value(0));
        }
        if (node.getConfig(ESCALATION1_HRS) == null) {
            node.setConfig(ESCALATION1_HRS, new Value(0));
        }
        if (node.getConfig(ESCALATION1_MNS) == null) {
            node.setConfig(ESCALATION1_MNS, new Value(0));
        }
        if (node.getConfig(ESCALATION2_DYS) == null) {
            node.setConfig(ESCALATION2_DYS, new Value(0));
        }
        if (node.getConfig(ESCALATION2_HRS) == null) {
            node.setConfig(ESCALATION2_HRS, new Value(0));
        }
        if (node.getConfig(ESCALATION2_MNS) == null) {
            node.setConfig(ESCALATION2_MNS, new Value(0));
        }
    }

    /**
     * Adds the record to all the streams in the corresponding collection.
     */
    void notifyAllUpdates(AlarmRecord record) {
        ArrayList<AlarmStreamer> list = allUpdatesListenerCache;
        synchronized (allUpdatesListeners) {
            if (allUpdatesListeners.size() != list.size()) {
                allUpdatesListenerCache = new ArrayList<>();
                list = allUpdatesListenerCache;
                list.addAll(allUpdatesListeners);
            }
        }
        for (int i = list.size(); --i >= 0; ) {
            list.get(i).update(record);
        }
    }

    /**
     * Adds the record to all the streams in the corresponding collection.
     */
    private void notifyEscalation1(AlarmRecord record) {
        ArrayList<AlarmStreamer> list = escalation1ListenerCache;
        synchronized (escalation1Listeners) {
            if (escalation1Listeners.size() != list.size()) {
                escalation1ListenerCache = new ArrayList<>();
                list = escalation1ListenerCache;
                list.addAll(escalation1Listeners);
            }
        }
        for (int i = list.size(); --i >= 0; ) {
            list.get(i).update(record);
        }
    }

    /**
     * Adds the record to all the streams in the corresponding collection.
     */
    private void notifyEscalation2(AlarmRecord record) {
        ArrayList<AlarmStreamer> list = escalation2ListenerCache;
        synchronized (escalation2Listeners) {
            if (escalation2Listeners.size() != list.size()) {
                escalation2ListenerCache = new ArrayList<>();
                list = escalation2ListenerCache;
                list.addAll(escalation2Listeners);
            }
        }
        for (int i = list.size(); --i >= 0; ) {
            list.get(i).update(record);
        }
    }

    /**
     * Adds the record to all the streams in the corresponding collection.
     */
    void notifyNewRecord(AlarmRecord record) {
        ArrayList<AlarmStreamer> list = newAlarmListenerCache;
        synchronized (newAlarmListeners) {
            if (newAlarmListeners.size() != list.size()) {
                newAlarmListenerCache = new ArrayList<>();
                list = newAlarmListenerCache;
                list.addAll(newAlarmListeners);
            }
        }
        for (int i = list.size(); --i >= 0; ) {
            list.get(i).update(record);
        }
    }

    /**
     * Whether or not to escalate.  If days, hours, and mins are all zero or
     * less this will return false.
     *
     * @param from      This should be the create timestamp of the alarm record.
     * @param days      Only values greater than zero are considered.
     * @param hours     Only values greater than zero are considered.
     * @param mins      Only values greater than zero are considered.
     * @param checkTime The 'now' time, will be the lastEscalation time in the next cycle.
     * @return Whether or not an escalation is needed.
     */
    private boolean shouldEscalate(Calendar from, int days, int hours, int mins,
            long checkTime) {
        if ((days <= 0) && (hours <= 0) && (mins <= 0)) {
            return false;
        }
        TimeUtils.addDays(days, from);
        TimeUtils.addHours(hours, from);
        TimeUtils.addMinutes(mins, from);
        long escalate = from.getTimeInMillis();
        if ((lastEscalationCheck < escalate) && (escalate <= checkTime)) {
            return true;
        }
        return false;
    }

    /**
     * Action handler for streaming esclation 1 alarms
     */
    private void streamEscalation1(final ActionResult event) {
        final AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
        event.setStreamState(StreamState.INITIALIZED);
        final Table table = event.getTable();
        table.setMode(Table.Mode.STREAM);
        table.sendReady();
        AlarmStreamer streamer = new AlarmStreamer(escalation1Listeners, event, cursor);
        AlarmUtil.run(streamer,"Escalation 1");
    }

    /**
     * Action handler for streaming escalation 2 alarms
     */
    private void streamEscalation2(final ActionResult event) {
        final AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
        event.setStreamState(StreamState.INITIALIZED);
        final Table table = event.getTable();
        table.setMode(Table.Mode.STREAM);
        table.sendReady();
        AlarmStreamer streamer = new AlarmStreamer(escalation2Listeners, event, cursor);
        AlarmUtil.run(streamer,"Escalation 2");
    }

    /**
     * Action handler for streaming new alarms
     */
    private void streamNewAlarms(final ActionResult event) {
        final AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
        event.setStreamState(StreamState.INITIALIZED);
        final Table table = event.getTable();
        table.setMode(Table.Mode.STREAM);
        table.sendReady();
        AlarmStreamer streamer = new AlarmStreamer(newAlarmListeners, event, cursor);
        AlarmUtil.run(streamer,"New Alarms");
    }


    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

} //class


