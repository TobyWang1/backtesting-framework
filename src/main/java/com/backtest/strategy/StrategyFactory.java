package com.backtest.strategy;

import java.util.List;

public class StrategyFactory {

    /**
     * Method to get a specific strategy based on the params
     * @param strategyName
     * @param observationWindow
     * @param stopLossPercent
     * @param takeProfitPercent
     * @return A specific strategy class implemented from Strategy interface
     */
    public static Strategy getStrategy(String strategyName, int observationWindow, double stopLossPercent, double takeProfitPercent) {
        switch (strategyName.toUpperCase()) {
            case "SMA":
                SimpleMovingAverageStrategy strategy = new SimpleMovingAverageStrategy();
                strategy.setStopLossPercent(stopLossPercent);
                strategy.setTakeProfitPercent(takeProfitPercent);
                strategy.setObservationWindow(observationWindow);
                return strategy;
            case "EMA":
                ExponentialMovingAverageStrategy strategy1 = new ExponentialMovingAverageStrategy();
                strategy1.setStopLossPercent(stopLossPercent);
                strategy1.setTakeProfitPercent(takeProfitPercent);
                strategy1.setObservationWindow(observationWindow);
                return strategy1;

            default:
                throw new IllegalArgumentException("Unknown strategy '" + strategyName + "'");
        }
    }

    /**
     * Return the available strategy names dynamically.
     * @return a list of available strategies.
     */
    public static List<String> getAvailableStrategies() {
        return List.of("SMA", "EMA"); // Add more strategies here as they are developed.
    }

}
