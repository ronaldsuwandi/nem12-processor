package com.ronaldsuwandi;

import com.ronaldsuwandi.record.NMIDataDetailsRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NEM12FileProcessor {
    NEM12ProcessorOutput processorOutput;

    public NEM12FileProcessor(NEM12ProcessorOutput processorOutput) {
        this.processorOutput = processorOutput;
    }

    final int MinutesInDay = 24 * 60;
    class NEM12State {
        boolean has100;
        boolean has900;

        NMIDataDetailsRecord dataDetailsRecord;

    }

    private NEM12State state = new NEM12State();
    public boolean isValid(String path) {
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            var firstLine = file.readLine();
            var validHeader = firstLine != null && firstLine.trim().startsWith("100");
            int footerBuffer = 1024; // read last 1kb for additional buffer
            byte[] footerByteBuffer = new byte[footerBuffer];
            long seek = 0;
            if (file.length() > footerBuffer) {
                seek = file.length() - footerBuffer;
            }
            file.seek(seek);
            file.read(footerByteBuffer, 0, footerBuffer);
            String footer = new String(footerByteBuffer, StandardCharsets.UTF_8);
            var validFooter = footer.trim().endsWith("900");
            return validHeader && validFooter;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    boolean isValidState(String recordType) {
        return switch (recordType) {
            case "100" -> !state.has100 && !state.has900;
            case "200", "900" -> state.has100 && !state.has900;
            case "300", "400", "500" -> state.has100 && !state.has900 && state.dataDetailsRecord != null;
            default -> throw new IllegalStateException("Unexpected value: " + recordType);
        };
    }
    void process100(String... args) {
        state.has100 = true;
    }

    void process200(String... args) {
//        System.out.println(Arrays.toString(args));

        // FIXME better array length handling
        LocalDate nextScheduledReadDate = null;
        if (args.length == 10) {
            nextScheduledReadDate = LocalDate.parse(args[9], dateFormatter);
        }
        // validate args length

        state.dataDetailsRecord = new NMIDataDetailsRecord(
                args[1], args[2], args[3], args[4], args[5], args[6], args[7], Integer.parseInt(args[8]), nextScheduledReadDate
        );
    }

    void process300(String... args) {
//        System.out.println(Arrays.toString(args));
        // validate args length
        int intervalRecordLength = MinutesInDay / state.dataDetailsRecord.intervalLength();
        double[] intervalRecords = new double[intervalRecordLength];
        // FIXME validate length
        for (int i=0;i<intervalRecordLength;i++) {
            intervalRecords[i] = Double.parseDouble(args[i + 2]);
        }

        // records = 3
        // 0 , 1date, 2r, 3r, 4r, 5


        // FIXME tidy up for optional value
        String reasonCode = null;
        if (args.length == (2 + intervalRecordLength + 1 + 1)) {
            reasonCode = args[2 + intervalRecordLength + 1];
        }
        String reasonDescription = null;
        if (args.length == (2 + intervalRecordLength + 2 + 1)) {
            reasonDescription = args[2 + intervalRecordLength + 2];
        }
        LocalDateTime updateDateTime = null;
        if (args.length == (2 + intervalRecordLength + 3 + 1)) {
            updateDateTime = LocalDateTime.parse(args[2 + intervalRecordLength + 3], dateTimeFormatter);
        }
        LocalDateTime msatsLoadDateTime = null;
        if (args.length == (2 + intervalRecordLength + 4 + 1)) {
            msatsLoadDateTime = LocalDateTime.parse(args[2 + intervalRecordLength + 4], dateTimeFormatter);
        }


//        IntervalDataRecord record = new IntervalDataRecord(
//                LocalDate.parse(args[1], dateFormatter),
//                intervalRecords,
//                args[2 + intervalRecordLength],
//                reasonCode,
//                reasonDescription,
//                updateDateTime,
//                msatsLoadDateTime
//        );

        LocalDate intervalDate = LocalDate.parse(args[1], dateFormatter);
        processorOutput.write(state.dataDetailsRecord.nmi(), intervalDate, state.dataDetailsRecord.intervalLength(), intervalRecords);
        // TODO do we need to generate new record? can we reuse for memory optimisation
//        System.out.println(record);
    }

    void process400(String... args) {
    }

    void process500(String... args) {
    }

    void process900(String... args) {
        state.has900 = true;
        System.out.println("DONE");
    }

    public void process(String path) throws NEM12Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            // do validation first so it's a 2 pass thing
            // 1st pass to confirm file is all good
            // 2nd pass is for the actual processing and push to output to db
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] split = line.split(",");


                if (!isValidState(split[0])) {
                    // invalid state
                    throw new NEM12Exception();
                }
                switch (split[0]) {
                    case "100" -> process100(split);
                    case "200" -> process200(split);
                    case "300" -> process300(split);
                    case "400" -> process400(split);
                    case "500" -> process500(split);
                    case "900" -> process900(split);

                    default -> throw new NEM12Exception();
                }
                // extract
                // only 1 header
                // when encounter 200
                //  - if next is 300, process300(200details)
                // - if next is 200, repeat
                // only 1 footer
                // Process each line as needed
                // output type sql
//                System.out.println(line); // Example: simply print each line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
