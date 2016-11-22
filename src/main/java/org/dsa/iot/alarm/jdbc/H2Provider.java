/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import org.dsa.iot.alarm.AlarmUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Uses an in memory instance of the H2 database.
 *
 * @author Aaron Hansen
 */
public class H2Provider extends JdbcProvider {

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception x) {
            AlarmUtil.logError("Cannot load org.h2.Driver", x);
        }
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    @Override
    protected Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:h2:./db/Alarms",
                                               "alarmLink",
                                               "alarmLink");
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return null;
    }

}
