/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.*;
import org.dsa.iot.alarm.AlarmService.Counts;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.actions.*;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.TimeUtils;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * An alarm class represents a group of alarms that are related in some way. Alarms can only be
 * created with an alarm class but some alarm lifecycle operations are handled on the service.
 * <p>
 * <p>
 * <p>
 * The alarm class offers many streams (as actions) for monitoring various states of alarms
 * including escalation.  Escalation happens when alarm goes unacknowledged for a certain period of
 * time and can be used to notify backup or higher seniority staff.
 *
 * @author Aaron Hansen
 */
public class AlarmClass extends AbstractAlarmObject {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String ESCALATION1_DYS = "Escalation 1 Days";
    private static final String ESCALATION1_HRS = "Escalation 1 Hours";
    private static final String ESCALATION1_MNS = "Escalation 1 Minutes";
    private static final String ESCALATION2_DYS = "Escalation 2 Days";
    private static final String ESCALATION2_HRS = "Escalation 2 Hours";
    private static final String ESCALATION2_MNS = "Escalation 2 Minutes";
    private static final String PURGE_CLOSED_DAYS = "Purge Closed Days";
    private static final String PURGE_OPEN_DAYS = "Purge Open Days";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private HashSet<AlarmStreamer> allUpdatesListeners = new HashSet<>();
    private ArrayList<AlarmStreamer> allUpdatesListenerCache = null;
    private HashSet<AlarmStreamer> escalation1Listeners = new HashSet<>();
    private ArrayList<AlarmStreamer> escalation1ListenerCache = null;
    private HashSet<AlarmStreamer> escalation2Listeners = new HashSet<>();
    private ArrayList<AlarmStreamer> escalation2ListenerCache = null;
    private long lastAutoPurge = -1;
    private long lastEscalationCheck = System.currentTimeMillis();
    private HashSet<AlarmStreamer> newAlarmListeners = new HashSet<>();
    private ArrayList<AlarmStreamer> newAlarmListenerCache = null;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Action handler for acknowledging all open alarms.
     */
    private void acknowledgeAllOpen(ActionResult event) {
        try {
            Value user = event.getParameter(USER);
            if (user == null) {
                throw new NullPointerException("Missing " + USER);
            }
            Alarming.Provider provider = Alarming.getProvider();
            AlarmCursor cur = Alarming.getProvider().queryOpenAlarms(this);
            while (cur.next()) {
                if (!cur.isAcknowledged()) {
                    provider.acknowledge(cur.getUuid(), user.getString());
                    AlarmRecord rec = Alarming.getProvider().getAlarm(cur.getUuid());
                    notifyAllUpdates(rec);
                }
            }
            getService().updateCounts(true);
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * Action handler for adding child algorithm nodes.
     */
    private void addAlgorithm(final ActionResult event) {
        try {
            Value name = event.getParameter(NAME);
            if (name == null) {
                throw new NullPointerException("Missing name");
            }
            String nameString = name.getString();
            Node parent = event.getNode().getParent();
            Node alarmClassNode = parent.getChild(nameString, true);
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
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * Adds the duration to the calendar and returns it.
     *
     * @param from  The calendar to add the duration, this is also returned.
     * @param days  Number of days to add (not simply 24 hours).
     * @param hours Number of hours to add to the given calendar.
     * @param mins  Number of minutes to add to the given calendar.
     * @return The provided calendar.
     */
    private Calendar applyEscalation(Calendar from, int days, int hours, int mins) {
        if ((days != 0) || (hours != 0) || (mins != 0)) {
            TimeUtils.addDays(days, from);
            TimeUtils.addHours(hours, from);
            TimeUtils.addMinutes(mins, from);
        }
        return from;
    }

    /**
     * Auto purge once an hour.
     */
    private void checkAutoPurge() {
        long now = System.currentTimeMillis();
        if (lastAutoPurge < 0) {
            lastAutoPurge = now;
            return;
        }
        if (now < (lastAutoPurge + TimeUtils.MILLIS_HOUR)) {
            return;
        }
        lastAutoPurge = now;
        //This wont't be deleting many records each pass.
        int days = getProperty(PURGE_CLOSED_DAYS).getNumber().intValue();
        boolean update = false;
        if (days > 0) {
            Calendar cal = TimeUtils.reuseCalendar(now);
            TimeUtils.addDays(-days, cal);
            AlarmCursor cur = Alarming.getProvider().queryAlarms(this, null, cal);
            while (cur.next()) {
                if (cur.isClosed()) {
                    AlarmUtil.logTrace("Auto purging: " + cur.getUuid().toString());
                    Alarming.getProvider().deleteRecord(cur.getUuid());
                    update = true;
                }
                Thread.yield();
            }
            TimeUtils.recycleCalendar(cal);
        }
        days = getProperty(PURGE_OPEN_DAYS).getNumber().intValue();
        if (days > 0) {
            Calendar cal = TimeUtils.reuseCalendar(now);
            TimeUtils.addDays(-days, cal);
            AlarmCursor cur = Alarming.getProvider().queryAlarms(this, null, cal);
            while (cur.next()) {
                if (cur.isOpen()) {
                    AlarmUtil.logTrace("Auto purging: " + cur.getUuid().toString());
                    Alarming.getProvider().deleteRecord(cur.getUuid());
                    update = true;
                }
                Thread.yield();
            }
            TimeUtils.recycleCalendar(cal);
        }
        if (update) {
            getService().updateCounts();
        }
    }

    /**
     * Iterates the open alarms looking for escalations.
     */
    private void checkEscalations() {
        long now = System.currentTimeMillis();
        try {
            //Bail if no listeners.
            if ((escalation1Listeners.isEmpty()) && (escalation2Listeners.isEmpty())) {
                return;
            }
            int e1dys = getProperty(ESCALATION1_DYS).getNumber().intValue();
            int e1hrs = getProperty(ESCALATION1_HRS).getNumber().intValue();
            int e1mns = getProperty(ESCALATION1_MNS).getNumber().intValue();
            boolean checkEs1 = (e1dys > 0) || (e1hrs > 0) || (e1mns > 0);
            int e2dys = getProperty(ESCALATION2_DYS).getNumber().intValue();
            int e2hrs = getProperty(ESCALATION2_HRS).getNumber().intValue();
            int e2mns = getProperty(ESCALATION2_MNS).getNumber().intValue();
            boolean checkEs2 = (e2dys > 0) || (e2hrs > 0) || (e2mns > 0);
            //Bail if no escalation configured.
            if (!checkEs1 && !checkEs2) {
                return;
            }
            Calendar calendar = Calendar.getInstance();
            AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
            while (cursor.next()) {
                if (cursor.isAckRequired() && !cursor.isAcknowledged()) {
                    calendar.setTimeInMillis(cursor.getCreatedTime());
                    if (checkEs1) {
                        applyEscalation(calendar, e1dys, e1hrs, e1mns);
                        if (shouldEscalate(calendar.getTimeInMillis(), now)) {
                            notifyEscalation1(cursor.newCopy());
                        }
                    }
                    if (checkEs2) {
                        //escalation 2 is relative to escalation 1.
                        applyEscalation(calendar, e2dys, e2hrs, e2mns);
                        if (shouldEscalate(calendar.getTimeInMillis(), now)) {
                            notifyEscalation2(cursor.newCopy());
                        }
                    }
                }
            }
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
        } finally {
            lastEscalationCheck = now;
        }
    }

    /**
     * Action handler for creating a new alarm record.
     */
    private void createAlarm(final ActionResult event) {
        if (!isEnabled() || !getService().isEnabled()) {
            return;
        }
        Value sourcePath = event.getParameter(SOURCE_PATH);
        Value createState = event.getParameter(CREATE_STATE);
        Value message = event.getParameter(MESSAGE, new Value(""));
        AlarmState alarmState = AlarmState.decode(createState.getString());
        AlarmRecord alarmRecord = ((AlarmService) getParent()).createAlarm(
                this, null, sourcePath.getString(), alarmState, message.toString());
        event.setStreamState(StreamState.INITIALIZED);
        Table table = event.getTable();
        table.setMode(Table.Mode.APPEND);
        AlarmUtil.encodeAlarm(alarmRecord, table, null, null);
        event.setStreamState(StreamState.CLOSED);
        notifyAllUpdates(alarmRecord);
        notifyNewRecord(alarmRecord);
        getService().updateCounts();
    }

    /**
     * Calls execute on all child algorithms, then checks for escalations.
     */
    protected void execute() {
        if (isEnabled() && getService().isEnabled()) {
            AlarmObject child;
            for (int i = 0, len = childCount(); i < len; i++) {
                child = getChild(i);
                if (child instanceof AlarmAlgorithm) {
                    ((AlarmAlgorithm) child).execute();
                }
            }
            checkEscalations();
        }
        checkAutoPurge();
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
            to.setTimeInMillis(to.getTimeInMillis() + 1); //dglux uses inclusive end
        } else {
            //Default to today.
            TimeUtils.alignDay(from);
            TimeUtils.addDays(1, to);
            TimeUtils.alignDay(to);
        }
        final AlarmCursor cursor = Alarming.getProvider().queryAlarms(this, from, to);
        AlarmStreamer streamer = new AlarmStreamer(null, event, cursor);
        AlarmUtil.run(streamer, "Get Alarms");
    }

    /**
     * Action handler for getting a 'page' of alarms.
     */
    private void getAlarmPage(final ActionResult event) {
        AlarmCursor cursor = getAlarmPageCursor(event);
        int pageSize = 500;
        Value value = event.getParameter(PAGE_SIZE);
        if ((value != null) && (value.getNumber() != null)) {
            pageSize = value.getNumber().intValue();
        }
        if (pageSize > 0) {
            int page = 0;
            value = event.getParameter(PAGE);
            if ((value != null) && (value.getNumber() != null)) {
                page = value.getNumber().intValue();
            }
            cursor.setPaging(page, pageSize);
        }
        AlarmStreamer streamer = new AlarmStreamer(null, event, cursor);
        AlarmUtil.run(streamer, "Get Alarm Page");
    }

    /**
     * Action handler for getting a 'page' of alarms.
     */
    private void getAlarmPageCount(final ActionResult event) {
        int pageSize = 500;
        Value value = event.getParameter(PAGE_SIZE);
        if ((value != null) && (value.getNumber() != null)) {
            pageSize = value.getNumber().intValue();
        }
        int pages = 0;
        if (pageSize > 0) {
            AlarmCursor cursor = getAlarmPageCursor(event);
            int count = 0;
            while (cursor.next()) {
                count++;
            }
            pages = count / pageSize;
            if (count % pageSize > 0) {
                pages++;
            }
        }
        Table table = event.getTable();
        table.addRow(Row.make(new Value(pages)));
    }

    /**
     * Executes the alarm page query.
     */
    private AlarmCursor getAlarmPageCursor(final ActionResult event) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        String sortBy = null;
        boolean ascending = true;
        AckFilter ackFilter = AckFilter.ANY;
        AlarmFilter alarmFilter = AlarmFilter.ANY;
        OpenFilter openFilter = OpenFilter.ANY;
        Value value = event.getParameter(TIME_RANGE);
        if (value != null) {
            //just fail fast if invalid time range
            String[] parts = value.getString().split("/");
            TimeUtils.decode(parts[0], from);
            TimeUtils.decode(parts[1], to);
            to.setTimeInMillis(to.getTimeInMillis() + 1); //dglux uses inclusive end
        } else {
            //Default to today.
            TimeUtils.alignDay(from);
            TimeUtils.addDays(1, to);
            TimeUtils.alignDay(to);
        }
        value = event.getParameter(ACK_STATE);
        if ((value != null) && (value.getString() != null)) {
            ackFilter = AckFilter.getMode(value.getString());
        }
        value = event.getParameter(ALARM_STATE);
        if ((value != null) && (value.getString() != null)) {
            alarmFilter = AlarmFilter.getMode(value.getString());
        }
        value = event.getParameter(OPEN_STATE);
        if ((value != null) && (value.getString() != null)) {
            openFilter = OpenFilter.getMode(value.getString());
        }
        value = event.getParameter(SORT_BY);
        if ((value != null) && (value.getString() != null)) {
            sortBy = value.getString();
        }
        value = event.getParameter(SORT_ASCENDING);
        if ((value != null) && (value.getBool() != null)) {
            ascending = value.getBool();
        }
        return Alarming.getProvider().queryAlarms(
                this, from, to, ackFilter, alarmFilter, openFilter, sortBy, ascending);
    }

    /**
     * Action handler for getting all open alarms followed by a stream of updates.
     */
    private void getOpenAlarms(final ActionResult event) {
        boolean updates = true;
        Value stream = event.getParameter(STREAM_UPDATES);
        if ((stream != null) && (stream.getBool() != null)) {
            updates = stream.getBool();
        }
        final AlarmCursor cursor = Alarming.getProvider().queryOpenAlarms(this);
        AlarmStreamer streamer = null;
        if (updates) {
            streamer = new AlarmStreamer(allUpdatesListeners, event, cursor);
        } else {
            streamer = new AlarmStreamer(null, event, cursor);
        }
        allUpdatesListenerCache = null;
        AlarmUtil.run(streamer, "Open Alarms");
    }

    /**
     * Adds all child watch objects to the given bucket.
     */
    void getWatches(Collection<AlarmWatch> bucket) {
        AlarmObject child;
        for (int i = 0, len = childCount(); i < len; i++) {
            child = getChild(i);
            if (child instanceof AlarmAlgorithm) {
                ((AlarmAlgorithm) child).getWatches(bucket);
            }
        }
    }

    @Override
    protected void initActions() {
        //Acknowledge All
        Action action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                acknowledgeAllOpen(event);
            }
        });
        action.addParameter(new Parameter(USER, ValueType.STRING));
        getNode().createChild(ACKNOWLEDGE_ALL, false).setSerializable(false)
                 .setAction(action).build();
        //Add Algorithm
        Node node = getNode();
        action = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                addAlgorithm(event);
            }
        });
        action.addParameter(
                new Parameter(NAME, ValueType.STRING, new Value("")));
        Set<String> algos = Alarming.getProvider().getAlarmAlgorithms().keySet();
        action.addParameter(new Parameter(TYPE, ValueType.makeEnum(algos),
                                          new Value(algos.iterator().next())));
        node.createChild("Add Algorithm", false).setSerializable(false).setAction(action)
            .build();
        //Create Alarm action
        action = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                createAlarm(event);
            }
        });
        action.setResultType(ResultType.TABLE);
        action.addParameter(new Parameter(SOURCE_PATH, ValueType.STRING,
                                          new Value("")));
        action.addParameter(
                new Parameter(CREATE_STATE, ENUM_ALARM_TYPE, new Value(ALERT)));
        action.addParameter(
                new Parameter(MESSAGE, ValueType.STRING, new Value("80 Characters Max")));
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild(CREATE_ALARM, false).setSerializable(false).setAction(action).build();
        //Get Alarms
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                getAlarms(event);
            }
        });
        action.addParameter(
                new Parameter(TIME_RANGE, ValueType.STRING, new Value("today"))
                        .setEditorType(EditorType.DATE_RANGE));
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Get Alarms", false).setSerializable(false).setAction(action).build();
        //Get Open Alarms
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                getOpenAlarms(event);
            }
        });
        action.setResultType(ResultType.STREAM);
        action.addParameter(
                new Parameter(STREAM_UPDATES, ValueType.BOOL, new Value(true)));
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Get Open Alarms", false).setSerializable(false).setAction(action)
            .build();
        //Get Alarm Page
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                getAlarmPage(event);
            }
        });
        action.addParameter(
                new Parameter(TIME_RANGE, ValueType.STRING, new Value("today"))
                        .setEditorType(EditorType.DATE_RANGE));
        action.addParameter(
                new Parameter(PAGE, ValueType.NUMBER, new Value(0)));
        action.addParameter(
                new Parameter(PAGE_SIZE, ValueType.NUMBER, new Value(500)));
        action.addParameter(
                new Parameter(ACK_STATE, ACK_STATE_ENUM, new Value(ANY)));
        action.addParameter(
                new Parameter(ALARM_STATE, ALARM_STATE_ENUM, new Value(ANY)));
        action.addParameter(
                new Parameter(OPEN_STATE, OPEN_STATE_ENUM, new Value(ANY)));
        action.addParameter(
                new Parameter(SORT_BY, SORT_TYPE, new Value(CREATED_TIME)));
        action.addParameter(
                new Parameter(SORT_ASCENDING, ValueType.BOOL, new Value(true)));
        action.setResultType(ResultType.TABLE);
        AlarmUtil.encodeAlarmColumns(action);
        getNode().createChild("Get Alarm Page", false)
                 .setSerializable(false)
                 .setAction(action)
                 .build();
        //Get Alarm Page Count
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                getAlarmPageCount(event);
            }
        });
        action.addParameter(
                new Parameter(TIME_RANGE, ValueType.STRING, new Value("today"))
                        .setEditorType(EditorType.DATE_RANGE));
        action.addParameter(
                new Parameter(PAGE_SIZE, ValueType.NUMBER, new Value(500)));
        action.addParameter(
                new Parameter(ACK_STATE, ACK_STATE_ENUM, new Value(ANY)));
        action.addParameter(
                new Parameter(ALARM_STATE, ALARM_STATE_ENUM, new Value(ANY)));
        action.addParameter(
                new Parameter(OPEN_STATE, OPEN_STATE_ENUM, new Value(ANY)));
        action.setResultType(ResultType.VALUES);
        action.addResult(new Parameter(RESULT, ValueType.NUMBER));
        getNode().createChild("Get Alarm Page Count", false)
                 .setSerializable(false)
                 .setAction(action)
                 .build();
        //Stream Escalation 1
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                streamEscalation1(event);
            }
        });
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Stream Escalation 1", false).setSerializable(false).setAction(action)
            .build();
        //Stream Escalation 2
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                streamEscalation2(event);
            }
        });
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Stream Escalation 2", false).setSerializable(false).setAction(action)
            .build();
        //Stream New Alarms
        action = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                streamNewAlarms(event);
            }
        });
        action.setResultType(ResultType.STREAM);
        AlarmUtil.encodeAlarmColumns(action);
        node.createChild("Stream New Alarms", false).setSerializable(false).setAction(action)
            .build();
        addDeleteAction("Delete Alarm Class");
    }

    @Override
    protected void initData() {
        initAttribute("icon", new Value("class.png"));
        initProperty(PURGE_CLOSED_DAYS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(PURGE_OPEN_DAYS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(ENABLED, new Value(true)).setWritable(Writable.CONFIG);
        initProperty(ESCALATION1_DYS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(ESCALATION1_HRS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(ESCALATION1_MNS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(ESCALATION2_DYS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(ESCALATION2_HRS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(ESCALATION2_MNS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(IN_ALARM_COUNT, new Value(0)).createFakeBuilder()
                                                  .setSerializable(false)
                                                  .setWritable(Writable.NEVER);
        initProperty(OPEN_ALARM_COUNT, new Value(0)).createFakeBuilder()
                                                    .setSerializable(false)
                                                    .setWritable(Writable.NEVER);
        initProperty(TTL_ALARM_COUNT, new Value(0)).createFakeBuilder()
                                                   .setSerializable(false)
                                                   .setWritable(Writable.NEVER);
        initProperty(UNACKED_ALARM_COUNT, new Value(0)).createFakeBuilder()
                                                       .setSerializable(false)
                                                       .setWritable(Writable.NEVER);
    }

    /**
     * Adds the record to all the streams in the corresponding collection.
     */
    void notifyAllUpdates(AlarmRecord record) {
        ArrayList<AlarmStreamer> list = allUpdatesListenerCache;
        synchronized (allUpdatesListeners) {
            if ((list == null) || (list.size() != allUpdatesListeners.size())) {
                allUpdatesListenerCache = new ArrayList<>();
                list = allUpdatesListenerCache;
                list.addAll(allUpdatesListeners);
            }
        }
        for (int i = list.size(); --i >= 0; ) {
            list.get(i).update(record);
        }
        getService().notifyOpenAlarmStreams(record);
        getService().updateCounts();
    }

    /**
     * Adds the record to all the streams in the corresponding collection.
     */
    private void notifyEscalation1(AlarmRecord record) {
        AlarmUtil.logInfo("Escalation 1: " + record.getOwner().getNode().getPath());
        ArrayList<AlarmStreamer> list = escalation1ListenerCache;
        synchronized (escalation1Listeners) {
            if ((list == null) || (escalation1Listeners.size() != list.size())) {
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
        AlarmUtil.logInfo("Escalation 2: " + record.getOwner().getNode().getPath());
        ArrayList<AlarmStreamer> list = escalation2ListenerCache;
        synchronized (escalation2Listeners) {
            if ((list == null) || (escalation2Listeners.size() != list.size())) {
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
            if ((list == null) || (newAlarmListeners.size() != list.size())) {
                newAlarmListenerCache = new ArrayList<>();
                list = newAlarmListenerCache;
                list.addAll(newAlarmListeners);
            }
        }
        for (int i = list.size(); --i >= 0; ) {
            list.get(i).update(record);
        }
        getService().updateCounts();
    }

    /**
     * Whether or not escalation is needed.
     *
     * @param escalationTime The time escalation should occur.
     * @param now            The time to evaluate.
     * @return Whether or not an escalation is needed.
     */
    private boolean shouldEscalate(long escalationTime, long now) {
        return ((lastEscalationCheck < escalationTime) && (escalationTime <= now));
    }

    /**
     * Action handler for streaming esclation 1 alarms
     */
    private void streamEscalation1(final ActionResult event) {
        startStream(event, escalation1Listeners, getNode().getName() + " Escalation 1");
        escalation1ListenerCache = null;
    }

    /**
     * Action handler for streaming escalation 2 alarms
     */
    private void streamEscalation2(final ActionResult event) {
        startStream(event, escalation2Listeners, getNode().getName() + " Escalation 2");
        escalation2ListenerCache = null;
    }

    /**
     * Establishes a stream with no initial set of values.
     *
     * @param event     Action invocation event.
     * @param listeners The set the steamer object will add itself too.
     * @param title     The name of the thread handling the stream.
     */
    private void startStream(final ActionResult event,
                             Set<AlarmStreamer> listeners,
                             String title) {
        AlarmStreamer streamer = new AlarmStreamer(listeners, event, null);
        AlarmUtil.run(streamer, title);
    }

    /**
     * Action handler for streaming new alarms
     */
    private void streamNewAlarms(final ActionResult event) {
        startStream(event, newAlarmListeners, getNode().getName() + " New Alarms");
        newAlarmListenerCache = null;
    }

    /**
     * The param can be null which indicates 0 counts for everything.
     */
    void updateCounts(AlarmService.Counts counts) {
        if (counts == null) {
            counts = new Counts();
        }
        setProperty(IN_ALARM_COUNT, new Value(counts.alarms));
        setProperty(OPEN_ALARM_COUNT, new Value(counts.open));
        setProperty(TTL_ALARM_COUNT, new Value(counts.ttl));
        setProperty(UNACKED_ALARM_COUNT, new Value(counts.unacked));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}


