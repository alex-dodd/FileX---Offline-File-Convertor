package database;

import database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import models.ConversionRecord;

/**
 * This is my data access object for the ConversionRecord.
 * This class provides the methods that are used to interact with my conversion_history table.
 */
public class ConversionHistoryDAO {

    /**
     * This method nserts a new conversion record into the database.
     * @param record The ConversionRecord object to insert.
     */
    public void insertRecord(ConversionRecord record) {
        String sql = "INSERT INTO conversion_history(source_path, target_path, source_format, target_format, success, timestamp) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getSourcePath());
            pstmt.setString(2, record.getTargetPath());
            pstmt.setString(3, record.getSourceFormat());
            pstmt.setString(4, record.getTargetFormat());
            pstmt.setBoolean(5, record.isSuccess());
            pstmt.setTimestamp(6, Timestamp.valueOf(record.getTimestamp()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting record: " + e.getMessage());
        }
    }

    /**
     * This adds a new conversion record to the database.
     * This is a convenience method that calls insertRecord.
     * @param record The ConversionRecord object to add.
     */
    public void addConversionRecord(ConversionRecord record) {
        insertRecord(record);
    }

    /**
     * This collects/retrieves all the conversion records from the database.
     * @return A list of ConversionRecord objects.
     */
    public List<ConversionRecord> getAllRecords() {
        List<ConversionRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM conversion_history ORDER BY timestamp ASC";
        try (Connection conn = DatabaseManager.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(new ConversionRecord(
                        rs.getInt("id"),
                        rs.getString("source_path"),
                        rs.getString("target_path"),
                        rs.getString("source_format"),
                        rs.getString("target_format"),
                        rs.getBoolean("success"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving records: " + e.getMessage());
        }
        return records;
    }

    /**
     * THis clears asll the records from the conversion_history table.
     */
    public void clearAllRecords() {
        String sql = "DELETE FROM conversion_history";
        String resetSql = "DELETE FROM sqlite_sequence WHERE name='conversion_history'";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            stmt.executeUpdate(resetSql); // THis resets thee auto-increment sequence
        } catch (SQLException e) {
            System.err.println("Error clearing records: " + e.getMessage());
        }
    }
}
