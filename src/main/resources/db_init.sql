CREATE TABLE IF NOT EXISTS historical_price_data(
    ID INT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10),
    trade_date DATE,
    open DOUBLE NOT NULL,
    high DOUBLE NOT NULL,
    low DOUBLE NOT NULL,
    close DOUBLE NOT NULL,
    volume BIGINT NOT NULL
);