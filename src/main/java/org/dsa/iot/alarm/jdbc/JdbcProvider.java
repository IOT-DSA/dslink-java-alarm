/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm.jdbc;

import java.sql.*;
import java.util.Calendar;
import java.util.UUID;
import org.dsa.iot.alarm.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Alarming provider that uses a JDBC data source.  This uses a fixed schema, but
 * leaves obtaining the database connection up to subclasses.
 *
 * @author Aaron Hansen
 */
public abstract class JdbcProvider extends AbstractProvider implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String createAlarmTable =
            "create table if not exists Alarm_Records ("
                    + "Uuid varchar(36) not null, "
                    + "SourcePath varchar(254), "
                    + "AlarmClass varchar(254), "
                    + "AlarmType varchar(9), "
                    + "CreatedTime timestamp not null, "
                    + "NormalTime timestamp, "
                    + "AckTime timestamp, "
                    + "AckUser varchar(256), "
                    + "Message varchar(256), "
                    + "HasNotes boolean not null,"
                    + "IsOpen boolean not null, "
                    + "Watch integer, "
                    + "primary key (Uuid));";

    private static final String createNoteTable =
            "create table if not exists Alarm_Notes ("
                    + "Uuid varchar(36) not null, "
                    + "Timestamp timestamp not null, "
                    + "User varchar(256), "
                    + "Note longvarchar, "
                    + "primary key (Uuid,Timestamp));";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void addAlarm(final AlarmRecord arg) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                    "insert into Alarm_Records "
                            + "(Uuid, "
                            + "SourcePath, "
                            + "AlarmClass, "
                            + "AlarmType, "
                            + "CreatedTime, "
                            + "NormalTime, "
                            + "AckTime, "
                            + "AckUser, "
                            + "Message, "
                            + "HasNotes,"
                            + "IsOpen, "
                            + "Watch) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);");
            stmt.setString(1, arg.getUuid().toString());
            stmt.setString(2, arg.getSourcePath());
            stmt.setString(3, arg.getAlarmClass().getNode().getName());
            stmt.setString(4, AlarmState.encode(arg.getAlarmType()));
            stmt.setTimestamp(5, new Timestamp(arg.getCreatedTime()));
            stmt.setTimestamp(6, new Timestamp(arg.getNormalTime()));
            stmt.setTimestamp(7, new Timestamp(arg.getAckTime()));
            stmt.setString(8, arg.getAckUser());
            stmt.setString(9, arg.getMessage());
            stmt.setBoolean(10, arg.hasNotes());
            stmt.setBoolean(11, arg.isOpen());
            if (arg.getAlarmWatch() != null) {
                stmt.setInt(12, arg.getAlarmWatch().getHandle());
            } else {
                stmt.setInt(12, 0);
            }
            stmt.executeUpdate();
            conn.commit();
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        } finally {
            close(conn, stmt, null);
        }
    }

    @Override
    protected synchronized void addNote(Note arg) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            //insert the note
            stmt = conn.prepareStatement("insert into Alarm_Notes "
                                                 + "(Uuid, Timestamp, User, Note) VALUES (?,?,?,?);");
            stmt.setString(1, arg.getUUID().toString());
            stmt.setTimestamp(2, new Timestamp(arg.getTimestamp()));
            stmt.setString(3, arg.getUser());
            stmt.setString(4, arg.getText());
            stmt.executeUpdate();
            stmt.close();
            //update the alarm record
            stmt = conn.prepareStatement(
                    "update Alarm_Records set HasNotes = true where Uuid = ?;");
            stmt.setString(1, arg.getUUID().toString());
            stmt.executeUpdate();
            conn.commit();
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        } finally {
            close(conn, stmt, null);
        }
    }

    /**
     * Just a convenience for closing resources.
     *
     * @param conn      May be null.
     * @param statement May be null.
     * @param results   May be null.
     */
    static void close(Connection conn, Statement statement, ResultSet results) {
        if (results != null) {
            try {
                results.close();
            } catch (Exception x) {
                AlarmUtil.logError(results.toString(), x);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception x) {
                AlarmUtil.logError(statement.toString(), x);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception x) {
                AlarmUtil.logError(conn.toString(), x);
            }
        }
    }

    @Override
    public void deleteAllRecords() {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = getConnection();
            statement = conn.createStatement();
            statement.executeUpdate("delete from Alarm_Records;");
            statement.executeUpdate("delete from Alarm_Notes;");
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        } finally {
            close(conn, statement, null);
        }
    }

    @Override
    public void deleteRecord(UUID uuid) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            AlarmRecord rec = getAlarm(uuid);
            if (rec == null) {
                return;
            }
            conn = getConnection();
            statement = conn.prepareStatement(
                    "delete from Alarm_Records where Uuid = ?;");
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            if (rec.hasNotes()) {
                statement.close();
                statement = conn.prepareStatement(
                        "delete from Alarm_Notes where Uuid = ?;");
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        } finally {
            close(conn, statement, null);
        }
    }

    @Override
    public AlarmRecord getAlarm(UUID uuid) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        MyAlarmCursor cursor = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(
                    "select * from Alarm_Records where Uuid = ?;");
            statement.setString(1, uuid.toString());
            results = statement.executeQuery();
            cursor = new MyAlarmCursor(conn, statement, results);
            if (cursor.next()) {
                return cursor;
            }
        } catch (Exception x) {
            close(conn, statement, results);
            AlarmUtil.throwRuntime(x);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private String getColumnName(String displayName) {
        if (displayName.equals(UUID_STR)) {
            return "Uuid";
        }
        if (displayName.equals(CREATED_TIME)) {
            return "CreatedTime";
        }
        if (displayName.equals(SOURCE_PATH)) {
            return "SourcePath";
        }
        if (displayName.equals(ALARM_CLASS)) {
            return "AlarmClass";
        }
        if (displayName.equals(ALARM_TYPE)) {
            return "AlarmType";
        }
        if (displayName.equals(NORMAL_TIME)) {
            return "NormalTime";
        }
        if (displayName.equals(ACK_TIME)) {
            return "AckTime";
        }
        if (displayName.equals(ACK_USER)) {
            return "AckUser";
        }
        throw new IllegalArgumentException("Unknown column: " + displayName);
    }

    /**
     * Subclasses are responsible for obtaining a connection.
     */
    protected abstract Connection getConnection();

    @Override
    public NoteCursor getNotes(UUID uuid) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(
                    "select * from Alarm_Notes where Uuid = ? order by Timestamp;");
            statement.setString(1, uuid.toString());
            results = statement.executeQuery();
            return new MyNoteCursor(conn, statement, results);
        } catch (Exception x) {
            close(conn, statement, results);
            AlarmUtil.throwRuntime(x);
        }
        return null;
    }

    /**
     * Creates the database and tables if needed.
     */
    public void initializeDatabase() {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = getConnection();
            statement = conn.createStatement();
            statement.executeUpdate(createAlarmTable);
            statement.executeUpdate(createNoteTable);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        } finally {
            close(conn, statement, null);
        }
    }

    @Override
    public AlarmCursor queryAlarms(AlarmClass alarmClass,
                                   Calendar from,
                                   Calendar to) {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = getConnection();
            statement = conn.createStatement();
            ResultSet results = statement.executeQuery(
                    selectStatement(alarmClass,
                                    from,
                                    to,
                                    AckFilter.ANY,
                                    AlarmFilter.ANY,
                                    OpenFilter.ANY,
                                    null,
                                    true));
            return new MyAlarmCursor(conn, statement, results);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return null;
    }

    @Override
    public AlarmCursor queryAlarms(AlarmClass alarmClass,
                                   Calendar from,
                                   Calendar to,
                                   AckFilter ackFilter,
                                   AlarmFilter alarmFilter,
                                   OpenFilter openFilter,
                                   String orderBy,
                                   boolean ascending) {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = getConnection();
            statement = conn.createStatement();
            ResultSet results = statement.executeQuery(
                    selectStatement(alarmClass, from, to, ackFilter, alarmFilter,
                                    openFilter, orderBy, ascending));
            return new MyAlarmCursor(conn, statement, results);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return null;
    }

    @Override
    public AlarmCursor queryOpenAlarms(AlarmClass alarmClass) {
        try {
            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery(
                    selectStatement(alarmClass,
                                    null,
                                    null,
                                    AckFilter.ANY,
                                    AlarmFilter.ANY,
                                    OpenFilter.ANY,
                                    null,
                                    true));
            return new MyAlarmCursor(conn, statement, results);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>This only updates NormalTime, AckTime, AckUser, and IsOpen</p>
     */
    @SuppressFBWarnings("SQL_BAD_PREPARED_STATEMENT_ACCESS")
    @Override
    protected void saveRecord(AlarmRecord arg) {
        StringBuilder buf = new StringBuilder("update Alarm_Records set");
        //Indexes in prepared statements start at 1.
        int normalIdx = 0;
        int ackTimeIdx = 0;
        int ackUserIdx = 0;
        int currentIdx = 0;
        if (arg.getNormalTime() > 0) {
            normalIdx = ++currentIdx;
            buf.append(" NormalTime = ?,");
        }
        if (arg.getAckTime() > 0) {
            ackTimeIdx = ++currentIdx;
            buf.append(" AckTime = ?,");
        }
        if (arg.getAckUser() != null) {
            ackUserIdx = ++currentIdx;
            buf.append(" AckUser = ?,");
        }
        int isOpenIdx = ++currentIdx;
        int uuidIdx = ++currentIdx;
        buf.append(" IsOpen = ? where Uuid = ?");
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(buf.toString());
            if (normalIdx > 0) {
                stmt.setTimestamp(normalIdx, new Timestamp(arg.getNormalTime()));
            }
            if (ackTimeIdx > 0) {
                stmt.setTimestamp(ackTimeIdx, new Timestamp(arg.getAckTime()));
            }
            if (ackUserIdx > 0) {
                stmt.setString(ackUserIdx, arg.getAckUser());
            }
            stmt.setBoolean(isOpenIdx, arg.isOpen());
            stmt.setString(uuidIdx, arg.getUuid().toString());
            stmt.executeUpdate();
            conn.commit();
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        } finally {
            close(conn, stmt, null);
        }
    }

    /**
     * Creates a select statement based on the given parameters.
     *
     * @param alarmClass   Alarm class name, may be null.
     * @param from         Earliest inclusive created time, may be null.
     * @param to           First excluded created time, may be null.
     * @param ackFilter    Filter for ack state.
     * @param alarmFilter Filter for normal state.
     * @param openFilter   Filter for open state.
     * @param orderBy      Column to sort by.  See AlarmConstants.SORT_TYPE.
     * @param ascending    True to sort ascending, false for descending.
     */
    protected String selectStatement(AlarmClass alarmClass,
                                     Calendar from,
                                     Calendar to,
                                     AckFilter ackFilter,
                                     AlarmFilter alarmFilter,
                                     OpenFilter openFilter,
                                     String orderBy,
                                     boolean ascending) {
        StringBuilder buf = new StringBuilder();
        buf.append("select * from Alarm_Records");
        boolean hasWhere = false;
        if (alarmClass != null) {
            buf.append(" where AlarmClass = '");
            buf.append(alarmClass.getNode().getName());
            buf.append('\'');
            hasWhere = true;
        }
        if ((from != null) && (from.getTimeInMillis() > 0)) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("CreatedTime >= '");
            buf.append(new Timestamp(from.getTimeInMillis()));
            buf.append('\'');
            hasWhere = true;
        }
        if ((to != null) && (to.getTimeInMillis() > 0)) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("CreatedTime < '");
            buf.append(new Timestamp(to.getTimeInMillis()));
            buf.append('\'');
            hasWhere = true;
        }
        if (ackFilter == AckFilter.ACKED) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("AckTime > CreatedTime");
            hasWhere = true;
        } else if (ackFilter == AckFilter.UNACKED) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("AckTime < CreatedTime");
            hasWhere = true;
        }
        if (alarmFilter == AlarmFilter.ALARM) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("NormalTime < CreatedTime");
            hasWhere = true;
        } else if (alarmFilter == AlarmFilter.NORMAL) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("NormalTime > CreatedTime");
            hasWhere = true;
        }
        if (openFilter == OpenFilter.OPEN) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("IsOpen = true");
            hasWhere = true;
        } else if (openFilter == OpenFilter.CLOSED) {
            buf.append(hasWhere ? " and " : " where ");
            buf.append("IsOpen = false");
            hasWhere = true;
        }
        if (orderBy == null) {
            buf.append(" order by CreatedTime;");
        } else {
            buf.append(" order by ").append(getColumnName(orderBy));
            if (ascending) {
                buf.append(" ASC;");
            } else {
                buf.append(" DESC;");
            }
        }
        return buf.toString();
    }

    @Override
    public void start(AlarmService service) {
        super.start(service);
        initializeDatabase();
    }

    /**
     * Sets the AlarmRecord fields using the current position of the result set.
     */
    protected void toAlarm(ResultSet res, AlarmRecord rec) throws SQLException {
        rec.setUuid(UUID.fromString(res.getString("Uuid")));
        rec.setSourcePath(res.getString("SourcePath"));
        String str = res.getString("AlarmClass");
        if ((rec.getAlarmClass() == null)
                || !rec.getAlarmClass().getNode().getName().equals(str)) {
            rec.setAlarmClass(getService().getAlarmClass(str));
        }
        rec.setAlarmType(AlarmState.decode(res.getString("AlarmType")));
        Timestamp ts = res.getTimestamp("CreatedTime");
        if (ts != null) {
            rec.setCreatedTime(ts.getTime());
        }
        ts = res.getTimestamp("NormalTime");
        if (ts != null) {
            rec.setNormalTime(ts.getTime());
        }
        ts = res.getTimestamp("AckTime");
        if (ts != null) {
            rec.setAckTime(ts.getTime());
        }
        rec.setAckUser(res.getString("AckUser"));
        rec.setMessage(res.getString("Message"));
        rec.setHasNotes(res.getBoolean("HasNotes"));
        rec.setAlarmWatch(null);
        int handle = res.getInt("Watch");
        if (handle > 0) {
            Object obj = getService().getByHandle(handle);
            //If the configuration database is deleted, but the database isn't
            //cleared, then there could be bad handles.
            if (obj instanceof AlarmWatch) {
                rec.setAlarmWatch((AlarmWatch) obj);
            }
        }
    }

    /**
     * Sets the Note fields using the current position of the result set.
     */
    protected void toNote(ResultSet res, Note rec) throws SQLException {
        rec.setUUID(UUID.fromString(res.getString("Uuid")));
        rec.setUser(res.getString("User"));
        rec.setText(res.getString("Note"));
        Timestamp ts = res.getTimestamp("Timestamp");
        if (ts != null) {
            rec.setTimestamp(ts.getTime());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class MyAlarmCursor extends AlarmCursor {

        private Connection conn;
        private int limit;
        private boolean paging = false;
        private ResultSet results;
        private Statement statement;

        MyAlarmCursor(Connection conn, Statement statement, ResultSet results) {
            this.conn = conn;
            this.statement = statement;
            this.results = results;
        }

        @Override
        public void close() {
            JdbcProvider.close(conn, statement, results);
            conn = null;
            statement = null;
            results = null;
        }

        @Override
        public boolean next() {
            try {
                if (results == null) {
                    return false;
                }
                if (paging && (limit <= 0)) {
                    close();
                    return false;
                }
                if (results.next()) {
                    toAlarm(results, this);
                    if (paging) {
                        limit--;
                    }
                    return true;
                } else {
                    close();
                }
            } catch (Exception x) {
                AlarmUtil.logError("AlarmCursor.next", x);
                close();
                AlarmUtil.throwRuntime(x);
            }
            return false;
        }

        @Override
        public void setPaging(int page, int pageSize) {
            if (pageSize > 0) {
                paging = true;
                limit = pageSize;
                skip(page * pageSize);
            }
        }

        private void skip(int count) {
            try {
                if (results == null) {
                    return;
                }
                if (count <= 0) {
                    return;
                }
                while (results.next()) {
                    if (--count <= 0) {
                        return;
                    }
                }
                close();
            } catch (Exception x) {
                AlarmUtil.logError("AlarmCursor.skip", x);
                close();
                AlarmUtil.throwRuntime(x);
            }
        }

    }

    private class MyNoteCursor extends NoteCursor {

        private Connection conn;
        private ResultSet results;
        private Statement statement;

        MyNoteCursor(Connection conn, Statement statement, ResultSet results) {
            this.conn = conn;
            this.statement = statement;
            this.results = results;
        }

        @Override
        public void close() {
            JdbcProvider.close(conn, statement, results);
            conn = null;
            statement = null;
            results = null;
        }

        @Override
        public boolean next() {
            try {
                if (results == null) {
                    return false;
                }
                if (results.next()) {
                    toNote(results, this);
                    return true;
                } else {
                    close();
                }
            } catch (Exception x) {
                AlarmUtil.logError("NoteCursor.next", x);
                close();
                AlarmUtil.throwRuntime(x);
            }
            return false;
        }
    }

} //class
