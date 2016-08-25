/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.*;
import org.dsa.iot.dslink.node.value.*;

/**
 * This algorithm creates alarms when boolean data sources turn true.  This will allow
 * other links the self detect alarmable conditions.
 *
 * @author Aaron Hansen
 */
public class BooleanAlgorithm extends AlarmAlgorithm implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String ALARM_VALUE = "Alarm Value";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override protected String getAlarmMessage(AlarmWatch watch) {
        return "Value: " + watch.getCurrentValue().toString();
    }

    @Override protected void initData() {
        super.initData();
        initProperty(ALARM_VALUE, new Value(true)).setWritable(Writable.CONFIG);
    }

    @Override protected boolean isAlarm(AlarmWatch watch) {
        Value value = watch.getCurrentValue();
        if (value != null) {
            return getProperty(ALARM_VALUE).getBool().equals(value.getBool());
        }
        return false;
    }

    @Override protected void onPropertyChange(Node child, ValuePair valuePair) {
        if (child.getName().equals(ALARM_VALUE)) {
            AlarmUtil.enqueue(this);
        }
    }

}


