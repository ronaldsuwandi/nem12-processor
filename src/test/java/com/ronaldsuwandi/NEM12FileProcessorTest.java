package com.ronaldsuwandi;

import com.ronaldsuwandi.config.NEM12Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.SQLException;

class NEM12FileProcessorTest {

    @Test
    void process() {
//        URL resourceUrl = getClass().getClassLoader().getResource("large_nem12_file.csv");
        URL resourceUrl = getClass().getClassLoader().getResource("nem12-example.csv");


        NEM12Config config = new NEM12Config(
                10,
                "jdbc:postgresql://localhost/postgres",
                "postgres",
                "postgres",
                20
        );
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl(config.dbUri());
        dbConfig.setUsername(config.dbUser());
        dbConfig.setPassword(config.dbPassword());
        dbConfig.setMaximumPoolSize(config.dbPoolSize());

        try (HikariDataSource ds = new HikariDataSource(dbConfig)) {
            NEM12ProcessorOutput output = new NEM12PostgresOutput(ds);

            NEM12FileProcessor processor = new NEM12FileProcessor(config, resourceUrl.getPath(), output);
            processor.start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void isValid() {
//        URL resourceUrl = getClass().getClassLoader().getResource("nem12-invalid-missing-footer.csv");
//        NEM12FileProcessor processor = new NEM12FileProcessor();
//        boolean result = processor.isValid(resourceUrl.getFile());
//        Assertions.assertTrue(result);
    }

    @Test
    void process300() {
//        String input200 = "200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610";
//
//        NEM12FileProcessor processor = new NEM12FileProcessor();
//        processor.process200(input200.split(","));
//
//        String input = "300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0.848,1.271,0.895,1.327,1.013,1.793,0.988,0.985,0.876,0.555,0.760,0.938,0.566,0.512,0.970,0.760,0.731,0.615,0.886,0.531,0.774,0.712,0.598,0.670,0.587,0.657,0.345,0.231,A,,,20050310121004,20050310182204";
//        processor.process300(input.split(","));
    }

}
