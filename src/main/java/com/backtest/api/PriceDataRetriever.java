package com.backtest.api;

import com.backtest.api.service.PriceDataFetchService;

import java.util.List;

import com.backtest.api.service.PriceDataRetrievalException;
import com.backtest.db.StockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangzhicheng
 */
public class PriceDataRetriever {
    private static final Logger LOG = LoggerFactory.getLogger(PriceDataRetriever.class);

    public List<StockData> retrievePriceData(String ticker) throws PriceDataRetrievalException{
        LOG.info("Retrieving price data for ticker: {}", ticker);
        try {
            PriceDataFetchService priceDataFetchService = new PriceDataFetchService();
            return priceDataFetchService.fetchPriceData(ticker);
        } catch (Exception e) {
            LOG.error("Unexpected error during data retrieval", e);
            throw new PriceDataRetrievalException("Failed to retrieve price data for ticker " + ticker, e);
        }
    }

    public static void main(String[] args) {
        LOG.info("PriceDataRetriever starting");
        if (args.length == 0) {
            LOG.warn("Please provide a company's ticker name e.g., AAPL for Apple as first argument");
        }
    }
}
