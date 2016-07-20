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

    static final String DATABASE_NAME = "Database Name";
    static final String DATABASE_PASS = "Database Password";
    static final String DATABASE_URL = "Database URL";
    static final String DATABASE_USER = "Database User";
    static final String JDBC_DRIVER = "JDBC Driver";

    static final Logger LOGGER = LoggerFactory.getLogger(RemoteJdbcAlarmService.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public RemoteJdbcAlarmService() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public String getDatabaseName() {
        return getProperty(DATABASE_NAME).getString();
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

    @Override protected void initProperties() {
        super.initProperties();
        Node node = getNode();
        if (node.getConfig(JDBC_DRIVER) == null) {
            node.setConfig(JDBC_DRIVER, new Value("com.driver.ClassName"));
        }
        if (node.getConfig(DATABASE_URL) == null) {
            node.setConfig(DATABASE_URL, new Value("jdbc://localhost:1527"));
        }
        if (node.getConfig(DATABASE_NAME) == null) {
            node.setConfig(DATABASE_NAME, new Value("Alarms"));
        }
        if (node.getConfig(DATABASE_USER) == null) {
            node.setConfig(DATABASE_USER, new Value("userName"));
        }
        if (node.getConfig(DATABASE_PASS) == null) {
            node.setConfig(DATABASE_PASS, new Value("userPass"));
        }
    }

} //class
