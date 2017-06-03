/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Skeletal Alarming.Provider.
 *
 * @author Aaron Hansen
 */
public abstract class AbstractProvider implements Alarming.Provider {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    protected AlarmService service;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc} <p/>
     * This implementation configures the alarm record and calls saveRecord.
     */
    @Override
    public void acknowledge(UUID uuid, String user) {
        AlarmRecord rec = getAlarm(uuid);
        if (user == null) {
            throw new NullPointerException("User");
        }
        AlarmUtil.logInfo("Acknowledge " + uuid + " by " + user);
        if (rec.getAckTime() <= 0) {
            rec.setAckTime(System.currentTimeMillis());
            rec.setAckUser(user);
            saveRecord(rec);
        }
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation configures the alarm record and calls saveRecord, then
     * calls doAddNote.
     */
    @Override
    public void addNote(UUID uuid, String user, String note) {
        AlarmRecord rec = getAlarm(uuid);
        if (!rec.hasNotes()) {
            rec.setHasNotes(true);
            saveRecord(rec);
        }
        addNote(new Note(uuid).setUser(user).setText(note)
                              .setTimestamp(System.currentTimeMillis()));
    }

    /**
     * Called by the other addNote method, after the alarm record has been updated
     * to indicate it has notes.
     */
    protected abstract void addNote(Note note);

    /**
     * {@inheritDoc} <p/>
     * This implementation returns a map with all the algorithms defined in the
     * core alarm sdk.
     */
    @Override
    public Map<String, Class> getAlarmAlgorithms() {
        TreeMap<String, Class> ret = new TreeMap<>();
        ret.put("Boolean Algorithm", BooleanAlgorithm.class);
        ret.put("Out of Range Algorithm", OutOfRangeAlgorithm.class);
        ret.put("Stale Algorithm", StaleAlgorithm.class);
        ret.put("String Algorithm", StringAlgorithm.class);
        return ret;
    }

    /**
     * The service passed to the start method.
     */
    protected AlarmService getService() {
        return service;
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation returns an AlarmClass.class.
     */
    @Override
    public AlarmClass newAlarmClass(String name) {
        return new AlarmClass();
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation returns an AlarmRecord.class.
     */
    @Override
    public AlarmRecord newAlarmRecord() {
        return new AlarmRecord();
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation returns an AlarmService.class.
     */
    @Override
    public AlarmService newAlarmService() {
        return new AlarmService();
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation configures the alarm record and calls saveRecord.
     */
    @Override
    public void returnToNormal(UUID uuid) {
        AlarmRecord rec = getAlarm(uuid);
        if (uuid == null) {
            throw new NullPointerException("UUID");
        }
        AlarmUtil.logInfo("Return to normal " + uuid);
        if (rec.getNormalTime() <= 0) {
            rec.setNormalTime(System.currentTimeMillis());
            saveRecord(rec);
        }
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation does nothing other than store the service.
     */
    @Override
    public void start(AlarmService service) {
        this.service = service;
    }

    /**
     * {@inheritDoc} <p/>
     * This implementation does nothing.
     */
    @Override
    public void stop() {
    }

    /**
     * Called by various methods in the abstract implementation.
     */
    protected abstract void saveRecord(AlarmRecord alarmRecord);

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

} //class
