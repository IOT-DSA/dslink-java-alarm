/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.value.ValueType;

/**
 * Common constants.  Names used in multiple files should be here.  Names used multiple
 * times in the same file, but nowhere else should be defined in that file.
 *
 * @author Aaron Hansen
 */
public interface AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Strings
    ///////////////////////////////////////////////////////////////////////////

    String ACK_TIME = "Ack Time";
    String ACK_USER = "Ack User";
    String ACKNOWLEDGE_ALL = "Acknowledge All";
    String ALARM_CLASS = "Alarm Class";
    String ALARM_TYPE = "Alarm Type";
    String ALARM_WATCH_COUNT = "Alarm Watch Count"; //TODO delete after 1/1/18
    String ALERT = "Alert";
    String ENABLED = "Enabled";
    String CREATE_ALARM = "Create Alarm";
    String CREATE_STATE = "Create State";
    String CREATED_TIME = "Created Time";
    String FAULT = "Fault";
    String HAS_NOTES = "Has Notes";
    String HANDLE = "Handle";
    String IN_ALARM_COUNT = "In Alarm Count";
    String IS_ACKNOWLEDGED = "Is Acknowledged";
    String IS_NORMAL = "Is Normal";
    String JAVA_TYPE = "javaType";
    String MESSAGE = "Message";
    String NAME = "Name";
    String NORMAL = "Normal";
    String NORMAL_TIME = "Normal Time";
    String NORMAL_WATCH_COUNT = "Normal Watch Count"; //TODO delete after 1/1/18
    String NOTE = "Note";
    String OFFNORMAL = "Offnormal";
    String OPEN_ALARM_COUNT = "Open Alarm Count";
    String PATH = "Path";
    String SOURCE_PATH = "Source Path";
    String STREAM_UPDATES = "Stream Updates";
    String TIMESTAMP = "Timestamp";
    String TIME_RANGE = "Time Range";
    String TTL_ALARM_COUNT = "Total Alarm Count";
    String TYPE = "Type";
    String UNACKED_ALARM_COUNT = "Unacked Alarm Count";
    String USER = "User";
    String UUID_STR = "UUID";
    String WATCH_PATH = "Watch Path";

    //Settings for H2 TCP server (and other external DBs)
    String DATABASE_PASS = "DB Password";
    String DATABASE_URL = "DB URL";
    String DATABASE_NAME = "DB NAME";
    String DATABASE_USER = "DB User";
    String JDBC_DRIVER = "JDBC Driver";
    String EXTERNAL_DB_ACCESS_ENABLED = "DB Server On";

    ///////////////////////////////////////////////////////////////////////////
    // Enums
    ///////////////////////////////////////////////////////////////////////////

    ValueType ENUM_ALARM_TYPE = ValueType.makeEnum(ALERT, FAULT, OFFNORMAL);

    ValueType ENUM_LOG_LEVEL = ValueType.makeEnum("trace", "info", "warn", "error");

    ///////////////////////////////////////////////////////////////////////////
    // Tuning
    ///////////////////////////////////////////////////////////////////////////

    /**
     * How many alarm records to send without an interleaving call to table.sendReady.
     */
    int ALARM_RECORD_CHUNK = 50;

    /**
     * How long to wait for a stream to establish before failing.
     */
    long WAIT_FOR_STREAM = 1000;

}
