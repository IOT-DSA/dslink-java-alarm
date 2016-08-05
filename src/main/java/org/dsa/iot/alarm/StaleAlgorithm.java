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
import org.dsa.iot.dslink.util.*;
import java.util.*;

/**
 * This algorithm creates alarms for sources whose value does not change after a certain
 * period of time. This can be useful for detecting sensor failure.
 *
 * @author Aaron Hansen
 */
public class StaleAlgorithm extends AlarmAlgorithm {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String STALE_DAYS = "Stale Days";
    private static final String STALE_HOURS = "Stale Hours";
    private static final String STALE_MINUTES = "Stale Minutes";

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
        return "Stale value";
    }

    @Override protected void initData() {
        super.initData();
        initProperty(STALE_DAYS, new Value(1)).setWritable(Writable.CONFIG);
        initProperty(STALE_HOURS, new Value(0)).setWritable(Writable.CONFIG);
        initProperty(STALE_MINUTES, new Value(0)).setWritable(Writable.CONFIG);
    }

    @Override protected boolean isAlarm(AlarmWatch watch) {
        long start = watch.getStateTime();
        Calendar from = AlarmUtil.getCalendar(start);
        TimeUtils.addDays(getProperty(STALE_DAYS).getNumber().intValue(), from);
        TimeUtils.addHours(getProperty(STALE_HOURS).getNumber().intValue(), from);
        TimeUtils.addMinutes(getProperty(STALE_MINUTES).getNumber().intValue(), from);
        long end = from.getTimeInMillis();
        AlarmUtil.recycle(from);
        return ((end - start) > watch.getTimeInCurrentState());
    }

    @Override protected void onPropertyChange(Node node, ValuePair valuePair) {
        String name = node.getName();
        if (name.equals(STALE_DAYS)) {
            AlarmUtil.enqueue(this);
        } else if (name.equals(STALE_HOURS)) {
            AlarmUtil.enqueue(this);
        } else if (name.equals(STALE_MINUTES)) {
            AlarmUtil.enqueue(this);
        }
    }

}
