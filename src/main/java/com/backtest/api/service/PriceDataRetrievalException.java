package com.backtest.api.service;

public class PriceDataRetrievalException extends Exception {
  public PriceDataRetrievalException(String message) {
    super(message);
  }

  public PriceDataRetrievalException(String message, Throwable cause) {
    super(message, cause);
  }
}
