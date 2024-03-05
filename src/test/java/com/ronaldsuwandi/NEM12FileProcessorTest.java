package com.ronaldsuwandi;

import com.ronaldsuwandi.config.NEM12Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;

class NEM12FileProcessorTest {
    NEM12Config config = new NEM12Config(1, "", "", "", 1);;
    @Test
    void processValidFile() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-example.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        processor.start();
        Mockito.verify(output, Mockito.times(8)).write(Mockito.any());
    }

    @Test
    void processValidEmptyFile() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-example-valid-but-empty.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        processor.start();
        Mockito.verify(output, Mockito.times(0)).write(Mockito.any());
    }

    @Test
    void processInvalidMissing900() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-invalid-missing-footer.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        Assertions.assertThrows(NEM12Exception.class, () -> processor.start());
    }

    @Test
    void processInvalidMissing100() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-invalid-missing-header.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        Assertions.assertThrows(NEM12Exception.class, () -> processor.start());
    }
    @Test
    void processInvalidDuplicate900() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-invalid-duplicate-footer.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        Assertions.assertThrows(NEM12Exception.class, () -> processor.start());
    }

    @Test
    void processInvalidDuplicate100() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-invalid-duplicate-header.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        Assertions.assertThrows(NEM12Exception.class, () -> processor.start());
    }
    @Test
    void processInvalidMissing200() {
        NEM12ProcessorOutput output = Mockito.mock();
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-invalid-missing-200-record.csv");
        NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
        Assertions.assertThrows(NEM12Exception.class, () -> processor.start());
    }

}
