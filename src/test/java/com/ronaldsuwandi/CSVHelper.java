package com.ronaldsuwandi;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;

public class CSVHelper {
    public static CSVRecord createCSVRecord(String csvData) {
        try (CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT)) {
            return parser.getRecords().get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
