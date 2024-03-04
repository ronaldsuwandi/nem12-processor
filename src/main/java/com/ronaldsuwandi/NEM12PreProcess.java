package com.ronaldsuwandi;

import org.apache.commons.csv.CSVRecord;

public interface NEM12PreProcess {
    void preProcess100(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception;
    void preProcess200(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception;
    void preProcess300(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception;
    void preProcess400(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception;
    void preProcess500(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception;
    void preProcess900(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception;
}
