package com.ronaldsuwandi;

import com.ronaldsuwandi.record.NMIDataDetailsRecord;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NEM12ValidatorTest {

    @Test
    void validateRecordMultiple100() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;

        CSVRecord record = CSVHelper.createCSVRecord("100,NEM12,200506081149,UNITEDDP,NEMMCO");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }

    @Test
    void validateRecordMissing100() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has900 = true;

        CSVRecord record = CSVHelper.createCSVRecord("200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }


    @Test
    void validateRecord200Without100() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = false;

        CSVRecord record = CSVHelper.createCSVRecord("200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }

    @Test
    void validateRecord200With900() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;
        state.has900 = true;

        CSVRecord record = CSVHelper.createCSVRecord("200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }


    @Test
    void validateRecord300Without100() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = false;

        CSVRecord record = CSVHelper.createCSVRecord("300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0.848,1.271,0.895,1.327,1.013,1.793,0.988,0.985,0.876,0.555,0.760,0.938,0.566,0.512,0.970,0.760,0.731,0.615,0.886,0.531,0.774,0.712,0.598,0.670,0.587,0.657,0.345,0.231,A,,,20050310121004,20050310182204");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }

    @Test
    void validateRecord300Without200() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;

        CSVRecord record = CSVHelper.createCSVRecord("300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0.848,1.271,0.895,1.327,1.013,1.793,0.988,0.985,0.876,0.555,0.760,0.938,0.566,0.512,0.970,0.760,0.731,0.615,0.886,0.531,0.774,0.712,0.598,0.670,0.587,0.657,0.345,0.231,A,,,20050310121004,20050310182204");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }

    @Test
    void validateRecord300With900() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;
        state.has900 = true;

        CSVRecord record = CSVHelper.createCSVRecord("300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0.848,1.271,0.895,1.327,1.013,1.793,0.988,0.985,0.876,0.555,0.760,0.938,0.566,0.512,0.970,0.760,0.731,0.615,0.886,0.531,0.774,0.712,0.598,0.670,0.587,0.657,0.345,0.231,A,,,20050310121004,20050310182204");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }


    @Test
    void validateRecord900Without100() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = false;
        state.has900 = true;

        CSVRecord record = CSVHelper.createCSVRecord("900");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }

    @Test
    void validateRecordMultiple900() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;
        state.has900 = true;

        CSVRecord record = CSVHelper.createCSVRecord("900");
        assertThrows(NEM12Exception.class, () -> NEM12Validator.validateRecord(state, record));
    }

    @Test
    void validateRecord200() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;
        state.has900 = false;

        CSVRecord record = CSVHelper.createCSVRecord("200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610");
        NEM12Validator.validateRecord(state, record);
    }
    @Test
    void validateRecord300() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;
        state.dataDetailsRecord = new NMIDataDetailsRecord("nmi","config","register","","","","kwh",5,null);
        state.has900 = false;

        CSVRecord record = CSVHelper.createCSVRecord("300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0.848,1.271,0.895,1.327,1.013,1.793,0.988,0.985,0.876,0.555,0.760,0.938,0.566,0.512,0.970,0.760,0.731,0.615,0.886,0.531,0.774,0.712,0.598,0.670,0.587,0.657,0.345,0.231,A,,,20050310121004,20050310182204");
        NEM12Validator.validateRecord(state, record);
    }
    @Test
    void validateRecord() {
        NEM12FileProcessor.NEM12State state = new NEM12FileProcessor.NEM12State();
        state.has100 = true;
        state.dataDetailsRecord = new NMIDataDetailsRecord("nmi","config","register","","","","kwh",5,null);
        state.has900 = false;

        CSVRecord record = CSVHelper.createCSVRecord("900");
        NEM12Validator.validateRecord(state, record);
    }
}
