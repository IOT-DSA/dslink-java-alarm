/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.actions.table.Table.Mode;
import org.dsa.iot.dslink.util.TimeUtils;

/**
 * Action handler for sending a stream of alarms.  There can be an initial set to send (optional)
 * and after that, all records passed to the update method are sent.
 *
 * @author Aaron Hansen
 */
class AlarmStreamer extends AlarmActionHandler implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean closedLocally = false;
    private Collection listenerContainer;
    private AlarmCursor initialSet;
    private ActionResult request;
    private Table table;
    private LinkedList<AlarmRecord> updates = new LinkedList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Will set this as the close handler on the given request and will add/remove itself from the
     * given listenerContainer.
     *
     * @param listenerContainer Optional, where to add and remove this instance.  If this is null,
     *                          then no updates will be sent (ie only the initial set will be
     *                          sent).
     * @param initialSet        Optional, initial table to send.
     */
    public AlarmStreamer(Collection listenerContainer, ActionResult request,
                         AlarmCursor initialSet) {
        request.setCloseHandler(this);
        this.listenerContainer = listenerContainer;
        if (listenerContainer != null) {
            synchronized (listenerContainer) {
                listenerContainer.add(this);
            }
        }
        this.request = request;
        this.initialSet = initialSet;
        request.setStreamState(StreamState.INITIALIZED);
        this.table = request.getTable();
        table.setMode(Mode.APPEND);
        table.sendReady();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Force closes the stream.
     */
    public void close() {
        closedLocally = true;
    }

    /**
     * Does not return until there is a record, or the stream is closed.
     *
     * @return Possibly null if the stream is closed.
     */
    public AlarmRecord getNextUpdate() {
        synchronized (updates) {
            while (isValid() && (updates.size() == 0)) {
                try {
                    updates.wait(5000);
                } catch (Exception ignore) {
                }
                if (updates.size() > 0) {
                    return updates.removeFirst();
                }
            }
        }
        return null;
    }

    /**
     * True if there are pending updates.
     */
    public synchronized boolean hasUpdates() {
        return updates.size() > 0;
    }

    /**
     * True if both sides of the connection are open.
     */
    public boolean isValid() {
        return isOpen() && !closedLocally;
    }

    /**
     * Sends the initial set of alarms (if not null), then sends updates until the stream is
     * closed.
     */
    public void run() {
        Calendar cal = TimeUtils.reuseCalendar();
        StringBuilder buf = new StringBuilder();
        table.waitForStream(WAIT_FOR_STREAM, true);
        if (initialSet != null) {
            int count = 1;
            while (isValid() && initialSet.next()) {
                AlarmUtil.encodeAlarm(initialSet, table, cal, buf);
                if ((++count % ALARM_RECORD_CHUNK) == 0) {
                    table.sendReady();
                    Thread.yield();
                }
            }
        }
        if (isValid()) {
            request.setStreamState(StreamState.OPEN);
            table.sendReady();
            Thread.yield();
        }
        if (initialSet != null) {
            initialSet.close();
            initialSet = null;
        }
        AlarmRecord record;
        if (listenerContainer != null) {
            int count = 0;
            while (isValid()) {
                record = getNextUpdate();
                if (record != null) {
                    AlarmUtil.encodeAlarm(record, table, cal, buf);
                }
                if (isValid()) {
                    if (!hasUpdates() || (++count % ALARM_RECORD_CHUNK) == 0) {
                        count = 0;
                        table.sendReady();
                        Thread.yield();
                    }
                }
            }
        }
        if (isOpen()) {
            request.setStreamState(StreamState.CLOSED);
            table.sendReady();
            Thread.yield();
        }
        updates = null;
        if (listenerContainer != null) {
            synchronized (listenerContainer) {
                listenerContainer.remove(this);
            }
        }
        TimeUtils.recycleCalendar(cal);
    }

    /**
     * Adds a record to the update queue.
     *
     * @param record Do not use an AlarmCursor.
     */
    public void update(AlarmRecord record) {
        if (isValid()) {
            synchronized (updates) {
                updates.add(record);
                updates.notify();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

} //class


