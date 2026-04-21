package com.busapp.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StartupDbVerifier implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupDbVerifier.class);

    private final JdbcTemplate jdbcTemplate;

    public StartupDbVerifier(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        log.info("Database connectivity verified at startup: SELECT 1 -> {}", result);
    }
}
