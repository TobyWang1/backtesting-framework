package com.backtest.strategy;

import com.backtest.db.StockData;

import java.util.ArrayList;
import java.util.List;

public class SimpleMovingAverageStrategy implements Strategy {
    private double stopLossPercent = STOP_LOSS_PERCENT;
    private double takeProfitPercent = TAKE_PROFIT_PERCENT;
    private int observationWindow = OBSERVATION_WINDOW;

    @Override
    public void setStopLossPercent(double percent) {
        this.stopLossPercent = percent;
    }

    @Override
    public void setTakeProfitPercent(double percent) {
        this.takeProfitPercent = percent;
    }

    @Override
    public void setObservationWindow(int days) {
        this.observationWindow = days;
    }

    /**
     * Simulate simple buy/sell trades using the SMA strategy
     * @param marketData A list of StockData objects retrieved from database.
     * @return Returns a list of all generated trade signals.
     */
    @Override
    public List<TradeSignal> simulateTrades(List<StockData> marketData) {
        if (marketData == null || marketData.size() < observationWindow) {
            throw new IllegalArgumentException("Insufficient market data");
        }

        List<Double> movingAverages = calculateMovingAverage(marketData);
        List<TradeSignal> tradeSignals = new ArrayList<>();
        boolean isPositionOpen = false; // Tracks if we own stock
        double entryPrice = 0.0;

        for (int i = observationWindow; i < marketData.size(); i++) {
            StockData currentDay = marketData.get(i - 1);
            double closingPrice = currentDay.getClose();
            double sma = movingAverages.get(i - observationWindow);

            // Calculate stop-loss and take-profit levels
            double stopLoss = isPositionOpen ? entryPrice * (1 - stopLossPercent) : 0;
            double takeProfit = isPositionOpen ? entryPrice * (1 + takeProfitPercent) : 0;

            // Trading logic based on SMA
            if (!isPositionOpen && closingPrice < sma) {
                // Enter position
                tradeSignals.add(new TradeSignal("BUY", currentDay.getTradeDate(), closingPrice));
                isPositionOpen = true;
                entryPrice = closingPrice;
            } else if (isPositionOpen && (closingPrice > sma || closingPrice <= stopLoss || closingPrice >= takeProfit)) {
                // Exit position on SELL signal, stop-loss, or take-profit
                tradeSignals.add(new TradeSignal("SELL", currentDay.getTradeDate(), closingPrice));
                isPositionOpen = false;
                entryPrice = 0.0;
            }
        }

        return tradeSignals;
    }
    /**
     * Helper method: Calculate the SMA for the market data.
     * @param marketData A list of StockData objects retrieved from database.
     * @return Calculated Simple Moving Averages values as a list.
     */
    private List<Double> calculateMovingAverage(List<StockData> marketData) {
        List<Double> movingAverages = new ArrayList<>();

        // Add initial placeholders for incomplete windows
        for (int i = 0; i < observationWindow - 1; i++) {
            movingAverages.add(0.0);
        }

        // Calculate SMA for complete windows
        for (int i = observationWindow - 1; i < marketData.size(); i++) {
            double sum = 0.0;
            for (int j = i - observationWindow + 1; j <= i; j++) {
                sum += marketData.get(j).getClose();
            }
            movingAverages.add(sum / observationWindow);
        }

        return movingAverages;
    }

}
