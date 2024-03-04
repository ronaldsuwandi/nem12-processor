package com.ronaldsuwandi;

import com.ronaldsuwandi.record.IntervalDataRecord;

public interface NEM12PostProcess {
    void postProcess100(NEM12FileProcessor.NEM12State state) throws NEM12Exception;
    void postProcess200(NEM12FileProcessor.NEM12State state) throws NEM12Exception;
    void postProcess300(NEM12FileProcessor.NEM12State state, IntervalDataRecord intervalDataRecord) throws NEM12Exception;
    void postProcess400(NEM12FileProcessor.NEM12State state) throws NEM12Exception;
    void postProcess500(NEM12FileProcessor.NEM12State state) throws NEM12Exception;
    void postProcess900(NEM12FileProcessor.NEM12State state) throws NEM12Exception;
}
