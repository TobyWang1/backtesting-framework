package com.backtest;

import com.backtest.api.PriceDataRetriever;
import com.backtest.db.DataRepository;
import com.backtest.db.DatabaseConnector;
import com.backtest.db.StockData;
import com.backtest.engine.ExecutionEngine;
import com.backtest.strategy.Strategy;
import com.backtest.strategy.StrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class BacktestApp {
    private static final Logger LOG = LoggerFactory.getLogger(BacktestApp.class);
    private static final String DEFAULT_TICKER = "AAPL";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String ticker = promptForTicker(scanner);
        initializeDataBase();

        List<StockData> historicalPriceData = fetchData(ticker);
        if (historicalPriceData.isEmpty()) return;
        insertHistoricalPriceData(ticker, historicalPriceData);

        Date[] availableDateRange = getAvailableDateRange(ticker);
        Date[] selectedDateRange = promptForDateRange(scanner, availableDateRange);

        List<StockData> queriedData = DataRepository.retrieveHistoricalPriceData(ticker, selectedDateRange[0], selectedDateRange[1]);
        if (queriedData.isEmpty()) return;

        Strategy strategy = selectStrategy(scanner);

        double initialCashBalance = promptForInitialCashBalance(scanner);
        executeBacktest(strategy, queriedData, initialCashBalance);
    }


    private static void initializeDataBase() {
        try {
            DatabaseConnector.createHistoricalDataTable();
        } catch (Exception e) {
            LOG.error("Could not setup the database", e);
        }
    }

    /**
     * Prompt message for uer to enter a company's ticker
     * @param scanner
     * @return the entered ticker name or default ticker AAPL
     */
    private static String promptForTicker(Scanner scanner) {
        LOG.info("Enter the ticker symbol (default is set to AAPL): ");
        String ticker = scanner.nextLine().toUpperCase().trim();
        return ticker.isEmpty() ? DEFAULT_TICKER : ticker;
    }

    /**
     * Fetch historical price data using Polygon API and store the data into database
     * @param ticker
     * @return A list of StockData objects
     */
    private static List<StockData> fetchData(String ticker) {
        PriceDataRetriever priceDataRetriever = new PriceDataRetriever();
        try {
            List<StockData> historicalPriceData = priceDataRetriever.retrievePriceData(ticker);
            if (historicalPriceData.isEmpty()) {
                LOG.error("Invalid ticker symbol: {}. no data retrieved from the API.", ticker);
            } else {
                LOG.info("Successfully retrieved {} price records for {} ", historicalPriceData.size(), ticker);
            }
            return historicalPriceData;
        } catch (Exception e) {
            LOG.error("Error during price data retrieval", e);
            return List.of();
        }
    }

    /**
     * Insert the fetched historical price data into database.
     * @param ticker
     * @param historicalPriceData
     */
    private static void insertHistoricalPriceData(String ticker, List<StockData>historicalPriceData) {
        DataRepository.insertHistoricalPriceData(ticker, historicalPriceData);
    }

    /**
     * Get the available date range from database
     * @param ticker
     * @return the available date range in a list of Date objects .e.g, 2024-01-20 to 2024-05-02
     */
    private static Date[] getAvailableDateRange(String ticker) {
        Date[] availableDateRange = DataRepository.getAvailableDateRange(ticker);
        LOG.info("Available dates for {} are {} to {}", ticker, availableDateRange[0], availableDateRange[1]);
        return availableDateRange;
    }

    /**
     * Prompt for user to enter the date range
     * @param scanner
     * @param availableDateRange
     * @return the date range in a list of Date objects that user has entered or default date range if no input from user
     */
    private static Date[] promptForDateRange(Scanner scanner, Date[] availableDateRange) {
        LOG.info("Select a time period to test the strategy (default is set to 2024-01-02 to 2024-11-20, format: YYYY-MM-DD to YYYY-MM-DD): ");

        // Read input from the scanner
        String inputLine = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        Date defaultStartDate = Date.valueOf("2024-01-02");
        Date defaultEndDate = Date.valueOf("2024-11-20");

        // Check for user input
        if (!inputLine.isEmpty()) {
            String[] dateInput = inputLine.split("\\s+to\\s+"); // Split only if input is not empty

            if (dateInput.length != 2) {
                LOG.error("Invalid data range format");
                return new Date[0];
            }

            Date startDate = Date.valueOf(dateInput[0]);
            Date endDate = Date.valueOf(dateInput[1]);

            if (startDate.before(availableDateRange[0]) || endDate.after(availableDateRange[1])) {
                LOG.error("Selected date range is out of available dates");
                return new Date[0];
            }

            return new Date[]{startDate, endDate};
        }

        return new Date[]{defaultStartDate, defaultEndDate};
    }

    /**
     * Provide available strategies for user to select with customized params
     * @param scanner
     * @return the selected strategy by user, if not input, the default strategy is SMA.
     */
    private static Strategy selectStrategy(Scanner scanner) {
        LOG.info("Available Strategies: ");
        List<String> strategies = StrategyFactory.getAvailableStrategies();
        strategies.forEach(strategy -> LOG.info("{}", strategy));

        // Choose strategy
        LOG.info("Enter the strategy to use (default is SMA: Simple Moving Average): ");
        String strategyName = scanner.nextLine().trim(); // Read input and trim whitespace

        if (strategyName.isEmpty()) {
            strategyName = "sma";
        }

        // Choose observation window
        LOG.info("Enter the observation window (default is set to 10 days, the value must be at least 5 days: ");
        String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        int observationWindow = Strategy.OBSERVATION_WINDOW;

        if (!input.isEmpty()) {
            try {
                observationWindow = Integer.parseInt(input);
                if (observationWindow < 5) {
                    LOG.error("Invalid observation window, it must be at least 5. 10 has been selected as default.");
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid input, it should be a numeric value. 10 has been selected as moving average window.");
            }
        }

        // Choose stop loss percentage
        LOG.info("Enter the stop loss percentage (default is set to 0.10, please enter numeric value in range between 0.00 and 1.00): ");
        String stopLossInput = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        double stopLossPercent = Strategy.STOP_LOSS_PERCENT;

        if (!stopLossInput.isEmpty()) {
            try {
                stopLossPercent = Double.parseDouble(stopLossInput);
            } catch (NumberFormatException e) {
                LOG.error("Invalid stop loss input, must be a numeric value. Default 0.10 has been selected.");
            }
        }

        // Choose take profit percentage
        LOG.info("Enter the take profit percentage (default is set to 0.20, please enter numeric value in range between 0.00 and 1.00): ");
        String takeProfitInput = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        double takeProfitPercent = Strategy.TAKE_PROFIT_PERCENT;

        if (!takeProfitInput.isEmpty()) {
            try {
                takeProfitPercent = Double.parseDouble(takeProfitInput);
            } catch (NumberFormatException e) {
                LOG.error("Invalid take profit input, must be a numeric value. Default 0.20 has been selected.");
            }
        }

        // Create strategy with customized params
        LOG.info("Stop loss percent: {}%; Take profit percent: {}%", stopLossPercent * 100, takeProfitPercent * 100);
        return StrategyFactory.getStrategy(strategyName, observationWindow, stopLossPercent, takeProfitPercent);
    }

    /**
     * Prompt for user to enter initial cash balance for backtest
     * @param scanner
     * @return the initial cash balance as double, if no input, the default amount is 10000
     */
    private static double promptForInitialCashBalance(Scanner scanner) {
        LOG.info("Enter the initial cash balance for test (default is 10000): ");
        try {
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            return input.isEmpty() ? 10000 : Double.parseDouble(input);
        } catch (NumberFormatException e) {
            LOG.error("Invalid input, it should be a numeric value. 10000 has been selected as initial cash balance");
            return 10000;
        }
    }

    /**
     * The function to execute backtest
     * @param strategy selected strategy name
     * @param queriedData queried historical price data for the selected company
     * @param initialCashBalance the initial cash balance to run the backtest
     */
    private static void executeBacktest(Strategy strategy, List<StockData> queriedData, double initialCashBalance) {
        ExecutionEngine engine = new ExecutionEngine(initialCashBalance);
        engine.runBackTest(strategy, queriedData);
    }

}
