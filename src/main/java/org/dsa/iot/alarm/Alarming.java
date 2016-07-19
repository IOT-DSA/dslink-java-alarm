/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.*;

/**
 * Each custom link will implement its own Provider and set that instance here.  There
 * can only be one provider per process.
 *
 * @author Aaron Hansen
 */
public class Alarming {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Provider provider;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the instance passed to setProvider.
     */
    public static Provider getProvider() {
        return provider;
    }

    /**
     * Each link must set it's provider here before the link is started.
     */
    public static void setProvider(Provider arg) {
        provider = arg;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The provider interface.
     */
    public static interface Provider {

        /**
         * Acknowledge the specified alarm using the give user.
         */
        public void acknowledge(UUID uuid, String user);

        /** Add a new record to the provider's store. */
        public void addAlarm(AlarmRecord newRecord);

        /** Add the given note to the alarm record indicated by the UUID. */
        public void addNote(UUID uuid, String user, String note);

        /** Delete everything. */
        public void deleteAllRecords();

        /** Delete everything related to the alarm record for the give UUID. */
        public void deleteRecord(UUID uuid);

        /**
         * The algorithms available in this link. The returned map should be mutable
         * for subclasses.  All alarm algorithms must subclass AlarmAlgorithm and
         * support the no-arg public constructor.
         */
        public Map<String,Class> getAlarmAlgorithms();

        /** The alarm record for the given UUID. */
        public AlarmRecord getAlarm(UUID uuid);

        /**
         * Returns the notes for the given alarm record.  This must not return null.
         */
        public NoteCursor getNotes(UUID uuid);

        /**
         * Called when users invoke the action to create a new alarm class.
         */
        public AlarmClass newAlarmClass(String name);

        /**
         * All records in the link should be the same type.
         */
        public AlarmRecord newAlarmRecord();

        /**
         * Called when the service if first added to the link, but upon deserialization
         * after a restart.
         */
        public AlarmService newAlarmService();

        /**
         * Return the alarm record to the normal state.
         */
        public void returnToNormal(UUID uuid);

        /**
         * Prepare any resources for usage, such as opening a database.
         */
        public void start(AlarmService service);

        /**
         * Release any resources being used.
         */
        public void stop();

        /**
         * Returns a cursor of alarms from the given alarm class in specified
         * time range.  Implementors should expect concurrent queries and database
         * updates.
         * @param from Inclusive start time, can be null.
         * @param to First excluded end time, can be null.
         */
        public AlarmCursor queryAlarms(AlarmClass alarmClass, Calendar from, Calendar to);

        /**
         * Returns a cursor of open alarms for the given alarm class.  Implementors
         * should expect concurrent queries and database updates.
         */
        public AlarmCursor queryOpenAlarms(AlarmClass alarmClass);

    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
