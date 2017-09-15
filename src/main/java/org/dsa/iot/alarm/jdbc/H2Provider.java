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
import java.sql.Statement;

import org.dsa.iot.alarm.AlarmService;
import org.dsa.iot.alarm.AlarmUtil;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.h2.tools.Server;

/**
 * Uses an in memory instance of the H2 database.
 *
 * @author Aaron Hansen
 */
public class H2Provider extends JdbcProvider {

    private static Server server;
    private static AlarmService service;
    private static String DEF_DB_NAME = "./db/Alarms";
    private static String DEF_USR = "alarmLink";
    private static String DEF_PASS = "alarmLink";
    private static String NO_URL = "No Access";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception x) {
            AlarmUtil.logError("Cannot load org.h2.Driver", x);
        }
    }

    private void startTCPServer() {
        try {
            server = Server.createTcpServer("-tcpAllowOthers").start();
        } catch (SQLException e) {
            AlarmUtil.logError("Cannot start Web Server", e);
        }
        updateServerURL();
    }

    private void stopTCPServer() {
        if (server != null) server.stop();
        service.setProperty(AlarmService.DATABASE_URL, new Value(NO_URL));
    }

    private String getCurDBName() {
        return service.getProperty(AlarmService.DATABASE_NAME).getString();
    }

    private String getServerURL() {
        return (server != null) ? "jdbc:h2:" + server.getURL() + "/" + getCurDBName() : NO_URL;
    }

    private void updateServerURL() {
        service.setProperty(AlarmService.DATABASE_URL, new Value(getServerURL()));
    }


    protected void initData() {
        service.initProperty(AlarmService.JDBC_DRIVER, new Value("org.h2.Driver"))
                .setWritable(Writable.NEVER);
        service.initProperty(AlarmService.DATABASE_URL, new Value(getServerURL()))
                .setWritable(Writable.NEVER);
        service.initProperty(AlarmService.DATABASE_NAME, new Value(DEF_DB_NAME))
                .setWritable(Writable.CONFIG);
        service.initProperty(AlarmService.DATABASE_USER, new Value(DEF_USR))
                .setWritable(Writable.NEVER);
        service.initProperty(AlarmService.EXTERNAL_DB_ACCESS_ENABLED, new Value(false))
                .setWritable(Writable.CONFIG);
        service.initDBPassword(DEF_PASS);
    }

    protected void initActions() {
        Action action = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                handleTCPSetup(event);
            }
        });
        action.addParameter(new Parameter(AlarmService.DATABASE_NAME, ValueType.STRING));
        action.addParameter(new Parameter(AlarmService.DATABASE_USER, ValueType.STRING));
        action.addParameter(new Parameter(AlarmService.DATABASE_PASS, ValueType.STRING));
        service.getNode().createChild("Edit DB Cred", false).setSerializable(false).setAction(action)
                .build();
    }

    private void handleTCPSetup(ActionResult event) {
        Value newUsr = event.getParameter(AlarmService.DATABASE_USER);
        Value newPass = event.getParameter(AlarmService.DATABASE_PASS);
        //Probably leave it out, too confusing, creates new database.
//        Value newName = event.getParameter(AlarmService.DATABASE_NAME);
        String curUsr = service.getProperty(AlarmService.DATABASE_USER).getString();
//        if (newName != null) {
//            service.setProperty(AlarmService.DATABASE_NAME, newName);
//        }
        Connection data = getConnection();
        try {
            if (!newUsr.getString().toUpperCase().equals(curUsr.toUpperCase())) {
                Statement chg_usr = data.createStatement();
                chg_usr.execute("ALTER USER " + curUsr + " RENAME TO " + newUsr.getString());
                data.commit();
            }

            Statement chg_pass = data.createStatement();
            chg_pass.execute("ALTER USER " + newUsr.getString() + " SET PASSWORD '" + newPass.getString() + "'");
            data.commit();
        } catch (Exception ex) {
            AlarmUtil.logError("User/Pass change error:", ex);
        }

        service.setProperty(AlarmService.DATABASE_USER, newUsr);
        service.setDBPassword(newPass.getString());
    }


    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    @Override
    protected Connection getConnection() {
        if (service == null) {
            service = getService();
            initData();
            initActions();
            if (service.getProperty(AlarmService.EXTERNAL_DB_ACCESS_ENABLED).getBool()) {
                startTCPServer();
            }
        }

        try {
            updateServerURL();
            return DriverManager.getConnection("jdbc:h2:" + getCurDBName(), //"jdbc:h2:~/test"
                    service.getProperty(AlarmService.DATABASE_USER).getString(), //"sa"
                    service.getDBPassword()); //""
        } catch (Exception x) {
            AlarmUtil.logError("Failed to login:", x);
        }
        return null;
    }

    @Override
    public void changeDatabaseAccessTo(boolean allow) {
        if (allow) {
            startTCPServer();
        } else {
            stopTCPServer();
        }
    }
}
