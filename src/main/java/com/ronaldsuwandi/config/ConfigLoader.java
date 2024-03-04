package com.ronaldsuwandi.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.net.URL;

public class ConfigLoader {
    public static NEM12Config loadNEM12Config(URL url) {
        Configurations configs = new Configurations();
        try {
            Configuration config = configs.properties(url);
            return new NEM12Config(
                    config.getInt("threads"),
                    config.getString("database.uri"),
                    config.getString("database.user"),
                    config.getString("database.password"),
                    config.getInt("database.poolSize")
            );
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
