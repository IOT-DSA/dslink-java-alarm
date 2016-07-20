/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.*;
import org.dsa.iot.dslink.node.actions.*;
import org.dsa.iot.dslink.node.value.*;
import org.dsa.iot.dslink.util.*;
import org.dsa.iot.dslink.util.json.*;
import java.util.concurrent.*;

/**
 * Alarm algorithms evaluate the state of AlarmWatch objects, and generate alarms for
 * each when the conditions of the algorithm are met.
 * <p/>
 * Subclasses should override the following methods:
 * <ul>
 * <li>isAlarm(AlarmWatch).</li>
 * <li>getAlarmMessage(AlarmWatch).</li>
 * </ul>
 *
 * @author Aaron Hansen
 */
public abstract class AlarmAlgorithm extends AbstractAlarmObject implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String ALARM_TYPE = "Alarm Type";
    private static final String AUTO_UPDATE_INTERVAL = "Auto Update Interval";
    private static final String TO_ALARM_INHIBIT = "To Alarm Inhibit";
    private static final String TO_NORMAL_INHIBIT = "To Normal Inhibit";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private AlarmState alarmState; //alert, fault or offnormal
    private ScheduledFuture autoUpdateFuture;
    private boolean updatingAll = false;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public AlarmAlgorithm() {
    }

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
            Node watchNode = parent.getChild(nameString);
            if (watchNode != null) {
                throw new IllegalArgumentException(
                        "Name already in use: " + name.getString());
            }
            Value path = event.getParameter(PATH);
            if (path == null) {
                throw new NullPointerException("Missing path");
            }
            AlarmWatch watch = (AlarmWatch) newChild(nameString, watchType());
            watch.getNode().setRoConfig(PATH, path);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
    }

   /**
     * Schedules the auto update timer.
     */
    @Override public void doSteady() {
        rescheduleAutoUpdate();
        getNode().getListener().setConfigHandler((p) -> onConfigChanged(p));
    }

    /**
     * Cancels the auto update timer.
     */
    @Override public void doStop() {
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
        if (alarmState == null) {
            String str = getProperty(ALARM_TYPE).getString();
            alarmState = AlarmState.decode(str);
        }
        return alarmState;
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

    @Override protected void initActions() {
        Node node = getNode();
        //Add Watch
        Action action = new Action(Permission.WRITE, this::addWatch);
        action.addParameter(
                new Parameter(NAME, ValueType.STRING, new Value("Unique name")));
        action.addParameter(
                new Parameter(PATH, ValueType.STRING, new Value("/path/to/node")));
        node.createChild("Add Watch").setSerializable(false).setAction(action).build();
        //Set Alarm Type
        action = new Action(Permission.WRITE, this::setAlarmType);
        action.addParameter(new Parameter(TYPE, ENUM_ALARM_TYPE, new Value(ALERT)));
        node.createChild("Set Alarm Type").setSerializable(false).setAction(action)
                .build();
        //Set Alarm Type
        action = new Action(Permission.WRITE, this::setAutoUpdateInterval);
        action.addParameter(
                new Parameter(AUTO_UPDATE_INTERVAL, ValueType.NUMBER, new Value(0))
                        .setMetaData(new JsonObject().put("unit", "s")));
        node.createChild("Set Auto Update Interval").setSerializable(false).setAction(
                action).build();
        //Update All
        action = new Action(Permission.WRITE, this::updateAll);
        node.createChild("Update All").setSerializable(false).setAction(action).build();
        addDeleteAction("Delete Algorithm");
    }

    @Override protected void initProperties() {
        Node node = getNode();
        if (node.getConfig(ENABLED) == null) {
            node.setConfig(ENABLED, new Value(false));
        }
        if (node.getRoConfig(ALARM_TYPE) == null) {
            node.setRoConfig(ALARM_TYPE, new Value(ALERT));
        }
        if (node.getRoConfig(AUTO_UPDATE_INTERVAL) == null) {
            node.setRoConfig(AUTO_UPDATE_INTERVAL, new Value(0));
        }
        if (node.getConfig(TO_ALARM_INHIBIT) == null) {
            node.setConfig(TO_ALARM_INHIBIT, new Value(0));
        }
        if (node.getConfig(TO_NORMAL_INHIBIT) == null) {
            node.setConfig(TO_NORMAL_INHIBIT, new Value(0));
        }
    }

    /**
     * Subclasses must override this method to determine the current state of the
     * watch.  This method should be as efficient as possible.
     */
    protected abstract boolean isAlarm(AlarmWatch watch);

    /**
     * Subclass callback for whenever a configuration variable changes.
     */
    protected void onConfigChanged(final NodeListener.ValueUpdate update) {
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
        int interval = getNode().getRoConfig(AUTO_UPDATE_INTERVAL).getNumber().intValue();
        if (interval > 0) {
            int delay = Math.min(5, interval);
            autoUpdateFuture = Objects.getDaemonThreadPool().scheduleAtFixedRate(this,
                                                                                 delay,
                                                                                 interval,
                                                                                 TimeUnit.SECONDS);
        }
    }

    /**
     * Calls updateAll(), enabling async updates.
     */
    public void run() {
        updateAll();
    }

    /**
     * Set the alarm type config which is used to determine the alarm state
     * when alarms are first created.
     */
    protected void setAlarmType(final ActionResult event) {
        try {
            Value type = event.getParameter(TYPE);
            if (type == null) {
                throw new IllegalArgumentException("Type name");
            }
            getNode().setRoConfig(ALARM_TYPE, type);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * Set the config and reconfigures auto updating.
     */
    protected void setAutoUpdateInterval(final ActionResult event) {
        try {
            Value interval = event.getParameter(AUTO_UPDATE_INTERVAL);
            if (interval == null) {
                throw new IllegalArgumentException(AUTO_UPDATE_INTERVAL);
            }
            getNode().setRoConfig(AUTO_UPDATE_INTERVAL, interval);
            rescheduleAutoUpdate();
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
    }

    /**
     * The calls getAlarmType(watch) then passes that to updateState(state,watch).  This
     * gets called by watches on cov, and by updateAll.
     */
    protected void update(AlarmWatch watch) {
        if (isAlarm(watch))
            updateState(getAlarmType(), watch);
        else
            updateState(AlarmState.NORMAL, watch);
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
            long inhibit = getToNormalInhibit();
            if ((inhibit > 0) && (watch.getTimeInCurrentState() > inhibit)) {
                return;
            }
        } else {
            long inhibit = getToAlarmInhibit();
            if ((inhibit > 0) && (watch.getTimeInCurrentState() > inhibit)) {
                return;
            }
        }
        AlarmClass alarmClass = getAlarmClass();
        watch.setAlarmState(state);
        if (state == AlarmState.NORMAL) {
            AlarmRecord rec = watch.getLastAlarmRecord();
            if (rec != null) {
                Alarming.getProvider().returnToNormal(rec.getUuid());
                alarmClass.notifyAllUpdates(rec);
            }
        } else {
            AlarmRecord rec = alarmClass.getService().createAlarm(getAlarmClass(),
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
