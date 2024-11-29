package com.backtest.engine;

import com.backtest.db.StockData;
import com.backtest.strategy.Strategy;
import com.backtest.strategy.TradeSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExecutionEngine {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionEngine.class);
    private static final double TRADE_RISK_PERCENT = 0.2; // Risk 20% of the portfolio on each trade.

    private double cashBalance;
    private double sharesOwned;
    private final double initialCashBalance;

    /**
     * Constructor
     * @param initialCashBalance
     */
    public ExecutionEngine(double initialCashBalance) {
        this.initialCashBalance = initialCashBalance;
        this.cashBalance = initialCashBalance;
        this.sharesOwned = 0;
    }

    public void runBackTest(Strategy strategy, List<StockData> marketData) {
        List<TradeSignal> signals = strategy.simulateTrades(marketData);
        double finalPrice = 0;

        for (TradeSignal signal : signals) {
            if ("BUY".equals(signal.getType())) {
               finalPrice = executeBuy(signal);
            } else if ("SELL".equals(signal.getType())) {
                finalPrice = executeSell(signal);
            }
        }
        summarizeResults(finalPrice);
    }

    /**
     * Process a BUY signal
     * @param signal a list of TradeSignal objects
     */
    private double executeBuy(TradeSignal signal) {
        double tradeRiskAmount = TRADE_RISK_PERCENT * initialCashBalance;
        double sharesToBuy = (tradeRiskAmount / signal.getPrice());
        if (sharesToBuy > 0) {
            cashBalance -= sharesToBuy * signal.getPrice();
            sharesOwned += sharesToBuy;
            String msg = "Executed BUY: " + sharesToBuy + " shares at " + signal.getPrice() + " on " + signal.getDate();
            LOG.info(msg);
        }
        return signal.getPrice();
    }

    /**
     * Process a SELL signal
     * @param signal a list of TradeSignal objects
     */
    private double executeSell(TradeSignal signal) {
        if (sharesOwned > 0) {
            cashBalance += sharesOwned * signal.getPrice();
            String msg = "Executed SELL: " + sharesOwned + " shares at " + signal.getPrice() + " on " + signal.getDate();
            LOG.info(msg);
            sharesOwned = 0;
        }
        return signal.getPrice();
    }

    // Simple summary from trading results
    private void summarizeResults(double finalPrice) {
        LOG.info("\n================== Portfolio Summary ==================");
        LOG.info("Final Cash Balance: {}", cashBalance);
        String sharesOwnedMsg = String.format("%.3f", sharesOwned);
        LOG.info("Shares Owned: {}", sharesOwnedMsg);
        LOG.info("Last execution price: {}", finalPrice);
        double netProfit = cashBalance - initialCashBalance + finalPrice * sharesOwned;
        String netProfitMsg = String.format("%.3f", netProfit);
        double netProfitPercentage = (netProfit / initialCashBalance) * 100;
        String netProfitPercentageMsg = String.format("%.3f", netProfitPercentage);
        LOG.info("Net Profit: {}, in percent {}%", netProfitMsg, netProfitPercentageMsg);
    }
}
