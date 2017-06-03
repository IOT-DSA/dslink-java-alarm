/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Calendar;
import java.util.concurrent.Callable;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Misc utilities.
 *
 * @author Aaron Hansen
 */
public class AlarmUtil implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(AlarmService.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Calendar calendarCache;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    // Prevent construction.
    private AlarmUtil() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Encodes the columns for an action that returns a table/stream of alarms.
     *
     * @param record   The record to encode.
     * @param table    Where to encode the record.
     * @param cacheCal Optional but efficient if encoding many rows at once.
     * @param cacheBuf Optional but efficient if encoding many rows at once.
     */
    public static void encodeAlarm(AlarmRecord record, Table table, Calendar cacheCal,
                                   StringBuilder cacheBuf) {
        if (cacheBuf == null) {
            cacheBuf = new StringBuilder();
        }
        String createdTime = null;
        String normalTime = null;
        String ackTime = null;
        boolean recycleCal = false;
        if (cacheCal == null) {
            cacheCal = getCalendar(record.getCreatedTime());
            recycleCal = true;
        } else {
            cacheCal.setTimeInMillis(record.getCreatedTime());
        }
        cacheBuf.setLength(0);
        createdTime = TimeUtils.encode(cacheCal, true, cacheBuf).toString();
        if (record.getNormalTime() > 0) {
            cacheBuf.setLength(0);
            cacheCal.setTimeInMillis(record.getNormalTime());
            normalTime = TimeUtils.encode(cacheCal, true, cacheBuf).toString();
        }
        if (record.getAckTime() > 0) {
            cacheBuf.setLength(0);
            cacheCal.setTimeInMillis(record.getAckTime());
            ackTime = TimeUtils.encode(cacheCal, true, cacheBuf).toString();
        }
        //Alarm classes can be deleted.
        String alarmClassName = "";
        AlarmClass alarmClass = record.getAlarmClass();
        if (alarmClass != null) {
            alarmClassName = alarmClass.getNode().getName();
        }
        String watchPath = null;
        AlarmWatch watch = record.getAlarmWatch();
        if (watch != null) {
            watchPath = watch.getPath();
        }
        table.addRow(Row.make(new Value(record.getUuid().toString()),
                              new Value(record.getSourcePath()),
                              new Value(alarmClassName),
                              new Value(createdTime),
                              new Value(AlarmState.encode(record.getAlarmType())),
                              new Value(normalTime),
                              new Value(ackTime),
                              new Value(record.getAckUser()),
                              new Value(record.getMessage()),
                              new Value(record.hasNotes()),
                              new Value(watchPath),
                              new Value(record.isNormal()),
                              new Value(record.isAcknowledged())));
        if (recycleCal) {
            recycle(cacheCal);
        }
    }

    /**
     * Encodes the columns for an action that returns a table of alarms.
     */
    public static void encodeAlarmColumns(Action action) {
        action.addResult(new Parameter(toColumnName(UUID_STR), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(SOURCE_PATH), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(ALARM_CLASS), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(CREATED_TIME), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(ALARM_TYPE), ENUM_ALARM_TYPE));
        action.addResult(new Parameter(toColumnName(NORMAL_TIME), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(ACK_TIME), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(ACK_USER), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(MESSAGE), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(HAS_NOTES), ValueType.BOOL));
        action.addResult(new Parameter(toColumnName(WATCH_PATH), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(IS_NORMAL), ValueType.STRING));
        action.addResult(new Parameter(toColumnName(IS_ACKNOWLEDGED), ValueType.STRING));
    }

    /**
     * Enqueues the parameter into the alarming thread pool.
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public static void enqueue(Callable callable) {
        Objects.getDaemonThreadPool().submit(callable);
    }

    /**
     * Enqueues the parameter into the alarming thread pool.
     */
    public static void enqueue(Runnable runnable) {
        Objects.getDaemonThreadPool().submit(runnable);
    }

    /**
     * Abstracts the implementation as that is likely to change in the future.
     */
    public static void logError(String msg, Throwable x) {
        LOG.error(msg, x);
    }

    /**
     * Abstracts the implementation as that is likely to change in the future.
     */
    public static void logInfo(String msg) {
        LOG.info(msg);
    }

    /**
     * Abstracts the implementation as that is likely to change in the future.
     */
    public static void logTrace(String msg) {
        LOG.trace(msg);
    }

    /**
     * Abstracts the implementation as that is likely to change in the future.
     */
    public static void logWarning(String msg) {
        LOG.warn(msg);
    }

    /**
     * Attempts to reuse a cached instance.  Callers should return the calendar with the
     * recycle method.
     *
     * @param arg Time to set in the calendar.
     * @return Never null, will create a new calendar if necessary.
     */
    public static Calendar getCalendar(long arg) {
        Calendar calendar = null;
        synchronized (StaleAlgorithm.class) {
            if (calendarCache != null) {
                calendar = calendarCache;
                calendarCache = null;
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTimeInMillis(arg);
        return calendar;
    }

    /**
     * Submit a calendar for reuse.
     */
    public static void recycle(Calendar arg) {
        calendarCache = arg;
    }

    /**
     * Creates a daemon thread to execute the given runnable.
     *
     * @param runnable   What to run.
     * @param threadName Optional, name to give the thread.
     */
    public static void run(Runnable runnable, String threadName) {
        Thread thread = new Thread(runnable);
        if (threadName == null) {
            threadName = "Alarm Link";
        }
        thread.setName(threadName);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Replaces spaces with underscores.
     */
    private static String toColumnName(String columnName) {
        return columnName.replace(' ', '_');
    }

    /**
     * Ensures that a runtime exception is thrown. If the given error is not a
     * runtime exception, then one will be thrown with the parameter as the inner
     * exception.
     */
    public static void throwRuntime(Throwable error) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        }
        throw new RuntimeException(error);
    }

    /**
     * Looks for the JAVA_TYPE config and if found, instantiates an instance and calls
     * init for the given node thus, loading the entire subtree of AlarmObjects.
     *
     * @param node The node the alarm object represents/proxies.
     * @return An instance of the javaType config, or null if there isn't one.
     * @throws RuntimeException Which will wrap the true exception if there are issues instantiating
     *                          the type.
     */
    public static AlarmObject tryCreateAlarmObject(Node node) {
        AlarmObject ret = null;
        String typeName = null;
        try {
            Value value = node.getRoConfig(JAVA_TYPE);
            if (value == null) {
                return null;
            }
            typeName = value.getString();
            Class type = Class.forName(typeName);
            ret = (AlarmObject) type.newInstance();
            ret.init(node);
            return ret;
        } catch (Exception x) {
            logError(node.getPath(), x);
            throwRuntime(x);
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
