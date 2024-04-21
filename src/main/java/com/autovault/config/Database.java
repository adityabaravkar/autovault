package com.autovault.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface Database {

    Logger logger = LogManager.getLogger(Database.class);

    default Connection connection() throws SQLException {
        Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:autovault.db");
            // logger.info("Database connection established.");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw e;
        }
        return connection;
    }
}
