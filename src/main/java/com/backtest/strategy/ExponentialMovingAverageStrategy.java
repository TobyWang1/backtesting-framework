package com.backtest.strategy;

import com.backtest.db.StockData;

import java.util.ArrayList;
import java.util.List;

public class ExponentialMovingAverageStrategy implements Strategy {
    private double stopLossPercent = STOP_LOSS_PERCENT;
    private double takeProfitPercent = TAKE_PROFIT_PERCENT;
    private int observationWindow = OBSERVATION_WINDOW;
    private double multiplier = 2.0 / (observationWindow + 1); // Calculate multiplier for EMA

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
        // Update multiplier when the observation window is set
        this.multiplier = 2.0 / (days + 1);
    }

    /**
     * Simulate buy/sell trades using the EMA strategy
     * @param marketData A list of StockData objects retrieved from database.
     * @return Returns a list of all generated trade signals.
     */
    @Override
    public List<TradeSignal> simulateTrades(List<StockData> marketData) {
        if (marketData == null || marketData.size() < observationWindow) {
            throw new IllegalArgumentException("Insufficient market data");
        }

        List<Double> exponentialMovingAverages = calculateExponentialMovingAverage(marketData);
        List<TradeSignal> tradeSignals = new ArrayList<>();
        boolean isPositionOpen = false;
        double entryPrice = 0.0;

        for (int i = observationWindow; i < marketData.size(); i++) {
            StockData currentDay = marketData.get(i - 1);
            double closingPrice = currentDay.getClose();
            double ema = exponentialMovingAverages.get(i - observationWindow);

            // Calculate stop-loss and take-profit levels
            double stopLoss = isPositionOpen ? entryPrice * (1 - stopLossPercent) : 0;
            double takeProfit = isPositionOpen ? entryPrice * (1 + takeProfitPercent) : 0;

            // Trading logic based on EMA
            if (!isPositionOpen && closingPrice < ema) {
                // Enter position
                tradeSignals.add(new TradeSignal("BUY", currentDay.getTradeDate(), closingPrice));
                isPositionOpen = true;
                entryPrice = closingPrice;
            } else if (isPositionOpen && (closingPrice > ema || closingPrice <= stopLoss || closingPrice >= takeProfit)) {
                // Exit position on SELL signal, stop-loss, or take-profit
                tradeSignals.add(new TradeSignal("SELL", currentDay.getTradeDate(), closingPrice));
                isPositionOpen = false;
                entryPrice = 0.0;
            }
        }

        return tradeSignals;
    }

    /**
     * Helper method: Calculate the EMA for the market data.
     * @param marketData A list of StockData objects retrieved from database.
     * @return Calculated Exponential Moving Average values as a list.
     */
    private List<Double> calculateExponentialMovingAverage(List<StockData> marketData) {
        List<Double> emas = new ArrayList<>();
        double ema = 0.0;

        // Calculate the first EMA value (SMA over the first 'observationWindow' days)
        for (int i = 0; i < observationWindow; i++) {
            ema += marketData.get(i).getClose();
        }
        ema /= observationWindow;
        emas.add(ema); // Add the initial EMA value

        // Calculate EMA for the rest of the data
        for (int i = observationWindow; i < marketData.size(); i++) {
            StockData currentDay = marketData.get(i);
            ema = ((currentDay.getClose() - ema) * multiplier) + ema; // EMA formula
            emas.add(ema);
        }

        return emas;
    }
}