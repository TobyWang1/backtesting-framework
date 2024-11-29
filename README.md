# Backtesting Framework

## Main Objectives
1. Calling API to retrieve the historical market data.
2. Storing the fetched market data into database.
3. Querying the data based on user input.
4. Developing trading strategies and letting user to customize the strategies.
5. Executing the trading strategy to get the testing results.

## Main Features
1. Data Retrieval: Using the third party API (Polygon) to retrieve the market data with the input ticker.
2. Trading Strategy: A strategy factory that stores all the trading strategies.
3. Testing Engine: Execute a user selected trading strategy with other user specified params like observation window, risk factors etc.
4. Result Summary: A summary to give user a better insight of how the strategy could have performed using the historical data.

## Configuration
1. API Key: The project requires Polygon API key to retrieve market data. Create the environment variable in a new `application.properties` file in the `src/main/resources` and store the API key there.
2. Database Connection: Any database parameter e.g. `password` also needs to be added in the `application.properties` file. In this example, H2 database has been used for demonstration.

## Roadmap
The project is still in development, below are some key features to do next:
1. Build a metric system to measure the performance of each backtest in order to get user a better summary and more insights.
2. Add more trading strategies. Currently there are only two basic strategies `Simple Moving Average` and `Exponential Moving Average` are available.
3. Write tests, e.g, unit tests, function tests etc.
4. Build a user interface. Currently you can only interact with the application in CLI, would be ideal to build a frontend part for it.
