package com.backtest.strategy;

import com.backtest.db.StockData;

import java.util.List;

public interface Strategy {
    double STOP_LOSS_PERCENT = 0.10;
    double TAKE_PROFIT_PERCENT = 0.20;
    int OBSERVATION_WINDOW = 10;

    List<TradeSignal> simulateTrades(List<StockData> marketData);

    void setStopLossPercent(double percent);
    void setTakeProfitPercent(double percent);
    void setObservationWindow(int days);
}
