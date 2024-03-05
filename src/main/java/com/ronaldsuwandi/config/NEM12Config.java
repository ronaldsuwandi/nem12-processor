package com.ronaldsuwandi.config;

import java.util.Objects;

public record NEM12Config(
        int consumerThreads,
        String dbUri,
        String dbUser,
        String dbPassword,
        int dbPoolSize
) {
    public NEM12Config {
        if (consumerThreads <= 0) {
            throw new RuntimeException("Invalid config. threads must be positive number");
        }
        if (dbPoolSize <= 0) {
            throw new RuntimeException("Invalid config. dbPoolSize must be positive number");
        }
        Objects.requireNonNull(dbUri);
        Objects.requireNonNull(dbUser);
        Objects.requireNonNull(dbPassword);
    }
}
