package com.backtest.db;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class StockData {
    private String symbol;
    private LocalDate tradeDate;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    // Constructor
    public StockData(String symbol, long tradeTimestamp, double open, double high, double low, double close, long volume) {
        this.symbol = symbol;
        this.tradeDate = convertTimestampToDate(tradeTimestamp);
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    // Convert timestamp in milliseconds to LocalDate
    private LocalDate convertTimestampToDate(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Getter methods
    public String getSymbol() {return symbol;}
    public LocalDate getTradeDate() {return tradeDate;}
    public double getOpen() {return open;}
    public double getHigh() {return high;}
    public double getLow() {return low;}
    public double getClose() {return close;}
    public long getVolume() {return volume;}
}
