/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.link.*;
import org.dsa.iot.dslink.methods.requests.*;
import org.dsa.iot.dslink.node.*;
import org.dsa.iot.dslink.node.value.*;
import org.dsa.iot.dslink.util.*;
import org.dsa.iot.dslink.util.handler.*;
import org.dsa.iot.dslink.util.json.*;
import org.slf4j.*;
import java.util.*;

/**
 * Represents an alarm source that an algorithm will monitor for alarm conditions.
 * There is a primary alarm source, but other paths may be used by subclasses for
 * determining more complex conditions.
 *
 * @author Aaron Hansen
 */
public class AlarmWatch extends AbstractAlarmObject
        implements Runnable, Handler<SubscriptionValue> {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String ALARM_STATE = "Alarm State";
    private static final String ALARM_STATE_TIME = "Alarm State Time";
    private static final String LAST_ALARM_RECORD = "Last Alarm Record";
    private static final String LAST_COV = "Last COV";
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmWatch.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Calendar cachedCalendar = null;
    private boolean firstCallback = true;
    private long lastStateTime = System.currentTimeMillis();
    private AlarmAlgorithm parentAlgorithm;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public AlarmWatch() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Subscribes to the path.
     */
    @Override protected void doSteady() {
        firstCallback = true;
        Requester requester = getRequester();
        requester.subscribe(getSourcePath(), this);
        //TODO - I think this should be done on alarm
        //Add @@alarm to source
        String myPath = getAlgorithm().getAlarmClass().getService().getLinkHandler()
                .getRequesterLink().getPath() + '/' + getNode().getPath();
        JsonObject obj = new JsonObject();
        obj.put("@", "merge");
        obj.put("type", "paths");
        obj.put("val", new JsonArray().add(myPath));
        Value pathList = new Value(obj);
        String path = getSourcePath() + "/@@alarm";
        requester.set(new SetRequest(path, pathList), null);
        super.doSteady();
    }

    /**
     * Un-subscribes the path.
     */
    @Override protected void doStop() {
        getRequester().unsubscribe(getSourcePath(), null);
        parentAlgorithm = null;
    }

    /**
     * Override hook, called periodically on the entire tree, this does nothing.
     */
    protected void execute() {
    }

    /**
     * Gets from the corresponding RoConfig.
     */
    protected AlarmState getAlarmState() {
        return AlarmState.decode(getProperty(ALARM_STATE).getString());
    }

    protected AlarmAlgorithm getAlgorithm() {
        if (parentAlgorithm == null) {
            parentAlgorithm = (AlarmAlgorithm) getParent();
        }
        return parentAlgorithm;
    }

    /**
     * Attempts to return a calendar instance without instantiating a new one.  Call
     * returnCachedCalendar when complete.  This is thread-safe.
     *
     * @param millis The time to set in the returned instance.
     * @return A calendar, never null, with its internal time set to the parameter.
     */
    protected static Calendar getCachedCalendar(long millis) {
        Calendar ret = null;
        synchronized (AlarmWatch.class) {
            ret = cachedCalendar;
            cachedCalendar = null;
        }
        if (ret == null) {
            ret = Calendar.getInstance();
        }
        if (ret.getTimeInMillis() != millis) {
            ret.setTimeInMillis(millis);
        }
        return ret;
    }

    /**
     * Retrieves the alarm record for the last alarm record uuid, or null.
     *
     * @return Possibly null.
     */
    protected AlarmRecord getLastAlarmRecord() {
        UUID uuid = getLastAlarmUuid();
        if (uuid == null) {
            return null;
        }
        return Alarming.getProvider().getAlarm(uuid);
    }

    /**
     * Retrieves the UUID for the last alarm record, or null if unknown.
     *
     * @return Possibly null.
     */
    protected UUID getLastAlarmUuid() {
        Value lastAlarmRecord = getNode().getRoConfig(LAST_ALARM_RECORD);
        if (lastAlarmRecord == null) {
            return null;
        }
        String uuidStr = lastAlarmRecord.getString();
        if (uuidStr.length() == 0) {
            return null;
        }
        return UUID.fromString(uuidStr);
    }

    protected Requester getRequester() {
        return getAlgorithm().getAlarmClass().getService().getLinkHandler()
                .getRequesterLink().getRequester();
    }

    /**
     * Return the string value of the RoConfig on the surrogate node.
     */
    protected String getSourcePath() {
        return getProperty(SOURCE_PATH).getString();
    }

    /**
     * The Java instant that the watch entered the current state.
     */
    public long getStateTime() {
        return lastStateTime;
    }

    /**
     * How many milliseconds has the source has been in its current state, as best known
     * by this watch.
     */
    protected long getTimeInCurrentState() {
        return System.currentTimeMillis() - lastStateTime;
    }

    /**
     * Called when the target changes.  Sets up some internal state
     * then calls AlarmAlgorithm.update(this) asynchronously.
     */
    public void handle(SubscriptionValue subValue) {
        if (subValue.getValue().equals(getNode().getValue())) {
            if (isValid()) {
                AlarmUtil.enqueue(this);
            }
            return;
        }
        long time = subValue.getValue().getTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        Calendar cal = getCachedCalendar(time);
        getNode().setRoConfig(LAST_COV,
                              new Value(TimeUtils.encode(cal, true, null).toString()));
        returnCachedCalendar(cal);
        getNode().setValue(subValue.getValue());
        if (isValid()) {
            AlarmUtil.enqueue(this);
        }
    }

    /**
     * Adds the necessary data to the alarm service node.
     */
    @Override protected void initActions() {
        addDeleteAction("Delete Watch");
    }

    /**
     * Adds the necessary data to the alarm service node.
     */
    @Override protected void initProperties() {
        initProperty(ENABLED, new Value(true));
        initProperty(SOURCE_PATH, new Value("/path/to/node")).setWritable(Writable.NEVER);
        initProperty(ALARM_STATE, new Value(NORMAL)).setWritable(Writable.NEVER);
        Calendar cal = Calendar.getInstance();
        if (!hasProperty(ALARM_STATE_TIME)) {
            initProperty(ALARM_STATE_TIME,
                         new Value(TimeUtils.encode(cal, true, null).toString()))
                    .setWritable(Writable.NEVER);
        }
        else {
            initProperty(ALARM_STATE_TIME, null, null).setWritable(Writable.NEVER);
            TimeUtils.decode(getProperty(ALARM_STATE_TIME).getString(), cal);
        }
        lastStateTime = cal.getTimeInMillis();
        initProperty(LAST_ALARM_RECORD, new Value("")).setWritable(Writable.NEVER);
        initProperty(LAST_COV, new Value("null")).setWritable(Writable.NEVER);
    }

    /**
     * Removes @@alarm from the target.
     */
    @Override public void removed() {
        Requester requester = getRequester();
        String path = getSourcePath() + "/@@alarm";
        requester.remove(new RemoveRequest(path), null);
    }

    /**
     * Callers of getCachedCalendar should call this when they are finished with
     * the instance.
     */
    protected static void returnCachedCalendar(Calendar cal) {
        synchronized (AlarmWatch.class) {
            cachedCalendar = cal;
        }
    }

    /**
     * Used to call AlarmAlgorithm.update asynchronously.
     */
    @Override public void run() {
        getAlgorithm().update(this);
    }

    /**
     * Sets alarm state and time if the given state represents a change.
     */
    protected void setAlarmState(AlarmState state) {
        Value currentState = getProperty(ALARM_STATE);
        String stateStr = AlarmState.encode(state);
        if (stateStr.equals(currentState.getString())) {
            return;
        }
        getNode().setRoConfig(ALARM_STATE, new Value(stateStr));
        lastStateTime = System.currentTimeMillis();
        Calendar cal = getCachedCalendar(lastStateTime);
        getNode().setRoConfig(ALARM_STATE_TIME,
                              new Value(TimeUtils.encode(cal, true, null).toString()));
        returnCachedCalendar(cal);
    }

    /**
     * Sets the corresponding config.
     */
    protected void setLastAlarmUuid(UUID uuid) {
        getNode().setRoConfig(LAST_ALARM_RECORD, new Value(uuid.toString()));
    }

}
