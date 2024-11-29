package com.backtest.strategy;

import java.time.LocalDate;

public class TradeSignal {
    private String type; // BUY or SELL
    private LocalDate date; // Date of the trade signal
    private double price; // Price at which the signal was generated

    public TradeSignal(String type, LocalDate date, double price) {
        this.type = type;
        this.date = date;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return type + " on " + date + " at price " + price;
    }
}
