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
import org.slf4j.*;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(BooleanAlgorithm.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public BooleanAlgorithm() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

   @Override protected String getAlarmMessage(AlarmWatch watch) {
        return "Value: " + watch.getNode().getValue().toString();
    }

    @Override protected void initProperties() {
        super.initProperties();
        initProperty(ALARM_VALUE, new Value(true)).setWritable(Writable.CONFIG);
    }

    @Override protected boolean isAlarm(AlarmWatch watch) {
        return getProperty(ALARM_VALUE).getBool() == watch.getNode().getValue().getBool();
    }

    @Override protected void onPropertyChange(Node child, ValuePair valuePair) {
        if (child.getName().equals(ALARM_VALUE)) {
            AlarmUtil.enqueue(this);
        }
    }

}


