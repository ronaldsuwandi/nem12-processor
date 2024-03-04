package com.ronaldsuwandi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class NEM12PostgresOutput implements NEM12ProcessorOutput {
    private static final Logger logger = LoggerFactory.getLogger(NEM12FileProcessor.class);

    // DB connection
    private final DataSource dataSource;
    private final String insertSql = "INSERT INTO meter_readings (nmi, register_id, timestamp, consumption) VALUES (?, ?, ?, ?);";

    public NEM12PostgresOutput(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    public void write(OutputEntry output) {
        // batching + transaction
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(insertSql)) {

            pStatement.setString(1, output.nmi());
            pStatement.setString(2, output.registerId());
            LocalDateTime start = output.date().atStartOfDay();
            for (int i = 0; i < output.consumptions().length; i++) {
                LocalDateTime timestamp = start.plusMinutes((long) i * output.intervalLengthMinutes());
                pStatement.setTimestamp(3, Timestamp.valueOf(timestamp));
                pStatement.setDouble(4, output.consumptions()[i]);
                pStatement.addBatch();
            }
            pStatement.executeBatch();

        } catch (SQLException e) {
            logger.error("SQL error", e);
        }
    }
}
