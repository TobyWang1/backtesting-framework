package com.backtest.api.service;

import com.backtest.config.Config;
import com.backtest.db.StockData;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PriceDataFetchService {
    private static final String API_KEY = Config.getApiKey();
    private static final String BASE_URL = "https://api.polygon.io/v2/aggs/ticker/";
    private static final Logger LOG = LoggerFactory.getLogger(PriceDataFetchService.class);

    /**
     *  This method is fetching the price data
     * @param symbol A symbol represents a company's ticker
     * @throws IOException
     * @throws InterruptedException
     */
    public List<StockData> fetchPriceData(String symbol) throws IOException, InterruptedException {
        // Create url of ticker's time series data
        String url = BASE_URL + symbol + "/range/1/day/2024-01-01/2024-11-20?adjusted=true&sort=asc&limit=50000&apiKey=" + API_KEY;

        // Building the request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Sending the request and receiving a response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the request was successful
        if (response.statusCode() == 200) {
            LOG.info("Price data fetched successfully");
            try {
                //Parse the response body which is in JSON format
                PriceDataParsingService priceDataParsingService = new PriceDataParsingService();
                return priceDataParsingService.parsePriceDataResponse(response.body());
            } catch (JSONException e) {
                LOG.error("Failed to parse the response", e);
                throw new IOException("Error parsing data", e);
            }
        } else {
            LOG.error("Failed to fetch data. Status code: {}", response.statusCode());
            throw new IOException("Failed to fetch data. Status code: " + response.statusCode());
        }
    }
}
