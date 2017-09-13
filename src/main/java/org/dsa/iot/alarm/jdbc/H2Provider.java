/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm.jdbc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dsa.iot.alarm.AlarmService;
import org.dsa.iot.alarm.AlarmUtil;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.Value;
import org.h2.tools.Server;

/**
 * Uses an in memory instance of the H2 database.
 *
 * @author Aaron Hansen
 */
public class H2Provider extends JdbcProvider {

    private static Server server;
    private static AlarmService service;
    private static String DB_NAME = "./db/Alarms";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception x) {
            AlarmUtil.logError("Cannot load org.h2.Driver", x);
        }
    }

    H2Provider() {
        super();
        try {
            server = Server.createTcpServer().start();
            System.out.println("URL: jdbc:h2:" + server.getURL() + "/" + DB_NAME);
        } catch (SQLException e) {
            AlarmUtil.logError("Cannot start Web Server", e);
            System.out.println("Cannot start Web Server" + e);
        }
    }

    protected void initData() {
        service.initProperty(AlarmService.JDBC_DRIVER, new Value("org.h2.Driver"))
                .setWritable(Writable.CONFIG);
        service.initProperty(AlarmService.DATABASE_URL, new Value("jdbc:h2:" + server.getURL() + "/" + DB_NAME))
                .setWritable(Writable.CONFIG);
        service.initProperty(AlarmService.DATABASE_USER, new Value("alarmLink"))
                .setWritable(Writable.CONFIG);
        service.initProperty(AlarmService.DATABASE_PASS, new Value("alarmLink"))
                .setWritable(Writable.CONFIG);
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    @Override
    protected Connection getConnection() {
        if (service == null) {
            service = getService();
            initData();
        }

        try {
            System.out.println("Got this far");
            System.out.println(service.getProperty(AlarmService.DATABASE_USER).getString());
            System.out.println(service.getProperty(AlarmService.DATABASE_PASS).getString());
            return DriverManager.getConnection(service.getProperty(AlarmService.DATABASE_URL).getString(), //"jdbc:h2:~/test"
                    service.getProperty(AlarmService.DATABASE_USER).getString(), //"sa"
                    service.getProperty(AlarmService.DATABASE_PASS).getString()); //""
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return null;
    }

}
