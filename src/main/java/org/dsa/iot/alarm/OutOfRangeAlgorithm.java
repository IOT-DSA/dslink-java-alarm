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

/**
 * This algorithm creates alarms for sources whose numeric value is less than a minimum
 * value, or greater than a maximum value.
 *
 * @author Aaron Hansen
 */
public class OutOfRangeAlgorithm extends AlarmAlgorithm {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String DEADBAND = "Deadband";
    private static final String MAX_VALUE = "Max Value";
    private static final String MIN_VALUE = "Min Value";

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
        String pattern = getProperty(MESSAGE).getString();
        return String.format(pattern, String.valueOf(watch.getCurrentValue()));
    }

    @Override
    protected void initData() {
        super.initData();
        initProperty(MIN_VALUE, new Value(0.0d)).setWritable(Writable.CONFIG);
        initProperty(MAX_VALUE, new Value(100.0d)).setWritable(Writable.CONFIG);
        initProperty(DEADBAND, new Value(0.0d)).setWritable(Writable.CONFIG);
        initProperty(MESSAGE, new Value("Value out of range: %s")).setWritable(Writable.CONFIG);
    }

    @Override
    protected boolean isAlarm(AlarmWatch watch) {
        Value value = watch.getCurrentValue();
        if (value != null) {
            double val = getNumeric(value);
            if (Double.isNaN(val)) {
                //This is not a down device alarm etc, so don't alarm on other conditions.
                return false;
            }
            double deadband = Math.abs(getProperty(DEADBAND).getNumber().doubleValue());
            double min = getProperty(MIN_VALUE).getNumber().doubleValue();
            double max = getProperty(MAX_VALUE).getNumber().doubleValue();
            if (watch.getAlarmState() == AlarmState.NORMAL) {
                if (val < (min - deadband)) {
                    return true;
                }
                if (val > (max + deadband)) {
                    return true;
                }
            } else {
                if (val <= (min + deadband)) {
                    return true;
                }
                if (val >= (max - deadband)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onPropertyChange(Node child, ValuePair valuePair) {
        if (isSteady()) {
            String name = child.getName();
            if (name.equals(MAX_VALUE)) {
                AlarmUtil.enqueue(this);
            } else if (name.equals(MIN_VALUE)) {
                AlarmUtil.enqueue(this);
            } else if (name.equals(DEADBAND)) {
                AlarmUtil.enqueue(this);
            }
        }
        super.onPropertyChange(child, valuePair);
    }

    /**
     * Try to extract a numeric from the value, even if it's another type.
     *
     * @return Double.NaN if a double can't be extracted.
     */
    private double getNumeric(Value value) {
        Number num = value.getNumber();
        if (num != null) {
            return num.doubleValue();
        }
        Boolean bool = value.getBool();
        if (bool != null) {
            return bool ? 1 : 0;
        }
        String str = value.getString();
        if (str != null) {
            try {
                return Double.parseDouble(str);
            } catch (Exception x) {
                AlarmUtil.logError(str, x);
            }
        }
        return Double.NaN;
    }

}
