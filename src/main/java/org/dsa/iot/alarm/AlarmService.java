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
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.*;
import org.slf4j.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Represents the visible root node of the link.  Its purpose is to create alarm classes
 * and manage alarm records independent of alarm class.
 *
 * @author Aaron Hansen
 */
public class AlarmService extends AbstractAlarmObject implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final Logger LOGGER = LoggerFactory.getLogger(AlarmService.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private final Object uuidMutex = new Object();
    private AlarmLinkHandler alarmLinkHandler;
    private Node alarmService;
    private ScheduledFuture executeFuture;
    private boolean executing = false;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public AlarmService() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Action handler for getting alarms for a table of alarm uuids.
     */
    private void acknowledge(ActionResult event) {
        Value uuid = event.getParameter(UUID_STR);
        if (uuid == null) {
            throw new NullPointerException("Missing UUID");
        }
        Value user = event.getParameter(USER);
        if (user == null) {
            throw new NullPointerException("Missing User");
        }
        UUID uuidObj = UUID.fromString(uuid.getString());
        Alarming.getProvider().acknowledge(uuidObj, user.getString());
        AlarmRecord rec = Alarming.getProvider().getAlarm(uuidObj);
        rec.getAlarmClass().notifyAllUpdates(rec);
    }

    /**
     * Action handler for adding child nodes representing alarm classes.
     */
    private void addAlarmClass(final ActionResult event) {
        try {
            Value name = event.getParameter(NAME);
            if (name == null) {
                throw new NullPointerException("Missing name");
            }
            String nameString = name.getString();
            Node parent = getNode();
            Node alarmClassNode = parent.getChild(nameString);
            if (alarmClassNode != null) {
                throw new IllegalArgumentException(
                        "Name already in use: " + name.getString());
            }
            //Create the child node representing the alarm class.
            alarmClassNode = parent.createChild(nameString).setSerializable(true).build();
            AlarmClass alarmClass = Alarming.getProvider().newAlarmClass(nameString);
            alarmClass.init(alarmClassNode);
            addChild(alarmClass);
            LOGGER.info("Added alarm class: " + name.getString());
        } catch (Exception x) {
            LOGGER.error("addAlarmClass", x);
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * Action handler for getting alarms for a table of alarm uuids.
     */
    private void addNote(ActionResult event) {
        Value uuid = event.getParameter(UUID_STR);
        if (uuid == null) {
            throw new NullPointerException("Missing UUID");
        }
        Value user = event.getParameter(USER);
        if (user == null) {
            throw new NullPointerException("Missing User");
        }
        Value note = event.getParameter(NOTE);
        if (note == null) {
            throw new NullPointerException("Missing User");
        }
        UUID uuidObj = UUID.fromString(uuid.getString());
        Alarming.getProvider().addNote(uuidObj, user.getString(), note.getString());
        AlarmRecord rec = Alarming.getProvider().getAlarm(uuidObj);
        rec.getAlarmClass().notifyAllUpdates(rec);
    }

    /**
     * Creates a new alarm record and returns it.
     */
    protected AlarmRecord createAlarm(AlarmClass alarmClass, String sourcePath,
            AlarmState createState, String message) {
        checkSteady();
        long now = System.currentTimeMillis();
        UUID uuid = null;
        synchronized (uuidMutex) {
            int num = alarmService.getRoConfig(RECORD_COUNT).getNumber().intValue();
            alarmService.setRoConfig(RECORD_COUNT, new Value(++num));
            uuid = java.util.UUID.randomUUID();
        }
        AlarmRecord alarmRecord = Alarming.getProvider().newAlarmRecord().setUuid(uuid)
                .setAlarmClass(alarmClass).setAlarmType(createState).setCreatedTime(now)
                .setMessage(message).setSourcePath(sourcePath);
        Alarming.getProvider().addAlarm(alarmRecord);
        return alarmRecord;
    }

    /**
     * Remove all traces of the alarm record for the given UUID_STR.
     */
    protected void deleteRecord(ActionResult event) {
        Value uuid = event.getParameter(UUID_STR);
        if (uuid == null) {
            throw new NullPointerException("Missing UUID_STR");
        }
        Alarming.getProvider().deleteRecord(UUID.fromString(uuid.getString()));
    }

    /**
     * Schedules execute in the daemon thread pool.
     */
    @Override protected void doSteady() {
        executeFuture = Objects.getDaemonThreadPool()
                .scheduleAtFixedRate(this::execute, 10, 10, TimeUnit.SECONDS);
        Alarming.getProvider().start(this);
    }

    /**
     * Cancels the execute callback.
     */
    @Override protected void doStop() {
        if (executeFuture != null) {
            executeFuture.cancel(false);
        }
        Alarming.getProvider().stop();
    }

    /**
     * Housekeeping, called by an executor.  Enforces max records and max
     * record age.
     */
    protected void execute() {
        if (!isSteady()) {
            return;
        }
        synchronized (this) {
            if (executing)
                return;
            executing = true;
        }
        try {
            AlarmObject kid;
            for (int i = 0, len = childCount(); i < len; i++) {
                kid = getChild(i);
                if (kid instanceof AlarmClass) {
                    ((AlarmClass) kid).execute();
                }
            }
        } catch (Exception x) {
            LOGGER.error("execute", x);
        } finally {
            executing = false;
        }
    }

    /**
     * Action handler for getting alarms for a table of alarm uuids.
     */
    private void getAlarm(final ActionResult event) {
        Value uuid = event.getParameter(UUID_STR);
        if (uuid == null) {
            throw new NullPointerException("Missing UUID_STR");
        }
        AlarmRecord record = Alarming.getProvider()
                .getAlarm(UUID.fromString(uuid.getString()));
        event.setStreamState(StreamState.INITIALIZED);
        Table table = event.getTable();
        table.setMode(Table.Mode.APPEND);
        AlarmUtil.encodeAlarm(record, table, null, null);
        event.setStreamState(StreamState.CLOSED);
    }

    /**
     * Searches for the child object with the given name.
     */
    public AlarmClass getAlarmClass(String name) {
        AlarmObject obj = null;
        for (int i = 0, len = childCount(); i < len; i++) {
            obj = getChild(i);
            if (obj.getNode().getName().equals(name)) {
                return (AlarmClass) obj;
            }
        }
        return null;
    }

    /**
     * Returns the DSLinkHandler
     */
    public AlarmLinkHandler getLinkHandler() {
        return alarmLinkHandler;
    }

    /**
     * Action handler for getting the notes for a specific alarm record.
     */
    private void getNotes(final ActionResult event) {
        Value uuid = event.getParameter(UUID_STR);
        if (uuid == null) {
            throw new NullPointerException("Missing UUID_STR");
        }
        NoteCursor cursor = Alarming.getProvider()
                .getNotes(UUID.fromString(uuid.getString()));
        event.setStreamState(StreamState.INITIALIZED);
        Table table = event.getTable();
        table.setMode(Table.Mode.APPEND);
        StringBuilder buf = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        while (cursor.next()) {
            calendar.setTimeInMillis(cursor.getTimestamp());
            TimeUtils.encode(calendar, true, buf);
            table.addRow(Row.make(new Value(buf.toString()), new Value(cursor.getUser()),
                    new Value(cursor.getText())));
        }
        event.setStreamState(StreamState.CLOSED);
    }

    /**
     * Adds the necessary data to the alarm service node.
     */
    @Override protected void initActions() {
        //Acknowledge
        Action action = new Action(Permission.WRITE, this::acknowledge);
        action.addParameter(new Parameter(UUID_STR, ValueType.STRING));
        action.addParameter(new Parameter(USER, ValueType.STRING));
        alarmService.createChild("Acknowledge").setSerializable(false).setAction(action)
                .build();
        //Add Alarm Class action
        action = new Action(Permission.WRITE, this::addAlarmClass);
        action.addParameter(
                new Parameter(NAME, ValueType.STRING, new Value("Display Name")));
        alarmService.createChild("Add Alarm Class").setSerializable(false)
                .setAction(action).build();
        //Add Note
        action = new Action(Permission.WRITE, this::addNote);
        action.addParameter(new Parameter(UUID_STR, ValueType.STRING));
        action.addParameter(new Parameter(USER, ValueType.STRING));
        action.addParameter(new Parameter(NOTE, ValueType.STRING));
        alarmService.createChild("Add Note").setSerializable(false).setAction(action)
                .build();
        //Delete All Records action
        action = new Action(Permission.WRITE,
                (p) -> Alarming.getProvider().deleteAllRecords());
        alarmService.createChild("Delete All Records").setSerializable(false)
                .setAction(action).build();
        //Delete Record
        action = new Action(Permission.WRITE, this::deleteRecord);
        action.addParameter(new Parameter(UUID_STR, ValueType.STRING));
        alarmService.createChild("Delete Record").setSerializable(false).setAction(action)
                .build();
        //Get Alarm
        action = new Action(Permission.READ, this::getAlarm);
        action.addParameter(new Parameter("UUID", ValueType.STRING));
        action.setResultType(ResultType.TABLE);
        AlarmUtil.encodeAlarmColumns(action);
        alarmService.createChild("Get Alarm").setSerializable(false).setAction(action)
                .build();
        //Get Notes
        action = new Action(Permission.READ, this::getNotes);
        action.addParameter(new Parameter(UUID_STR, ValueType.STRING));
        action.setResultType(ResultType.TABLE);
        action.addResult(new Parameter(TIMESTAMP, ValueType.STRING));
        action.addResult(new Parameter(USER, ValueType.STRING));
        action.addResult(new Parameter(NOTE, ValueType.STRING));
        alarmService.createChild("Get Notes").setSerializable(false).setAction(action)
                .build();
        //Return To Normal
        action = new Action(Permission.WRITE, this::returnToNormal);
        action.addParameter(new Parameter(UUID_STR, ValueType.STRING));
        alarmService.createChild("Return To Normal").setSerializable(false)
                .setAction(action).build();
    }

    @Override protected void initProperties() {
        Node node = getNode();
        if (node.getConfig(ENABLED) == null) {
            node.setConfig(ENABLED, new Value(false));
        }
    }

    /**
     * Calls  Alarming.getProvider().returnToNormal()
     */
    protected void returnToNormal(ActionResult event) {
        Value uuid = event.getParameter(UUID_STR);
        if (uuid == null) {
            throw new NullPointerException("Missing UUID");
        }
        UUID uuidObj = UUID.fromString(uuid.getString());
        Alarming.getProvider().returnToNormal(uuidObj);
        AlarmRecord rec = Alarming.getProvider().getAlarm(uuidObj);
        rec.getAlarmClass().notifyAllUpdates(rec);
    }

} //class
