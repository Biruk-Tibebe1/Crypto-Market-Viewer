package com.stockapp.rmi;

import com.stockapp.model.StockData;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface MarketDataService extends Remote {
    // Market data
    List<StockData> getHistoricalData(String symbol, String interval, int limit) throws RemoteException;
    double getCurrentPrice(String symbol) throws RemoteException;
    // currency-aware methods (currency code like "USDT" or "ETB")
    double getCurrentPriceIn(String symbol, String currency) throws RemoteException;
    Map<String, String> get24hTicker(String symbol) throws RemoteException;
    Map<String, String> get24hTickerIn(String symbol, String currency) throws RemoteException;
    Map<String, Double> getAllPrices() throws RemoteException;
    Map<String, Double> getAllPricesIn(String currency) throws RemoteException;
    List<String> getAllSymbols() throws RemoteException;
    Map<String, java.util.List<String>> getOrderBook(String symbol, int limit) throws RemoteException;
    Map<String, java.util.List<String>> getOrderBookIn(String symbol, int limit, String currency) throws RemoteException;
    java.util.List<String> getRecentTrades(String symbol, int limit) throws RemoteException;
    java.util.List<String> getRecentTradesIn(String symbol, int limit, String currency) throws RemoteException;
    List<StockData> getHistoricalDataIn(String symbol, String interval, int limit, String currency) throws RemoteException;
    // Additional helpers
    double getUsdToFiatRate(String fiat) throws RemoteException;
    java.util.Map<String, String> getCoinDetails(String symbol) throws RemoteException;
    // Real-time helper: return latest kline for symbol (pair like BTCUSDT) and interval
    StockData getLatestKline(String pairSymbol, String interval) throws RemoteException;
    // Trading APIs removed
}
