/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.link.Requester;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.SubscriptionValue;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.util.TimeUtils;
import org.dsa.iot.dslink.util.handler.Handler;

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

    private static final String ALARM_STATE_TIME = "Alarm State Time";
    protected static final String CURRENT_VALUE = "Current Value";
    private static final String LAST_ALARM_RECORD = "Last Alarm Record";
    private static final String LAST_COV = "Last COV";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private Boolean alarmDetected = null;
    private long alarmDetectedTime = System.currentTimeMillis();
    private long lastCov = alarmDetectedTime;
    private long lastStateTime = alarmDetectedTime;
    private AlarmAlgorithm parentAlgorithm;
    private String subscribedPath;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The current value of the watch, or null if that doesn't make sense.
     */
    public Value getCurrentValue() {
        return getProperty(CURRENT_VALUE);
    }

    /**
     * Time of the last cov;
     */
    public long getLastCov() {
        return lastCov;
    }

    @Override
    public void getOpenUUIDs(ArrayList<UUID> uuidList) {
        UUID openUUID = getLastAlarmUuid();
        if (openUUID != null) {
            uuidList.add(openUUID);
        }
    }

    /**
     * The Java instant that the watch entered the current state.
     */
    public long getStateTime() {
        return lastStateTime;
    }

    /**
     * Called when the target changes.  Sets up some internal state
     * then calls AlarmAlgorithm.update(this) asynchronously.
     */
    public void handle(SubscriptionValue subValue) {
        try {
            if (subValue.getValue().equals(getNode().getValue())) {
                if (isValid()) {
                    AlarmUtil.enqueue(this);
                }
                return;
            }
            lastCov = System.currentTimeMillis();
            Calendar cal = AlarmUtil.getCalendar(lastCov);
            setProperty(LAST_COV,
                        new Value(TimeUtils.encode(cal, true, null).toString()));
            AlarmUtil.recycle(cal);
            Value value = subValue.getValue();
            if (value != null) {
                Value curVal = getCurrentValue();
                if (curVal == null) {
                    initProperty(CURRENT_VALUE, value).setWritable(Writable.NEVER);
                } else {
                    if (curVal.getType() != subValue.getValue().getType()) {
                        Node child = getNode().getChild(CURRENT_VALUE, false);
                        child.setValueType(subValue.getValue().getType());
                    }
                    setProperty(CURRENT_VALUE, value);
                }
            }
            if (isValid()) {
                AlarmUtil.enqueue(this);
            }
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
        }
    }

    /**
     * Used to call AlarmAlgorithm.update asynchronously.
     */
    @Override
    public void run() {
        getAlgorithm().update(this);
    }

    /**
     * Subscribes to the path.
     */
    @Override
    protected void doSteady() {
        AlarmUtil.enqueue(new Runnable() {
            @Override
            public void run() {
                subscribePath();
            }
        });
        super.doSteady();
    }

    /**
     * Un-subscribes the path.
     */
    @Override
    protected void doStop() {
        unsubscribePath();
        parentAlgorithm = null;
    }

    /**
     * Override hook, called periodically on the entire tree; this does nothing.
     */
    protected void execute() {
    }

    /**
     * Gets from the corresponding property.
     */
    protected AlarmState getAlarmState() {
        return AlarmState.decode(getProperty(ALARM_STATE).getString());
    }

    /**
     * @return The parent algorithm node.
     */
    protected AlarmAlgorithm getAlgorithm() {
        if (parentAlgorithm == null) {
            parentAlgorithm = (AlarmAlgorithm) getParent();
        }
        return parentAlgorithm;
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
        Value lastAlarmRecord = getProperty(LAST_ALARM_RECORD);
        if (lastAlarmRecord == null) {
            return null;
        }
        String uuidStr = lastAlarmRecord.getString();
        if (uuidStr.length() == 0) {
            return null;
        }
        return UUID.fromString(uuidStr);
    }

    /**
     * This could return null if the requester isn't connected.
     */
    protected Requester getRequester() {
        DSLink link = getService().getLinkHandler().getRequesterLink();
        if (link != null) {
            return link.getRequester();
        }
        return null;
    }

    /**
     * Return the string value of the RoConfig on the surrogate node.
     */
    protected String getSourcePath() {
        return getProperty(SOURCE_PATH).getString();
    }

    /**
     * How many milliseconds has the source has been in its current state, as best known
     * by this watch.
     */
    protected long getTimeInCurrentState() {
        return System.currentTimeMillis() - lastStateTime;
    }

    /**
     * Adds the necessary data to the alarm service node.
     */
    @Override
    protected void initActions() {
        addDeleteAction("Delete Watch");
    }

    /**
     * Adds the necessary data to the alarm service node.
     */
    @Override
    protected void initData() {
        initAttribute("icon", new Value("watch.png"));
        initProperty(ENABLED, new Value(true)).setWritable(Writable.CONFIG);
        initProperty(SOURCE_PATH, new Value("")).setWritable(Writable.NEVER);
        initProperty(ALARM_STATE, new Value(NORMAL)).setWritable(Writable.NEVER);
        Calendar cal = AlarmUtil.getCalendar(System.currentTimeMillis());
        if (!hasProperty(ALARM_STATE_TIME)) {
            initProperty(ALARM_STATE_TIME,
                         new Value(TimeUtils.encode(cal, true, null).toString()))
                    .setWritable(Writable.NEVER);
        } else {
            initProperty(ALARM_STATE_TIME, null, null).setWritable(Writable.NEVER);
            TimeUtils.decode(getProperty(ALARM_STATE_TIME).getString(), cal);
        }
        AlarmUtil.recycle(cal);
        lastStateTime = cal.getTimeInMillis();
        initProperty(LAST_ALARM_RECORD, new Value("")).setWritable(Writable.NEVER);
        initProperty(LAST_COV, new Value("null")).setWritable(Writable.NEVER);
    }

    @Override
    protected void onPropertyChange(Node node, ValuePair valuePair) {
        if (isSteady()) {
            if (SOURCE_PATH.equals(node.getName())) {
                AlarmUtil.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        subscribePath();
                    }
                });
            }
        }
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
        setProperty(ALARM_STATE, new Value(stateStr));
        lastStateTime = System.currentTimeMillis();
        Calendar cal = AlarmUtil.getCalendar(lastStateTime);
        setProperty(ALARM_STATE_TIME,
                    new Value(TimeUtils.encode(cal, true, null).toString()));
        AlarmUtil.recycle(cal);
    }

    /**
     * Sets the corresponding property.
     */
    protected void setLastAlarmUuid(UUID uuid) {
        setProperty(LAST_ALARM_RECORD, new Value(uuid.toString()));
    }

    /**
     * Subscribes to the path.
     */
    protected void subscribePath() {
        try {
            if ((subscribedPath != null) && !subscribedPath.isEmpty()) {
                unsubscribePath();
            }
            subscribedPath = getSourcePath();
            if ((subscribedPath == null) || subscribedPath.isEmpty()) {
                return;
            }
            getService().getSubscriptions().subscribe(subscribedPath, this);
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
        }
    }

    /**
     * Un-subscribes the path that was last subscribed, not the current value of
     * the source path property.
     */
    protected void unsubscribePath() {
        try {
            if ((subscribedPath != null) && !subscribedPath.isEmpty()) {
                getService().getSubscriptions()
                            .unsubscribe(subscribedPath, this, null);
                subscribedPath = null;
            }
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
        }
    }

    /**
     * How long in millis since the change of state was first detected.
     */
    long getAlarmDetectedStateElapsedTime() {
        return System.currentTimeMillis() - alarmDetectedTime;
    }

    /**
     * Called by the algorithm each time it evaluates the alarm condition.  This tracks
     * state changes for inhibit purposes.
     *
     * @param state True for alarm, false for normal.
     */
    void setAlarmDetected(Boolean state) {
        if (alarmDetected == null) {
            if (getAlarmState() == AlarmState.NORMAL) {
                alarmDetected = Boolean.FALSE;
            } else {
                alarmDetected = Boolean.TRUE;
            }
        }
        if (alarmDetected.equals(state)) {
            return;
        }
        this.alarmDetected = state;
        this.alarmDetectedTime = System.currentTimeMillis();
    }

}
