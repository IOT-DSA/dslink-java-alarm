/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;

/**
 * This algorithm creates alarms based on String values.
 *
 * @author Aaron Hansen
 */
public class StringAlgorithm extends AlarmAlgorithm implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String ALARM_VALUE = "Alarm Value";
    private static final String ALARM_VALUE_MODE = "Alarm Value Mode";
    private static final String MODE_CONTAINS = "Contains";
    private static final String MODE_ENDSWITH = "EndsWith";
    private static final String MODE_EQUALS = "Equals";
    private static final String MODE_NOTEQUALS = "NotEquals";
    private static final String MODE_STARTSWITH = "StartsWith";

    private ValueType ENUM_VALUE_MODE = ValueType.makeEnum(
            MODE_EQUALS, MODE_NOTEQUALS, MODE_CONTAINS, MODE_STARTSWITH, MODE_ENDSWITH);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected String getAlarmMessage(AlarmWatch watch) {
        return "Value = " + watch.getCurrentValue().toString();
    }

    @Override
    protected void initData() {
        super.initData();
        initProperty(ALARM_VALUE, new Value("Value to alarm on"))
                .setWritable(Writable.CONFIG);
        initProperty(ALARM_VALUE_MODE, ENUM_VALUE_MODE, new Value(MODE_EQUALS))
                .setWritable(Writable.CONFIG);
    }

    @Override
    protected boolean isAlarm(AlarmWatch watch) {
        Value currentValue = watch.getCurrentValue();
        if (currentValue != null) {
            String alarmValue = getProperty(ALARM_VALUE).getString();
            String valueMode = getProperty(ALARM_VALUE_MODE).getString();
            String curString = currentValue.toString();
            if (valueMode.equals(MODE_EQUALS)) {
                return curString.equals(alarmValue);
            } else if (valueMode.equals(MODE_NOTEQUALS)) {
                return !curString.equals(alarmValue);
            } else if (valueMode.equals(MODE_CONTAINS)) {
                return curString.contains(alarmValue);
            } else if (valueMode.equals(MODE_STARTSWITH)) {
                return curString.startsWith(alarmValue);
            } else if (valueMode.equals(MODE_ENDSWITH)) {
                return curString.endsWith(alarmValue);
            }
        }
        return false;
    }

    @Override
    protected void onPropertyChange(Node child, ValuePair valuePair) {
        if (isSteady()) {
            if (ALARM_VALUE.equals(child.getName())) {
                AlarmUtil.enqueue(this);
            } else if (ALARM_VALUE_MODE.equals(child.getName())) {
                AlarmUtil.enqueue(this);
            }
        }
        super.onPropertyChange(child, valuePair);
    }

}


