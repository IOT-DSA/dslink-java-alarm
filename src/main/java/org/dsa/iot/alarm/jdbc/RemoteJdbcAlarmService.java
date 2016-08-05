/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm.jdbc;

import org.dsa.iot.alarm.*;
import org.dsa.iot.dslink.node.*;
import org.dsa.iot.dslink.node.value.*;
import org.slf4j.*;

/**
 * {@inheritDoc} <p/>
 * This adds the configuration needed to establish a connection to a remote JDBC
 * data source.
 *
 * @author Aaron Hansen
 */
public class RemoteJdbcAlarmService extends AlarmService {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final String DATABASE_PASS = "Database Password";
    static final String DATABASE_URL = "Database URL";
    static final String DATABASE_USER = "Database User";
    static final String JDBC_DRIVER = "JDBC Driver";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override public void doStart() {
        loadDriverClass();
        super.doStart();
    }

    public String getDatabasePass() {
        return getProperty(DATABASE_PASS).getString();
    }

    public String getDatabaseUrl() {
        return getProperty(DATABASE_URL).getString();
    }

    public String getDatabaseUser() {
        return getProperty(DATABASE_USER).getString();
    }

    public String getJdbcDriver() {
        return getProperty(JDBC_DRIVER).getString();
    }

    @Override protected void initData() {
        super.initData();
        initProperty(JDBC_DRIVER, new Value("com.driver.ClassName"))
                .setWritable(Writable.CONFIG);
        initProperty(DATABASE_URL, new Value("jdbc://localhost:1527"))
                .setWritable(Writable.CONFIG);
        initProperty(DATABASE_USER, new Value("userName"))
                .setWritable(Writable.CONFIG);
        initProperty(DATABASE_PASS, new Value("userPass"))
                .setWritable(Writable.CONFIG);
    }

    /**
     * Calls Class.forName on the value of the JDBC_DRIVER property.
     */
    protected void loadDriverClass() {
        String className = getJdbcDriver();
        if (className.length() > 0) {
            try {
                Class.forName(className);
            } catch (Exception x) {
                AlarmUtil.logError("Cannot load JDBC driver",x);
            }
        }
    }

    @Override protected void onPropertyChange(Node node, ValuePair valuePair) {
        if (JDBC_DRIVER.equals(node.getName())) {
            loadDriverClass();
        }
    }

} //class
