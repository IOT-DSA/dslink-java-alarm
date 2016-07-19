/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.methods.*;
import org.dsa.iot.dslink.node.actions.*;
import org.dsa.iot.dslink.node.actions.table.*;
import java.util.*;

/**
 * Action handler for sending a stream of alarms.  There can be an initial set to
 * send (optional) and after that, all records passed to the update method are sent.
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

    private boolean closed = false;
    private Collection container;
    private AlarmCursor initialSet;
    private ActionResult request;
    private Table table;
    private LinkedList<AlarmRecord> updates = new LinkedList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Will set this as the close handler on the given request and will add and
     *  remove itself from the given container.
     *
     *  @param container Where to add and remove this instnace.
     *  @param initialSet Optional.
     */
    public AlarmStreamer(
            Collection container,
            ActionResult request,
            AlarmCursor initialSet) {
        request.setCloseHandler(this);
        this.container = container;
        synchronized (container) {
            container.add(this);
        }
        this.request = request;
        this.initialSet = initialSet;
        request.setStreamState(StreamState.INITIALIZED);
        this.table = request.getTable();
        table.setMode(Table.Mode.STREAM);
        table.sendReady();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Force close the stream.
     */
    public void close() {
        closed = true;
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
                } catch (Exception ignore) {}
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
        return isOpen() && !closed;
    }

    /**
     * Sends the initial set of alarms if not null, then sends updates until the
     * stream is closed.
     */
    public void run() {
        table.waitForStream(10000, true);
        if (initialSet != null) {
            int count = 1;
            while (isValid() && initialSet.next()) {
                AlarmUtil.encodeAlarm(initialSet, table, null, null);
                if ((++count % ALARM_RECORD_CHUNK) == 0) {
                    table.sendReady();
                }
            }
        }
        if (isValid()) {
            request.setStreamState(StreamState.OPEN);
            table.sendReady();
        }
        initialSet = null;
        AlarmRecord record;
        while (isValid()) {
            record = getNextUpdate();
            if (record != null) {
                AlarmUtil.encodeAlarm(initialSet, table, null, null);
            }
            if (isValid()) {
                Thread.yield();
                if (!hasUpdates()) {
                    table.sendReady();
                }
            }
        }
        if (isOpen()) {
            request.setStreamState(StreamState.CLOSED);
        }
        updates = null;
        synchronized (container) {
            container.remove(this);
        }
    }

    /**
     * Adds a record to the update queue.  Do not a record cursor.
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


