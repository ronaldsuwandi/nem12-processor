package com.ronaldsuwandi.record;

import com.ronaldsuwandi.NEM12Exception;

import java.time.LocalDate;
import java.util.Objects;

public record NMIDataDetailsRecord(
        String nmi,
        String nmiConfiguration,
        String registerId,
        String nmiSuffix,
        String mdmDataStreamIdentifier,
        String meterSerialNumber,
        String uom,
        int intervalLength,
        LocalDate nextScheduledRecordDate
) {
    public NMIDataDetailsRecord {
        Objects.requireNonNull(nmi);
        Objects.requireNonNull(nmiConfiguration);
        Objects.requireNonNull(uom);

        if (intervalLength != 5 && intervalLength != 15 && intervalLength != 30) {
            throw new NEM12Exception();
        }
    }

}
