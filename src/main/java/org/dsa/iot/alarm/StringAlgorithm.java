/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.ArrayList;
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
    private static final String MODE_NOT_CONTAINS = "NotContains";
    private static final String MODE_CONTAINED = "Contained";
    private static final String MODE_NOT_CONTAINED = "NotContained";
    private static final String MODE_EQUALS = "Equals";
    private static final String MODE_NOTEQUALS = "NotEquals";
    private static final String MODE_ENDSWITH = "EndsWith";
    private static final String MODE_STARTSWITH = "StartsWith";
    private static final String MODE_IN_LIST = "InList";
    private static final String MODE_NOT_IN_LIST = "NotInList";

    private ValueType ENUM_VALUE_MODE = ValueType.makeEnum(
            MODE_EQUALS,
            MODE_NOTEQUALS,
            MODE_CONTAINS,
            MODE_NOT_CONTAINS,
            MODE_IN_LIST,
            MODE_NOT_IN_LIST,
            MODE_CONTAINED,
            MODE_NOT_CONTAINED,
            MODE_STARTSWITH,
            MODE_ENDSWITH);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private ArrayList<String> list;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected String getAlarmMessage(AlarmWatch watch) {
        String pattern = getProperty(MESSAGE).getString();
        return String.format(pattern, String.valueOf(watch.getCurrentValue()));
    }

    @Override
    protected void initData() {
        super.initData();
        initProperty(ALARM_VALUE, new Value("Value to alarm on"))
                .setWritable(Writable.CONFIG);
        initProperty(ALARM_VALUE_MODE, ENUM_VALUE_MODE, new Value(MODE_EQUALS))
                .setWritable(Writable.CONFIG);
        initProperty(MESSAGE, new Value("Value = %s")).setWritable(Writable.CONFIG);
    }

    @Override
    protected boolean isAlarm(AlarmWatch watch) {
        String alarmValue = getProperty(ALARM_VALUE).toString();
        String valueMode = getProperty(ALARM_VALUE_MODE).toString();
        String curString = null;
        Value currentValue = watch.getCurrentValue();
        if (currentValue != null) {
            curString = currentValue.toString();
        } else {
            curString = "null";
        }
        if (MODE_EQUALS.equals(valueMode)) {
            return curString.equals(alarmValue);
        } else if (MODE_NOTEQUALS.equals(valueMode)) {
            return !curString.equals(alarmValue);
        } else if (MODE_CONTAINS.equals(valueMode)) {
            return curString.contains(alarmValue);
        } else if (MODE_NOT_CONTAINS.equals(valueMode)) {
            return !curString.contains(alarmValue);
        } else if (MODE_CONTAINED.equals(valueMode)) {
            return alarmValue.contains(curString);
        } else if (MODE_NOT_CONTAINED.equals(valueMode)) {
            return !alarmValue.contains(curString);
        } else if (MODE_IN_LIST.equals(valueMode)) {
            if (list == null) {
                list = Csv.readRow(alarmValue);
            }
            return list.contains(curString);
        } else if (MODE_NOT_IN_LIST.equals(valueMode)) {
            if (list == null) {
                list = Csv.readRow(alarmValue);
            }
            return !list.contains(curString);
        } else if (MODE_STARTSWITH.equals(valueMode)) {
            return curString.startsWith(alarmValue);
        } else if (MODE_ENDSWITH.equals(valueMode)) {
            return curString.endsWith(alarmValue);
        }
        return false;
    }

    @Override
    protected void onPropertyChange(Node child, ValuePair valuePair) {
        if (isSteady()) {
            if (ALARM_VALUE.equals(child.getName())) {
                list = null;
                AlarmUtil.enqueue(this);
            } else if (ALARM_VALUE_MODE.equals(child.getName())) {
                list = null;
                AlarmUtil.enqueue(this);
            }
        }
        super.onPropertyChange(child, valuePair);
    }

    /**
     * Converts decimal numbers with no decimals into integer strings.
     */
    private String toString(Value value) {
        if (value == null) {
            return "null";
        }
        String ret = value.getString();
        if (ret != null) {
            return ret;
        }
        Number n = value.getNumber();
        if (n != null) {
            if (n instanceof Double) {
                double d = n.doubleValue();
                if (d % 1d == 0) {
                    long l = (long) d;
                    ret = String.valueOf(l);
                }
            } else if (n instanceof Float) {
                float f = n.floatValue();
                if (f % 1f == 0) {
                    long l = (long) f;
                    ret = String.valueOf(l);
                }
            }
        }
        if (ret == null) {
            ret = value.toString();
        }
        return ret;
    }

}


