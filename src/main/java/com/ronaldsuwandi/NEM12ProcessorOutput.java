package com.ronaldsuwandi;

import java.time.LocalDate;

public interface NEM12ProcessorOutput {
    void write(String nmi, LocalDate date, int intervalLengthMinutes, double[] consumptions);
}
