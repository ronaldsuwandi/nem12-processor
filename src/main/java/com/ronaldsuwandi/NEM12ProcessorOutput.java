package com.ronaldsuwandi;

import java.time.LocalDate;

public interface NEM12ProcessorOutput {
    void write(OutputEntry output);
    record OutputEntry(String nmi, String registerId, LocalDate date, int intervalLengthMinutes, double[] consumptions){}
}

