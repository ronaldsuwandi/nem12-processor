package com.ronaldsuwandi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class NEM12PostgresOutput implements NEM12ProcessorOutput {
    // DB connection

    private final int batchSize;
    private final PreparedStatement pStatement;
    private int batchCount = 0;

    public NEM12PostgresOutput(Connection conn, int batchSize) throws SQLException {
        this.batchSize = batchSize;
        String sql = "INSERT INTO meter_readings (nmi, timestamp, consumption) VALUES (?, ?, ?);";
        pStatement = conn.prepareStatement(sql);
    }

    public void write(String nmi, LocalDate date, int intervalLengthMinutes, double[] consumptions) {
        // batching + transaction
        try {
            pStatement.setString(1, nmi);
            LocalDateTime start = date.atStartOfDay();
            for (int i = 0; i < consumptions.length; i++) {
                LocalDateTime timestamp = start.plusMinutes((long) i * intervalLengthMinutes);
                pStatement.setTimestamp(2, Timestamp.valueOf(timestamp));
                pStatement.setDouble(3, consumptions[i]);
                pStatement.addBatch();
                batchCount++;
            }
            if (batchCount % batchSize == 0) {
                pStatement.executeBatch();
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
