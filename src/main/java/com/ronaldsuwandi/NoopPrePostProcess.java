package com.ronaldsuwandi;

import com.ronaldsuwandi.record.IntervalDataRecord;
import org.apache.commons.csv.CSVRecord;

public class NoopPrePostProcess implements NEM12PreProcess, NEM12PostProcess {
    @Override
    public void postProcess100(NEM12FileProcessor.NEM12State state)  throws NEM12Exception {

    }

    @Override
    public void postProcess200(NEM12FileProcessor.NEM12State state) throws NEM12Exception{

    }

    @Override
    public void postProcess300(NEM12FileProcessor.NEM12State state, IntervalDataRecord intervalDataRecord) throws NEM12Exception{

    }

    @Override
    public void postProcess400(NEM12FileProcessor.NEM12State state) throws NEM12Exception{

    }

    @Override
    public void postProcess500(NEM12FileProcessor.NEM12State state) throws NEM12Exception{

    }

    @Override
    public void postProcess900(NEM12FileProcessor.NEM12State state)throws NEM12Exception {

    }

    @Override
    public void preProcess100(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {

    }

    @Override
    public void preProcess200(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {

    }

    @Override
    public void preProcess300(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {

    }

    @Override
    public void preProcess400(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {

    }

    @Override
    public void preProcess500(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {

    }

    @Override
    public void preProcess900(NEM12FileProcessor.NEM12State state, CSVRecord record) throws NEM12Exception {

    }
}
