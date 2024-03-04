package com.ronaldsuwandi;

import com.ronaldsuwandi.config.ConfigLoader;
import com.ronaldsuwandi.config.NEM12Config;
import com.zaxxer.hikari.HikariConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

public class MainApp {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);


    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("Need to provide input file as an argument");
            System.exit(1);
        }
        String inputFile = args[0];

        URL configURL = ClassLoader.getSystemResource("config.properties");
        if (args.length == 2) {
            try {
                configURL = new URL(args[1]);
            } catch (MalformedURLException e) {
                logger.error("Invalid config path", e);
                System.exit(1);
            }
        }

        NEM12Config config = ConfigLoader.loadNEM12Config(configURL);
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl(config.dbUri());
        dbConfig.setUsername(config.dbUser());
        dbConfig.setPassword(config.dbPassword());
        dbConfig.setMaximumPoolSize(config.dbPoolSize());

        try {
            NEM12ProcessorOutput processorOutput = new NEM12PostgresOutput(dbConfig.getDataSource());
            NEM12FileProcessor processor = new NEM12FileProcessor(config, inputFile, processorOutput);

            long start = System.currentTimeMillis();
            processor.start();
            long elapsedTime = System.currentTimeMillis() - start;
            logger.info("Took {} ms to process", elapsedTime);

        } catch (SQLException e) {
            logger.error("Error setting up database connection", e);
            System.exit(1);
            throw new RuntimeException(e);
        }
    }
}
