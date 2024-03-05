package com.ronaldsuwandi;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class NEM12Validator implements NEM12PreProcess {
    private static final Logger logger = LoggerFactory.getLogger(NEM12Validator.class);

    static void validateRecord(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        if (record.size() == 0) {
            throw new NEM12Exception("Empty record");
        }
        String recordType = record.get(0);
        boolean isValid = switch (recordType) {
            case "100" -> !state.has100 && !state.has900;
            case "200", "900" -> state.has100 && !state.has900;
            case "300", "400", "500" -> state.has100 && !state.has900 && state.dataDetailsRecord != null;
            default -> false;
        };

        if (!isValid) {
            throw new NEM12Exception("Invalid state for record type: " + recordType);
        }
    }

    @Override
    public void preProcess100(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        validateRecord(state, record);
    }

    @Override
    public void preProcess200(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        validateRecord(state, record);
        String entry = record.get(9);
        if (!entry.isEmpty()) {
            try {
                LocalDate.parse(record.get(9), NEM12FileProcessor.dateFormatter);
            } catch (DateTimeParseException e) {
                throw new NEM12Exception("Invalid date format", e);
            }
        }
        if (record.size() < 9) {
            throw new NEM12Exception("Missing entry in 200 record. Record: " + record);
        }
    }

    @Override
    public void preProcess300(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        validateRecord(state, record);

        try {
            LocalDate.parse(record.get(1), NEM12FileProcessor.dateFormatter);
        } catch (DateTimeParseException e) {
            throw new NEM12Exception("Invalid date format", e);
        }

        int intervalRecordLength = NEM12FileProcessor.MinutesInDay / state.dataDetailsRecord.intervalLength();
        if (record.size() < 2 + intervalRecordLength) {
            throw new NEM12Exception("Invalid record value for record type: " + record.get(0));
        }

        for (int i = 0; i < intervalRecordLength; i++) {
            try {
                Double.parseDouble(record.get(i + 2));
            } catch (NumberFormatException e) {
                throw new NEM12Exception("Number parsing exception", e);
            }
        }

        // records = 3
        // 0 , 1date, 2value, 3value, 4value, 5, ...
        if (record.size() > (2 + intervalRecordLength + 3)) {
            String entry = record.get(2 + intervalRecordLength + 3);
            if (!entry.isEmpty()) {
                try {
                    LocalDateTime.parse(record.get(2 + intervalRecordLength + 3), NEM12FileProcessor.dateTimeFormatter);
                } catch (DateTimeParseException e) {
                    throw new NEM12Exception("Invalid date format", e);
                }
            }
        }

        if (record.size() > (2 + intervalRecordLength + 4)) {
            String entry = record.get(2 + intervalRecordLength + 4);
            if (!entry.isEmpty()) {
                try {
                    LocalDateTime.parse(record.get(2 + intervalRecordLength + 4), NEM12FileProcessor.dateTimeFormatter);
                } catch (DateTimeParseException e) {
                    throw new NEM12Exception("Invalid date format", e);
                }
            }
        }
    }

    @Override
    public void preProcess400(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        validateRecord(state, record);
    }

    @Override
    public void preProcess500(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        validateRecord(state, record);
    }

    @Override
    public void preProcess900(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {
        validateRecord(state, record);
    }
}
