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
import org.slf4j.*;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(StaleAlgorithm.class);

    private static final String STALE_DAYS = "Stale Days";
    private static final String STALE_HOURS = "Stale Hours";
    private static final String STALE_MINUTES = "Stale Minutes";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Calendar calendarCache;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public StaleAlgorithm() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Subclass callback for whenever a configuration variable changes.
     */
    protected void configChanged(final NodeListener.ValueUpdate update) {
        String name = update.name();
        if (name.equals(STALE_DAYS)) {
            AlarmUtil.enqueue(this);
        } else if (name.equals(STALE_HOURS)) {
            AlarmUtil.enqueue(this);
        } else if (name.equals(STALE_MINUTES)) {
            AlarmUtil.enqueue(this);
        }
    }

    @Override protected String getAlarmMessage(AlarmWatch watch) {
        return "Stale value";
    }

    /**
     * Attempts to reuse a cached instance.  Return the calendar with the recycle method.
     * @return Never null, will create a new calendar if necessary.
     */
    private static Calendar getCalendar(long arg) {
        Calendar calendar = null;
        synchronized (StaleAlgorithm.class) {
            if (calendarCache != null) {
                calendar = calendarCache;
                calendarCache = null;
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTimeInMillis(arg);
        return calendar;
    }

    @Override protected void initProperties() {
        super.initProperties();
        Node node = getNode();
        if (node.getConfig(STALE_DAYS) == null) {
            node.setConfig(STALE_DAYS, new Value(1));
        }
        if (node.getConfig(STALE_HOURS) == null) {
            node.setConfig(STALE_HOURS, new Value(0));
        }
        if (node.getConfig(STALE_MINUTES) == null) {
            node.setConfig(STALE_MINUTES, new Value(0));
        }
    }

    @Override protected boolean isAlarm(AlarmWatch watch) {
        long start = watch.getStateTime();
        Calendar from = getCalendar(start);
        TimeUtils.addDays(getProperty(STALE_DAYS).getNumber().intValue(), from);
        TimeUtils.addDays(getProperty(STALE_HOURS).getNumber().intValue(), from);
        TimeUtils.addDays(getProperty(STALE_MINUTES).getNumber().intValue(), from);
        long end = from.getTimeInMillis();
        recycle(from);
        return ((end - start) > watch.getTimeInCurrentState());
    }

    /**
     * Returns the calendar for reuse.
     */
    private static void recycle(Calendar arg) {
        calendarCache = arg;
    }

}
