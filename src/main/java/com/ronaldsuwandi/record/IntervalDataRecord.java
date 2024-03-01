package com.ronaldsuwandi.record;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record IntervalDataRecord(
        LocalDate intervalDate,
        double[] intervalValues,
        String qualityMethod,
        String reasonCode,
        String reasonDescription,
        LocalDateTime updateDateTime,
        LocalDateTime msatsLoadDateTime
) {
    public IntervalDataRecord {
        Objects.requireNonNull(intervalDate);
    }
}
