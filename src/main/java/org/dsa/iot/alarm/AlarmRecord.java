/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.UUID;

/**
 * Represents the details of a unique event.
 *
 * @author Aaron Hansen
 */
public class AlarmRecord {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private long ackTime;
    private String ackUser;
    private AlarmClass alarmClass;
    private AlarmState alarmType;
    private long createdTime;
    private boolean hasNotes = false;
    private String message;
    private long normalTime;
    private String sourcePath;
    private UUID uuid;
    private AlarmWatch watch;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A value greater than zero represents the instant the alarm was acknowledged,
     * otherwise the record is unacknowledged.
     */
    public long getAckTime() {
        return ackTime;
    }

    /**
     * A value greater than zero represents the instant the alarm was acknowledged,
     * otherwise the record is unacknowledged.
     */
    public AlarmRecord setAckTime(long ackTime) {
        this.ackTime = ackTime;
        return this;
    }

    /**
     * If the ack time is greater than zero, there should be a entity name representing
     * the acknowledging entity.
     */
    public String getAckUser() {
        return ackUser;
    }

    /**
     * If the ack time is greater than zero, there should be a entity name representing
     * the acknowledging entity.
     */
    public AlarmRecord setAckUser(String ackUser) {
        this.ackUser = ackUser;
        return this;
    }

    /**
     * The associate alarm class.
     */
    public AlarmClass getAlarmClass() {
        return alarmClass;
    }

    /**
     * The associate alarm class.
     */
    public AlarmRecord setAlarmClass(AlarmClass alarmClass) {
        this.alarmClass = alarmClass;
        return this;
    }

    /**
     * The initial state of the alarm.
     */
    public AlarmState getAlarmType() {
        return alarmType;
    }

    /**
     * The initial state of the alarm, can not be NORMAL.
     *
     * @param alarmType Must not be NORMAL.
     */
    public AlarmRecord setAlarmType(AlarmState alarmType) {
        if (alarmType == AlarmState.NORMAL) {
            throw new IllegalArgumentException("Normal is not an alarm type");
        }
        this.alarmType = alarmType;
        return this;
    }

    /**
     * The alarm watch responsible for the alarm, or null.
     *
     * @return Possibly null.
     */
    public AlarmWatch getAlarmWatch() {
        return watch;
    }

    /**
     * The alarm watch responsible for the alarm, or null.
     *
     * @return Possibly null.
     */
    public AlarmRecord setAlarmWatch(AlarmWatch watch) {
        this.watch = watch;
        return this;
    }

    /**
     * The instant the alarm was created.
     */
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * The instant the alarm was created.
     */
    public AlarmRecord setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    /**
     * Whether or not there are any notes for this alarm.  Notes can be retrieved
     * using the getNotes action in the AlarmService.
     */
    public boolean getHasNotes() {
        return hasNotes;
    }

    /**
     * Whether or not there are any notes for this alarm.  Notes can be retrieved
     * using the getNotes action in the AlarmService.
     */
    public AlarmRecord setHasNotes(boolean hasNotes) {
        this.hasNotes = hasNotes;
        return this;
    }

    /**
     * Brief description of the alarm.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Brief description of the alarm.
     */
    public AlarmRecord setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * A value greater than zero represents the instant the alarm returned to normal,
     * otherwise the record is still in it's created state.
     */
    public long getNormalTime() {
        return normalTime;
    }

    /**
     * A value greater than zero represents the instant the alarm returned to normal,
     * otherwise the record is still in it's created state.
     */
    public AlarmRecord setNormalTime(long normalTime) {
        this.normalTime = normalTime;
        return this;
    }

    /**
     * The path to the alarmable entity.
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * The path to the alarmable entity.
     */
    public AlarmRecord setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    /**
     * The unique record id.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * The unique record id.
     */
    public AlarmRecord setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets all internal fields of this record to match the given.
     */
    public void copy(AlarmRecord record) {
        ackTime = record.ackTime;
        ackUser = record.ackUser;
        alarmClass = record.alarmClass;
        alarmType = record.alarmType;
        createdTime = record.createdTime;
        hasNotes = record.hasNotes;
        message = record.message;
        normalTime = record.normalTime;
        sourcePath = record.sourcePath;
        uuid = record.uuid;
        watch = record.watch;
    }

    /**
     * If the alarm watch is non-null, that will be returned, otherwise the alarm
     * class will be returned.
     */
    public AlarmObject getOwner() {
        if (watch != null) {
            return watch;
        }
        return alarmClass;
    }

    /**
     * True if the alarm type is not alert.
     */
    public boolean isAckRequired() {
        return alarmType != AlarmState.ALERT;
    }

    /**
     * True if the ack time is greater than zero.
     */
    public boolean isAcknowledged() {
        return ackTime > 0;
    }

    /**
     * True if normal and acknowledged (if acknowledgment is required).
     */
    public boolean isClosed() {
        if (!isNormal()) {
            return false;
        }
        if (isAckRequired() && !isAcknowledged()) {
            return false;
        }
        return true;
    }

    /**
     * True if the normal time is greater than zero.
     */
    public boolean isNormal() {
        return normalTime > 0;
    }

    /**
     * True if not normal or unacknowledged (if acknowledgment is required).
     */
    public boolean isOpen() {
        return !isClosed();
    }

    /**
     * Clones this record.
     */
    public AlarmRecord newCopy() {
        AlarmRecord ret = new AlarmRecord();
        ret.copy(this);
        return ret;
    }

    /**
     * Clears all fields of this record.
     */
    public void reset() {
        ackTime = 0;
        ackUser = null;
        alarmClass = null;
        alarmType = null;
        createdTime = 0;
        hasNotes = false;
        message = null;
        normalTime = 0;
        sourcePath = null;
        uuid = null;
        watch = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

} //class
