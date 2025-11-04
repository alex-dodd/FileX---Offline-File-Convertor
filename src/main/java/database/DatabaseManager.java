package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * This class manages my SQLite database connection as well as it;s schema.
 * This class is responsible for setting up and providing access to my local database.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:conversion_history.db";

    /**
     * TThis establishes a connection to my SQLite database.
     * @return The connection object to the database.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * This initialises my database schema by creating the conversion_history table if it doesn't already exist.
     * This ensures my database is ready to store conversion records.
     */
    public static void initialize() {
        try (Connection conn = connect();
            Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS conversion_history (\n" + "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" + "source_path TEXT NOT NULL,\n" + "target_path TEXT NOT NULL,\n" + "source_format TEXT NOT NULL,\n" + "target_format TEXT NOT NULL,\n" + "success BOOLEAN NOT NULL,\n" + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP\n" +");";
            stmt.execute(sql);
            System.out.println("SUCCESS!!! Database initialised successfully.");
        } catch (SQLException e) {
            System.err.println("ANOTHER ERROR!!! Error initialising database: " + e.getMessage());
        }
    }
}
