package com.backtest.db;

import com.backtest.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConnector.class);
    //JDBC connection string to H2. Choose in-memory or disk-based storage for practice purpose.
    private static final String JDBC_URL = "jdbc:h2:~/backtestdb";
    private static final String USER = "sa";
    private static final String PASSWORD = Config.getDatabasePassword();

    private DatabaseConnector() {
    }

    /**
     * Method to establish connection to H2 database
     * @return A suitable driver to connect the database
     * @throws SQLException
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void createHistoricalDataTable() throws SQLException {
        String createTableSQL = loadSqlFromFile("db_init.sql");
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS historical_price_data");
            stmt.execute(createTableSQL);
            LOG.info("Table 'historical_price_data' created or already exists");
        } catch (Exception e){
            LOG.error("Cannot create the table", e);
        }
    }

    /**
     * Load the SQL from a SQL file
     * @param fileName
     * @return the sql data in string
     */
    private static String loadSqlFromFile(String fileName) {
        StringBuilder sql = new StringBuilder();
        try (InputStream inputStream = DatabaseConnector.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            if (inputStream == null) {
                LOG.error("SQL file not found");
                return "";
            }
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
        } catch (IOException e) {
            LOG.error("Cannot load SQL from file", e);
        }
        return sql.toString();
    }
}

