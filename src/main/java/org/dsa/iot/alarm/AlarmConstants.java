/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.value.*;

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

    String ALERT = "Alert";
    String ENABLED = "Enabled";
    String CREATE_ALARM = "Create Alarm";
    String CREATE_STATE = "Create State";
    String FAULT = "Fault";
    String JAVA_TYPE = "javaType";
    String MESSAGE = "Message";
    String NAME = "Name";
    String NORMAL = "Normal";
    String NOTE = "Note";
    String OFFNORMAL = "Offnormal";
    String PATH = "Path";
    String RECORD_COUNT = "Record Count";
    String SOURCE_PATH = "Source Path";
    String TIMESTAMP = "Timestamp";
    String TYPE = "Type";
    String USER = "User";
    String UUID_STR = "UUID";

    ///////////////////////////////////////////////////////////////////////////
    // Enums
    ///////////////////////////////////////////////////////////////////////////

    ValueType ENUM_ALARM_TYPE = ValueType.makeEnum(ALERT, FAULT, OFFNORMAL);

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
