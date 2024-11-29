package com.backtest.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

public class DataRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DataRepository.class);
    // SQL insert query for historical data
    private static final String INSERT_SQL = "INSERT INTO historical_price_data (symbol, trade_date, open, high, low, close, volume) " +
                                                "VALUES (?, ?, ?, ?, ?, ?, ?);";

    private DataRepository() {
    }

    /**
     * Build the database connection and insert the data into table following the schema
     * @param symbol
     * @param historicalPriceData
     */
    public static void insertHistoricalPriceData(String symbol, List<StockData> historicalPriceData) {
        // Try-with-resources to establish connection and close resources automatically
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {

            conn.setAutoCommit(false); // Enable transaction management

            pstmt.setString(1, symbol);
            // For each retrieved StockData object, bind it to the insert query parameters.
            for (StockData data: historicalPriceData) {
                pstmt.setDate(2, java.sql.Date.valueOf(data.getTradeDate()));
                pstmt.setDouble(3, data.getOpen());
                pstmt.setDouble(4, data.getHigh());
                pstmt.setDouble(5, data.getLow());
                pstmt.setDouble(6, data.getClose());
                pstmt.setLong(7, data.getVolume());

                pstmt.addBatch(); // Batch multiple inserts to improve performance
            }
            pstmt.executeBatch(); // Insert all records in a batch
            conn.commit();

            LOG.info("Data successfully inserted into 'historical_price_data' table for {}.", symbol);
        } catch (SQLException e) {
            LOG.error("Failed to insert historical data.", e);
        }
    }

    // Fetch available date range for a given symbol
    public static Date[] getAvailableDateRange(String symbol) {
        Date[] dateRange = new Date[2];
        String query = "SELECT MIN(trade_date) AS min_date, MAX(trade_date) AS max_date FROM historical_price_data WHERE symbol = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                dateRange[0] = rs.getDate("min_date");
                dateRange[1] = rs.getDate("max_date");
            }
        } catch (SQLException e) {
            LOG.error("Failed to fetch available date range.", e);
        }
        return dateRange;
    }

    // Query some data from database to test if it's working
    public static List<StockData> retrieveHistoricalPriceData(String symbol, Date startDate, Date endDate) {
        List<StockData> historicalData = new ArrayList<>();
        String query = "SELECT symbol, trade_date, open, high, low, close, volume FROM historical_price_data WHERE symbol = ? AND trade_date BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, symbol);
            pstmt.setDate(2, startDate);
            pstmt.setDate(3, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Retrieve trade_date as java.sql.Date
                Date tradeDate = rs.getDate("trade_date");
                // Convert java.sql.Date to long (milliseconds since epoch)
                long tradeTimestamp = tradeDate.getTime();

                StockData data = new StockData(
                        rs.getString("symbol"),
                        tradeTimestamp,
                        rs.getDouble("open"),
                        rs.getDouble("high"),
                        rs.getDouble("low"),
                        rs.getDouble("close"),
                        rs.getLong("volume")
                );
                historicalData.add(data);
            }
        } catch (SQLException e) {
            LOG.error("Failed to retrieve historical data.", e);
        }
        return historicalData;
    }
}
