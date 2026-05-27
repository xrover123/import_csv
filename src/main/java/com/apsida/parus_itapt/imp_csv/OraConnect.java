package com.apsida.parus_itapt.imp_csv;

import oracle.jdbc.pool.OracleDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class OraConnect implements AutoCloseable {
    private final Connection conn;
    private boolean connected;
    private String dlm;
    public void setDlm(String s_dlm) {dlm = s_dlm;}
    public boolean getConnected() {return connected;}
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[a-zA-Z0-9_]+$");

    // Конструктор устанавливает соединение
    private static Connection createConnection(String connectionStr, String userName, String password) {
        try {
            OracleDataSource ods = new OracleDataSource();
            ods.setURL(connectionStr);
            ods.setUser(userName);
            ods.setPassword(password);
            return ods.getConnection(); // Успешное подключение
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to Oracle database (" + connectionStr + "): " + e.getMessage());
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("SQL state: " + e.getSQLState());
            return null; // Неудачное подключение
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during connection: " + e.getMessage());
            return null; // Неудачное подключение
        }
    }
    public OraConnect(String ipAddr, String dbPort, String dbName, String userName, String password)  {
        String connectionStr = "jdbc:oracle:thin:@//" + ipAddr + ":" + dbPort + "/" + dbName;
        this.conn = createConnection(connectionStr,userName,password);
        if (this.conn == null) {
            this.connected = false;
        } else {
            this.connected = true;
            dlm = "|";
        }
    }
    private boolean isValidIdentifier(String identifier) {
        return SAFE_IDENTIFIER.matcher(identifier).matches();
    }

    public boolean fileImport(String fileName, String tableName) {
        if (!connected) {
            System.err.println("❌ Not connected to database.");
            return false;
        }
        if (isValidIdentifier(tableName)) {
            System.err.println("❌ Invalid table name: " + tableName+".");
            return false;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

            String line;
            if ((line = reader.readLine()) == null) {
                System.err.println("❌ the \""+fileName+"\" file is empty.");
                return false;
            } else {
                String[] columnNames = line.split(dlm);
                String sql = buildInsertSql(fileName, tableName, columnNames);
                if (sql == null) {
                    return false;
                }
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(dlm);
                        for (int i = 0; i < values.length; i++) {
                            stmt.setString(i + 1, values[i].trim());
                        }
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    conn.commit();
                    System.out.println("✔ File " + fileName + " imported successfully.");
                    return true;
                }
            }
        } catch (IOException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Rollback failed: " + ex.getMessage()+".");
            }
            System.err.println("❌ Error importing file " + fileName + ": " + e.getMessage()+".");
            e.printStackTrace();
            return false;
        }
    }

    private String buildInsertSql(String fileName, String tableName, String[] columnNames) {
        if (columnNames.length == 0) {
            System.err.println("❌ There is no description of the fields in the first line of the \""+fileName+"\" file.");
            return null;
        }
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 0; i < columnNames.length; i++) {
            if (isValidIdentifier(columnNames[i])) {
                sql.append(columnNames[i]);
                if (i < columnNames.length - 1) sql.append(", ");
            } else {
                System.err.println("❌ The structure of the \""+fileName+"\" file is broken.");
                return null;
            }
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columnNames.length; i++) {
            sql.append("?");
            if (i < columnNames.length - 1) sql.append(", ");
        }
        sql.append(")");
        return sql.toString();
    }

    // Закрываем соединение
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            connected = false;
            System.out.println("🔌 Connection closed");
        }
    }
}
