package com.ronaldsuwandi;

import com.ronaldsuwandi.config.NEM12Config;
import com.ronaldsuwandi.record.IntervalDataRecord;
import com.ronaldsuwandi.record.NMIDataDetailsRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.*;

public class NEM12FileProcessor implements NEM12PostProcess {
    private static final Logger logger = LoggerFactory.getLogger(NEM12FileProcessor.class);
    private final NEM12ProcessorOutput processorOutput;
    private final NEM12Config config;
    private final String inputFile;
    private final CSVFormat format = CSVFormat.DEFAULT;
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final ExecutorService executorService;
    private final CountDownLatch latch;

    private final BlockingQueue<NEM12ProcessorOutput.OutputEntry> queue = new LinkedBlockingQueue<>(10000);

    public NEM12FileProcessor(NEM12Config config, String inputFile, NEM12ProcessorOutput processorOutput) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(inputFile);
        Objects.requireNonNull(processorOutput);
        this.config = config;
        this.inputFile = inputFile;
        this.processorOutput = processorOutput;
        this.executorService = Executors.newFixedThreadPool(config.threads());
        this.latch = new CountDownLatch(config.threads());
    }

    public final static int MinutesInDay = 24 * 60;

    static class NEM12State {
        boolean has100;
        boolean has900;

        NMIDataDetailsRecord dataDetailsRecord;
    }

    private NEM12State state = new NEM12State();

    void process100(CSVRecord record) {
        state.has100 = true;
    }

    void process200(CSVRecord record) {
//        System.out.println(Arrays.toString(args));

        // FIXME better array length handling
        LocalDate nextScheduledReadDate = null;
        if (record.size() == 10) {
            nextScheduledReadDate = LocalDate.parse(record.get(9), NEM12FileProcessor.dateFormatter);
        }
        state.dataDetailsRecord = new NMIDataDetailsRecord(
                record.get(1),
                record.get(2),
                record.get(3),
                record.get(4),
                record.get(5),
                record.get(6),
                record.get(7),
                Integer.parseInt(record.get(8)),
                nextScheduledReadDate
        );
    }

    IntervalDataRecord process300(CSVRecord record) {
        int intervalRecordLength = MinutesInDay / state.dataDetailsRecord.intervalLength();
        double[] intervalRecords = new double[intervalRecordLength];
        for (int i = 0; i < intervalRecordLength; i++) {
            intervalRecords[i] = Double.parseDouble(record.get(i + 2));
        }
        String qualityMethod = record.get(2 + intervalRecordLength);
        String reasonCode = record.get(2 + intervalRecordLength + 1);
        String reasonDescription = record.get(2 + intervalRecordLength + 2);
        LocalDateTime updateDateTime = null;
        {
            String entry = record.get(2 + intervalRecordLength + 3);
            if (!entry.isEmpty()) {
                updateDateTime = LocalDateTime.parse(record.get(2 + intervalRecordLength + 3), dateTimeFormatter);
            }
        }
        LocalDateTime msatsLoadDateTime = null;
        {
            String entry = record.get(2 + intervalRecordLength + 4);
            if (!entry.isEmpty()) {
                msatsLoadDateTime = LocalDateTime.parse(record.get(2 + intervalRecordLength + 4), dateTimeFormatter);
            }
        }

        LocalDate intervalDate = LocalDate.parse(record.get(1), dateFormatter);
        return new IntervalDataRecord(
                intervalDate,
                intervalRecords,
                qualityMethod,
                reasonCode,
                reasonDescription,
                updateDateTime,
                msatsLoadDateTime
        );
    }

    void process400(CSVRecord record) {
    }

    void process500(CSVRecord record) {
    }

    void process900(CSVRecord record) {
        state.has900 = true;
    }

    void process(CSVRecord record, NEM12PreProcess preProcess, NEM12PostProcess postProcess) throws NEM12Exception {
        String recordType = record.get(0);
        switch (recordType) {
            case "100" -> {
                preProcess.preProcess100(state, record);
                process100(record);
                postProcess.postProcess100(state);
            }
            case "200" -> {
                preProcess.preProcess200(state, record);
                process200(record);
                postProcess.postProcess200(state);
            }
            case "300" -> {
                preProcess.preProcess300(state, record);
                IntervalDataRecord intervalDataRecord = process300(record);
                postProcess.postProcess300(state, intervalDataRecord);
            }
            case "400" -> {
                preProcess.preProcess400(state, record);
                process400(record);
                postProcess.postProcess400(state);
            }
            case "500" -> {
                preProcess.preProcess500(state, record);
                process500(record);
                postProcess.postProcess500(state);
            }
            case "900" -> {
                preProcess.preProcess900(state, record);
                process900(record);
                postProcess.postProcess900(state);
            }
        }

    }

    private void startProducer() {
        executorService.submit(() -> {
            try (BufferedReader reader = Files.newBufferedReader(Path.of(inputFile));
                 CSVParser parser = new CSVParser(reader, format)) {
                NoopPrePostProcess noop = new NoopPrePostProcess();
                for (CSVRecord record : parser) {
                    process(record, noop, this);
                }
            } catch (FileNotFoundException e) {
                throw new NEM12Exception("Input file not found", e);
            } catch (IOException e) {
                throw new NEM12Exception("IO exception when validating input file", e);
            } finally {
                try {
                    queue.put(poisonPill);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                latch.countDown();
            }
        });
    }

    private void startConsumers() {
        Runnable consumerTask = () -> {
            try {
                while (true) {
                    NEM12ProcessorOutput.OutputEntry outputEntry = queue.take();
                    if (outputEntry == poisonPill) {
                        latch.countDown();
                        queue.put(poisonPill); // so other thread will also listen
                        logger.info("Consumer shutting down");
                        break;
                    }
                    processorOutput.write(outputEntry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                latch.countDown();
            }
        };

        for (int i = 0; i < config.threads() - 1; i++) { // minus one to exclude producer thread
            executorService.submit(consumerTask);
        }
    }


    NEM12ProcessorOutput.OutputEntry poisonPill = new NEM12ProcessorOutput.OutputEntry(
            "-",
            "-",
            LocalDate.ofEpochDay(0),
            -1,
            new double[]{}
    );

    void validateInputFile() throws NEM12Exception {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(inputFile));
             CSVParser parser = new CSVParser(reader, format);
        ) {
            NEM12Validator validator = new NEM12Validator();
            NoopPrePostProcess noop = new NoopPrePostProcess();
            for (CSVRecord record : parser) {
                process(record, validator, noop);
            }
        } catch (FileNotFoundException e) {
            throw new NEM12Exception("Input file not found", e);
        } catch (IOException e) {
            throw new NEM12Exception("IO exception when validating input file", e);
        }
    }

    @Override
    public void postProcess100(NEM12State state) throws NEM12Exception {
        // not yet required
    }

    @Override
    public void postProcess200(NEM12State state) throws NEM12Exception {
        // not yet required
    }

    @Override
    public void postProcess300(NEM12State state, IntervalDataRecord intervalDataRecord) throws NEM12Exception {
        // push to queue
        try {
            queue.put(new NEM12ProcessorOutput.OutputEntry(
                    state.dataDetailsRecord.nmi(),
                    state.dataDetailsRecord.registerId(),
                    intervalDataRecord.intervalDate(),
                    state.dataDetailsRecord.intervalLength(),
                    intervalDataRecord.intervalValues()
            ));
        } catch (InterruptedException e) {
            throw new NEM12Exception("Thread interrupted", e);
        }
    }

    @Override
    public void postProcess400(NEM12State state) throws NEM12Exception {
        // not yet required
    }

    @Override
    public void postProcess500(NEM12State state) throws NEM12Exception {
        // not yet required
    }

    @Override
    public void postProcess900(NEM12State state) throws NEM12Exception {
        // not yet required
    }


    public void start() {
        try (FileInputStream fis = new FileInputStream(this.inputFile);
             FileChannel channel = fis.getChannel();
             FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {

            logger.debug("Latch count {}", latch.getCount());
            // first pass
            logger.info("Validating input file...");
            validateInputFile();
            logger.info("Input file validated. Processing...");
            // second pass, actual processing
            startProducer();
            startConsumers();
            logger.debug("Latch count {}", latch.getCount());
            latch.await();

        } catch (InterruptedException e) {
            logger.error("Thread interrupted", e);
        } catch (IOException e) {
            logger.error("IO error", e);
        } catch (NEM12Exception e) {
            logger.error("Error processing file", e);
        } finally {
            executorService.shutdown();
        }
    }
}
