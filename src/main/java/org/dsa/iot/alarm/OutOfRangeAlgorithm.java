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
 * This algorithm creates alarms for sources whose numeric value is less than a minimum
 * value, or greater than a maximum value.
 *
 * @author Aaron Hansen
 */
public class OutOfRangeAlgorithm extends AlarmAlgorithm {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final Logger LOGGER = LoggerFactory.getLogger(
            OutOfRangeAlgorithm.class);

    private static final String MAX_VALUE = "Max Value";
    private static final String MIN_VALUE = "Min Value";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public OutOfRangeAlgorithm() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

   @Override protected String getAlarmMessage(AlarmWatch watch) {
        return "Value out of range: " + watch.getNode().getValue().toString();
    }

    @Override protected void initProperties() {
        super.initProperties();
        initProperty(MIN_VALUE, new Value(0.0d)).setWritable(Writable.CONFIG);
        initProperty(MAX_VALUE, new Value(100.0d)).setWritable(Writable.CONFIG);
    }

    @Override protected boolean isAlarm(AlarmWatch watch) {
        double val = watch.getNode().getValue().getNumber().doubleValue();
        double tmp = getProperty(MIN_VALUE).getNumber().doubleValue();
        if (val < tmp) {
            return true;
        }
        tmp = getProperty(MAX_VALUE).getNumber().doubleValue();
        if (val > tmp) {
            return true;
        }
        return false;
    }

    @Override protected void onPropertyChange(Node child, ValuePair valuePair) {
        String name = child.getName();
        if (name.equals(MIN_VALUE)) {
            AlarmUtil.enqueue(this);
        } else if (name.equals(MIN_VALUE)) {
            AlarmUtil.enqueue(this);
        }
    }

}
