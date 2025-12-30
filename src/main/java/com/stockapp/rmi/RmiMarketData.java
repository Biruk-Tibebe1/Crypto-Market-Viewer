package com.stockapp.rmi;

import com.stockapp.model.StockData;

import java.util.List;
import java.util.Map;

public class RmiMarketData {
    private static MarketDataService service;

    public static synchronized void init(String host, int port) throws Exception {
        if (service != null) return;
        RmiClient client = new RmiClient(host, port);
        service = client.getService();
        if (service == null) throw new Exception("RMI MarketDataService lookup returned null");
    }

    private static void ensureInitialized() throws Exception {
        if (service == null) throw new Exception("RMI MarketData not initialized. Call RmiMarketData.init(host,port) first.");
    }

    public static double getCurrentPrice(String symbol, String unused) throws Exception {
        ensureInitialized();
        try {
            // If caller provided a currency code in 'unused', prefer server-side currency-aware method
            if (unused != null && !unused.trim().isEmpty()) {
                String cur = unused.trim().toUpperCase();
                try { return service.getCurrentPriceIn(symbol, cur); } catch (NoSuchMethodError | AbstractMethodError ignored) {}
            }
            return service.getCurrentPrice(symbol);
        } catch (Exception e) {
            throw new Exception("RMI getCurrentPrice failed: " + e.getMessage(), e);
        }
    }

    public static List<StockData> getHistoricalData(String symbol, String interval, int limit) throws Exception {
        ensureInitialized();
        try {
            return service.getHistoricalData(symbol, interval, limit);
        } catch (Exception e) {
            throw new Exception("RMI getHistoricalData failed: " + e.getMessage(), e);
        }
    }

    public static List<String> getAllSymbols() throws Exception {
        ensureInitialized();
        try {
            return service.getAllSymbols();
        } catch (Exception e) {
            throw new Exception("RMI getAllSymbols failed: " + e.getMessage(), e);
        }
    }

    public static Map<String, Double> getAllPrices() throws Exception {
        ensureInitialized();
        try {
            return service.getAllPrices();
        } catch (Exception e) {
            throw new Exception("RMI getAllPrices failed: " + e.getMessage(), e);
        }
    }

    public static Map<String, Double> getAllPrices(String currency) throws Exception {
        ensureInitialized();
        try {
            if (currency != null && !currency.trim().isEmpty()) {
                try { return service.getAllPricesIn(currency.trim().toUpperCase()); } catch (NoSuchMethodError | AbstractMethodError ignored) {}
            }
            return service.getAllPrices();
        } catch (Exception e) {
            throw new Exception("RMI getAllPrices failed: " + e.getMessage(), e);
        }
    }

    public static Map<String, String> get24hTicker(String symbol) throws Exception {
        ensureInitialized();
        try {
            return service.get24hTicker(symbol);
        } catch (Exception e) {
            throw new Exception("RMI get24hTicker failed: " + e.getMessage(), e);
        }
    }

    public static Map<String, String> get24hTicker(String symbol, String currency) throws Exception {
        ensureInitialized();
        try {
            if (currency != null && !currency.trim().isEmpty()) {
                try { return service.get24hTickerIn(symbol, currency.trim().toUpperCase()); } catch (NoSuchMethodError | AbstractMethodError ignored) {}
            }
            return service.get24hTicker(symbol);
        } catch (Exception e) {
            throw new Exception("RMI get24hTicker failed: " + e.getMessage(), e);
        }
    }

    public static java.util.Map<String, java.util.List<String>> getOrderBook(String symbol, int limit) throws Exception {
        ensureInitialized();
        try {
            return service.getOrderBook(symbol, limit);
        } catch (Exception e) {
            throw new Exception("RMI getOrderBook failed: " + e.getMessage(), e);
        }
    }

    public static java.util.Map<String, java.util.List<String>> getOrderBook(String symbol, int limit, String currency) throws Exception {
        ensureInitialized();
        try {
            if (currency != null && !currency.trim().isEmpty()) {
                try { return service.getOrderBookIn(symbol, limit, currency.trim().toUpperCase()); } catch (NoSuchMethodError | AbstractMethodError ignored) {}
            }
            return service.getOrderBook(symbol, limit);
        } catch (Exception e) {
            throw new Exception("RMI getOrderBook failed: " + e.getMessage(), e);
        }
    }

    public static java.util.List<String> getRecentTrades(String symbol, int limit) throws Exception {
        ensureInitialized();
        try {
            return service.getRecentTrades(symbol, limit);
        } catch (Exception e) {
            throw new Exception("RMI getRecentTrades failed: " + e.getMessage(), e);
        }
    }

    public static java.util.List<String> getRecentTrades(String symbol, int limit, String currency) throws Exception {
        ensureInitialized();
        try {
            if (currency != null && !currency.trim().isEmpty()) {
                try { return service.getRecentTradesIn(symbol, limit, currency.trim().toUpperCase()); } catch (NoSuchMethodError | AbstractMethodError ignored) {}
            }
            return service.getRecentTrades(symbol, limit);
        } catch (Exception e) {
            throw new Exception("RMI getRecentTrades failed: " + e.getMessage(), e);
        }
    }

    public static java.util.List<StockData> getHistoricalData(String symbol, String interval, int limit, String currency) throws Exception {
        ensureInitialized();
        try {
            if (currency != null && !currency.trim().isEmpty()) {
                try { return service.getHistoricalDataIn(symbol, interval, limit, currency.trim().toUpperCase()); } catch (NoSuchMethodError | AbstractMethodError ignored) {}
            }
            return service.getHistoricalData(symbol, interval, limit);
        } catch (Exception e) {
            throw new Exception("RMI getHistoricalData failed: " + e.getMessage(), e);
        }
    }

    public static StockData getLatestKline(String pairSymbol, String interval) throws Exception {
        ensureInitialized();
        try {
            return service.getLatestKline(pairSymbol, interval);
        } catch (Exception e) {
            throw new Exception("RMI getLatestKline failed: " + e.getMessage(), e);
        }
    }

    public static double getUsdToFiatRate(String fiat) throws Exception {
        ensureInitialized();
        try {
            return service.getUsdToFiatRate(fiat == null ? null : fiat.trim().toUpperCase());
        } catch (Exception e) {
            throw new Exception("RMI getUsdToFiatRate failed: " + e.getMessage(), e);
        }
    }

    public static java.util.Map<String, String> getCoinDetails(String symbol) throws Exception {
        ensureInitialized();
        try {
            return service.getCoinDetails(symbol);
        } catch (Exception e) {
            throw new Exception("RMI getCoinDetails failed: " + e.getMessage(), e);
        }
    }
}
