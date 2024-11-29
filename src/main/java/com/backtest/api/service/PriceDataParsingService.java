package com.backtest.api.service;

import com.backtest.db.StockData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PriceDataParsingService {
    private static final Logger LOG = LoggerFactory.getLogger(PriceDataParsingService.class);

    /**
     * Parses the price data from the API response and converts it into a list of StockData objects.
     *
     * @param responseBody The JSON response string from the API.
     * @return A list of StockData objects.
     * @throws JSONException If the JSON cannot be properly parsed.
     */
    public List<StockData> parsePriceDataResponse(String responseBody) throws JSONException {

        // Parse responseBody into a JSON object
        JSONObject json = new JSONObject(responseBody);

        List<StockData> stockDataList = new ArrayList<>();

        // Extract the symbol from the "ticker" section
        String symbol = null;
        if (json.has("ticker")) {
            symbol = json.getString("ticker");

            // Verify that the symbol was found
            if (symbol == null || symbol.isEmpty()) {
                throw new JSONException("Symbol not found in the 'Meta Data' section of the response.");
            }

            // Check if the JSON contains the "results" key
            if (json.has("results")) {
                JSONArray results = json.getJSONArray("results");

                // Loop through each data
                for (int i = 0; i < results.length(); i++) {
                    JSONObject dayData = results.getJSONObject(i);

                    long tradeTimeStamp = dayData.getLong("t");
                    double openPrice = dayData.getDouble("o");
                    double highPrice = dayData.getDouble("h");
                    double lowPrice = dayData.getDouble("l");
                    double closePrice = dayData.getDouble("c");
                    long volume = dayData.getLong("v");

                    StockData stockData = new StockData(symbol, tradeTimeStamp, openPrice, highPrice, lowPrice, closePrice, volume);
                    stockDataList.add(stockData);
                }
            } else {
                LOG.info("No 'Time Series (Daily)' data found in the response");
            }
        }

        // Sort by trade date before logging or returning results
        stockDataList.sort(Comparator.comparing(StockData::getTradeDate));

        // Log the sorted price data
        LOG.info("Ticker\tDate\tOpen\tHigh\tLow\tClose\tVolume");
        LOG.info("------------------------------------------------");
        for (StockData stockData : stockDataList) {
            String priceLog = String.format("%s\t%s\t%.2f\t%.2f\t%.2f\t%.2f\t%d",
                    stockData.getSymbol(),
                    stockData.getTradeDate(),
                    stockData.getOpen(),
                    stockData.getHigh(),
                    stockData.getLow(),
                    stockData.getClose(),
                    stockData.getVolume());
            LOG.info(priceLog);
        }

        return stockDataList;
    }
}
