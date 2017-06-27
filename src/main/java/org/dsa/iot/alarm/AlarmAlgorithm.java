/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * Alarm algorithms evaluate the state of children AlarmWatch objects, and generate
 * alarms for them when the conditions of the algorithm are met.
 * <p/>
 * Subclasses should override the following methods:
 * <ul>
 * <li>isAlarm(AlarmWatch).</li>
 * <li>getAlarmMessage(AlarmWatch).</li>
 * </ul>
 *
 * @author Aaron Hansen
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public abstract class AlarmAlgorithm extends AbstractAlarmObject implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String AUTO_UPDATE_INTERVAL = "Auto Update Interval";
    private static final String TO_ALARM_INHIBIT = "To Alarm Inhibit";
    private static final String TO_NORMAL_INHIBIT = "To Normal Inhibit";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private AlarmState alarmType; //only subset: alert, fault or offnormal
    private ScheduledFuture autoUpdateFuture;
    private boolean updatingAll = false;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Action handler for adding watches for alarm sources.
     */
    protected void addWatch(final ActionResult event) {
        try {
            Value name = event.getParameter(NAME);
            if (name == null) {
                throw new IllegalArgumentException("Missing name");
            }
            String nameString = name.getString();
            Node parent = event.getNode().getParent();
            Node watchNode = parent.getChild(nameString, true);
            if (watchNode != null) {
                throw new IllegalArgumentException(
                        "Name already in use: " + name.getString());
            }
            Value path = event.getParameter(PATH);
            if (path == null) {
                throw new NullPointerException("Missing path");
            }
            AlarmWatch watch = (AlarmWatch) newChild(nameString, watchType());
            watch.setProperty(SOURCE_PATH, path);
        } catch (Exception x) {
            AlarmUtil.logError(getNode().getPath(), x);
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * Schedules the auto update timer.
     */
    @Override
    public void doSteady() {
        rescheduleAutoUpdate();
    }

    /**
     * Cancels the auto update timer.
     */
    @Override
    public void doStop() {
        if (autoUpdateFuture != null) {
            autoUpdateFuture.cancel(false);
            autoUpdateFuture = null;
        }
    }

    /**
     * Calls execute on all child watches.
     */
    protected void execute() {
        if (!isEnabled()) {
            return;
        }
        AlarmObject child;
        for (int i = 0, len = childCount(); i < len; i++) {
            child = getChild(i);
            if (child instanceof AlarmWatch) {
                ((AlarmWatch) child).execute();
            }
        }
    }

    /**
     * A convenience that casts the parent.
     */
    AlarmClass getAlarmClass() {
        return (AlarmClass) getParent();
    }

    /**
     * Subclasses must override to provide a brief message about the alarm.
     */
    protected abstract String getAlarmMessage(AlarmWatch watch);

    /**
     * Alert, Fault, or Offnormal.
     */
    protected AlarmState getAlarmType() {
        if (alarmType == null) {
            String str = getProperty(ALARM_TYPE).getString();
            alarmType = AlarmState.decode(str);
        }
        return alarmType;
    }

    /**
     * The to alarm inhibit time in millis.
     */
    protected long getToAlarmInhibit() {
        return getProperty(TO_ALARM_INHIBIT).getNumber().longValue() * 1000l;
    }

    /**
     * The to normal inhibit time in millis.
     */
    protected long getToNormalInhibit() {
        return getProperty(TO_NORMAL_INHIBIT).getNumber().longValue() * 1000l;
    }

    @Override
    protected void initActions() {
        Node node = getNode();
        //Add Watch
        Action action = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                addWatch(event);
            }
        });
        action.addParameter(
                new Parameter(NAME, ValueType.STRING, new Value("")));
        action.addParameter(
                new Parameter(PATH, ValueType.STRING, new Value("")));
        node.createChild("Add Watch", false).setSerializable(false).setAction(action).build();
        //Delete
        addDeleteAction("Delete Algorithm");
        //Update All
        action = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                updateAll(event);
            }
        });
        node.createChild("Update All", false).setSerializable(false).setAction(action).build();
    }

    @Override
    protected void initData() {
        initAttribute("icon", new Value("algorithm.png"));
        initProperty(ENABLED, new Value(true)).setWritable(Writable.CONFIG);
        initProperty(ALARM_TYPE, ENUM_ALARM_TYPE, new Value(ALERT))
                .setWritable(Writable.CONFIG);
        initProperty(AUTO_UPDATE_INTERVAL, new Value(0)).createFakeBuilder()
                                                        .setConfig("unit", new Value("sec"))
                                                        .setWritable(Writable.CONFIG);
        initProperty(TO_ALARM_INHIBIT, new Value(0)).createFakeBuilder()
                                                    .setConfig("unit", new Value("sec"))
                                                    .setWritable(Writable.CONFIG);
        initProperty(TO_NORMAL_INHIBIT, new Value(0)).createFakeBuilder()
                                                     .setConfig("unit", new Value("sec"))
                                                     .setWritable(Writable.CONFIG);
    }

    /**
     * Subclasses must override this method to determine the current state of the
     * watch.  This method should be as efficient as possible.
     */
    protected abstract boolean isAlarm(AlarmWatch watch);

    @Override
    protected void onPropertyChange(Node node, ValuePair valuePair) {
        if (isSteady()) {
            if (AUTO_UPDATE_INTERVAL.equals(node.getName())) {
                rescheduleAutoUpdate();
            }
            if (ALARM_TYPE.equals(node.getName())) {
                alarmType = null;
            }
        }
    }

    /**
     * Cancels an existing timer, then schedules a new one if the auto update interval
     * is greater than zero.
     */
    private void rescheduleAutoUpdate() {
        if (autoUpdateFuture != null) {
            autoUpdateFuture.cancel(false);
            autoUpdateFuture = null;
        }
        int interval = getProperty(AUTO_UPDATE_INTERVAL).getNumber().intValue();
        if (interval > 0) {
            int delay = Math.min(5, interval);
            autoUpdateFuture = Objects.getDaemonThreadPool()
                                      .scheduleAtFixedRate(this, delay, interval, TimeUnit.SECONDS);
        }
    }

    /**
     * Calls updateAll(), enabling async updates.
     */
    public void run() {
        updateAll();
    }

    /**
     * The calls getAlarmType(watch) then passes that to updateState(state,watch).  This
     * gets called by watches on cov, and by updateAll.
     */
    protected void update(AlarmWatch watch) {
        if (isAlarm(watch)) {
            updateState(getAlarmType(), watch);
        } else {
            updateState(AlarmState.NORMAL, watch);
        }
    }

    /**
     * Updates the alarm state for all child watches.
     */
    protected void updateAll() {
        synchronized (this) {
            if (updatingAll) {
                return;
            }
            updatingAll = true;
        }
        try {
            AlarmObject child;
            for (int i = 0, len = childCount(); i < len; i++) {
                child = getChild(i);
                if (child instanceof AlarmWatch) {
                    update(((AlarmWatch) child));
                    Thread.yield();
                }
            }
        } finally {
            updatingAll = false;
        }
    }

    /**
     * Action handler, will update all asynchronously.
     */
    protected void updateAll(final ActionResult event) {
        AlarmUtil.enqueue(this);
    }

    /**
     * Updates the alarm state of the watch and will create or update the corresponding
     * alarm record.  Inhibits are taken into account as well.
     */
    protected void updateState(AlarmState state, AlarmWatch watch) {
        if (!isValid() || !watch.isValid()) {
            return;
        }
        if (state == watch.getAlarmState()) {
            return;
        }
        if (state == AlarmState.NORMAL) {
            watch.setDetectionState(Boolean.FALSE);
            long inhibit = getToNormalInhibit();
            if ((inhibit > 0) && (watch.getDetectionStateDuration() < inhibit)) {
                return;
            }
        } else {
            watch.setDetectionState(Boolean.TRUE);
            long inhibit = getToAlarmInhibit();
            if ((inhibit > 0) && (watch.getDetectionStateDuration() < inhibit)) {
                return;
            }
        }
        AlarmClass alarmClass = getAlarmClass();
        watch.setAlarmState(state);
        if (state == AlarmState.NORMAL) {
            AlarmRecord rec = watch.getLastAlarmRecord();
            if (rec != null) {
                UUID uuid = rec.getUuid();
                Alarming.getProvider().returnToNormal(uuid);
                alarmClass.notifyAllUpdates(Alarming.getProvider().getAlarm(uuid));
            }
        } else {
            AlarmRecord rec = getService().createAlarm(getAlarmClass(),
                                                       watch,
                                                       watch.getSourcePath(),
                                                       state,
                                                       getAlarmMessage(watch));
            watch.setLastAlarmUuid(rec.getUuid());
            alarmClass.notifyNewRecord(rec);
            alarmClass.notifyAllUpdates(rec);
        }
    }

    /**
     * What type to use for child watches, must be AlarmWatch or a subclass of it.
     * Returns AlarmWatch.class by default.
     */
    protected Class watchType() {
        return AlarmWatch.class;
    }

}
