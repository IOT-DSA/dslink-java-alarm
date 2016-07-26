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
import org.dsa.iot.dslink.node.actions.table.*;
import org.dsa.iot.dslink.node.value.*;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Misc utilities.
 *
 * @author Aaron Hansen
 */
public class AlarmUtil implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private AlarmAlgorithm parentAlgorithm;

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
        if (cacheCal == null) {
            cacheCal = Calendar.getInstance();
        }
        if (cacheBuf == null) {
            cacheBuf = new StringBuilder();
        } else {
            cacheBuf.setLength(0);
        }
        String createdTime = null;
        String normalTime = null;
        String ackTime = null;
        if (record.getCreatedTime() > 0) {
            cacheCal.setTimeInMillis(record.getCreatedTime());
            createdTime = TimeUtils.encode(cacheCal, true, cacheBuf).toString();
        }
        if (record.getNormalTime() > 0) {
            cacheCal.setTimeInMillis(record.getNormalTime());
            normalTime = TimeUtils.encode(cacheCal, true, cacheBuf).toString();
        }
        if (record.getAckTime() > 0) {
            cacheCal.setTimeInMillis(record.getAckTime());
            ackTime = TimeUtils.encode(cacheCal, true, cacheBuf).toString();
        }
        table.addRow(Row.make(new Value(record.getUuid().toString()),
                              new Value(record.getSourcePath()),
                              new Value(record.getAlarmClass().getNode().getName()),
                              new Value(createdTime),
                              new Value(AlarmState.encode(record.getAlarmType())),
                              new Value(normalTime), new Value(ackTime),
                              new Value(record.getAckUser()),
                              new Value(record.getMessage()),
                              new Value(record.getHasNotes())));
    }

    /**
     * Encodes the columns for an action that returns a table of alarms.
     */
    public static void encodeAlarmColumns(Action action) {
        action.addResult(new Parameter(UUID_STR, ValueType.STRING));
        action.addResult(new Parameter(SOURCE_PATH, ValueType.STRING));
        action.addResult(new Parameter(ALARM_CLASS, ValueType.STRING));
        action.addResult(new Parameter(CREATED_TIME, ValueType.STRING));
        action.addResult(new Parameter(ALARM_TYPE, ENUM_ALARM_TYPE));
        action.addResult(new Parameter(NORMAL_TIME, ValueType.STRING));
        action.addResult(new Parameter(ACK_TIME, ValueType.STRING));
        action.addResult(new Parameter(ACK_USER, ValueType.STRING));
        action.addResult(new Parameter(MESSAGE, ValueType.STRING));
        action.addResult(new Parameter(HAS_NOTES, ValueType.BOOL));
    }

    /**
     * Enqueues the parameter into the alarming thread pool.
     */
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
     * @throws RuntimeException Which will wrap the true exception if there are
     *                          issues instantiating the type.
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
            AlarmService.LOGGER.error("Error creating " + typeName, x);
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
