package com.stockapp.rmi;

// storage removed; no local caching
import com.stockapp.api.StockFetcher;
import com.stockapp.model.StockData;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class MarketDataServiceImpl extends UnicastRemoteObject implements MarketDataService {
    protected MarketDataServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<StockData> getHistoricalData(String symbol, String interval, int limit) throws RemoteException {
        try {
            List<StockData> fetched = StockFetcher.getHistoricalData(symbol, interval, limit);
            return fetched;
        } catch (Exception e) {
            throw new RemoteException("getHistoricalData failed: " + e.getMessage(), e);
        }
    }

    @Override
    public double getCurrentPrice(String symbol) throws RemoteException {
        try {
            return StockFetcher.getCurrentPrice(symbol, "");
        } catch (Exception e) {
            throw new RemoteException("getCurrentPrice failed: " + e.getMessage(), e);
        }
    }

    @Override
    public double getCurrentPriceIn(String symbol, String currency) throws RemoteException {
        try {
            double price = StockFetcher.getCurrentPrice(symbol, "");
            if (currency == null) return price;
            String cur = currency.trim().toUpperCase();
            if ("USD".equals(cur) || "USDT".equals(cur)) return price;
            double rate = StockFetcher.getUsdToFiatRate(cur);
            return price * rate;
        } catch (Exception e) {
            throw new RemoteException("getCurrentPriceIn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> get24hTicker(String symbol) throws RemoteException {
        try {
            return StockFetcher.get24hTicker(symbol);
        } catch (Exception e) {
            throw new RemoteException("get24hTicker failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> get24hTickerIn(String symbol, String currency) throws RemoteException {
        try {
            Map<String, String> base = StockFetcher.get24hTicker(symbol);
            if (currency == null) return base;
            String cur = currency.trim().toUpperCase();
            if ("USD".equals(cur) || "USDT".equals(cur)) return base;
            double rate = StockFetcher.getUsdToFiatRate(cur);
            Map<String, String> out = new java.util.HashMap<>(base);
            try {
                if (base.containsKey("lastPrice") && !base.get("lastPrice").isEmpty()) {
                    double lp = Double.parseDouble(base.get("lastPrice"));
                    out.put("lastPrice", String.valueOf(lp * rate));
                }
                if (base.containsKey("highPrice") && !base.get("highPrice").isEmpty()) {
                    double v = Double.parseDouble(base.get("highPrice")); out.put("highPrice", String.valueOf(v * rate));
                }
                if (base.containsKey("lowPrice") && !base.get("lowPrice").isEmpty()) {
                    double v = Double.parseDouble(base.get("lowPrice")); out.put("lowPrice", String.valueOf(v * rate));
                }
            } catch (Exception ignored) {}
            return out;
        } catch (Exception e) {
            throw new RemoteException("get24hTickerIn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Double> getAllPrices() throws RemoteException {
        try {
            return StockFetcher.getAllPrices();
        } catch (Exception e) {
            throw new RemoteException("getAllPrices failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Double> getAllPricesIn(String currency) throws RemoteException {
        try {
            Map<String, Double> base = StockFetcher.getAllPrices();
            if (currency == null) return base;
            String cur = currency.trim().toUpperCase();
            if ("USD".equals(cur) || "USDT".equals(cur)) return base;
            double rate = StockFetcher.getUsdToFiatRate(cur);
            Map<String, Double> out = new java.util.HashMap<>();
            for (Map.Entry<String, Double> e : base.entrySet()) out.put(e.getKey(), e.getValue() * rate);
            return out;
        } catch (Exception e) {
            throw new RemoteException("getAllPricesIn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAllSymbols() throws RemoteException {
        try {
            return StockFetcher.getAllSymbols();
        } catch (Exception e) {
            throw new RemoteException("getAllSymbols failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, java.util.List<String>> getOrderBook(String symbol, int limit) throws RemoteException {
        try {
            return StockFetcher.getOrderBook(symbol, limit);
        } catch (Exception e) {
            throw new RemoteException("getOrderBook failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, java.util.List<String>> getOrderBookIn(String symbol, int limit, String currency) throws RemoteException {
        try {
            Map<String, java.util.List<String>> base = StockFetcher.getOrderBook(symbol, limit);
            if (currency == null) return base;
            String cur = currency.trim().toUpperCase();
            if ("USD".equals(cur) || "USDT".equals(cur)) return base;
            double rate = StockFetcher.getUsdToFiatRate(cur);
            Map<String, java.util.List<String>> out = new java.util.HashMap<>();
            java.util.List<String> bids = new java.util.ArrayList<>();
            java.util.List<String> asks = new java.util.ArrayList<>();
            for (String b : base.getOrDefault("bids", java.util.Collections.emptyList())) {
                String[] parts = b.split(" @ ");
                try { double p = Double.parseDouble(parts[0]) * rate; bids.add(String.format(java.util.Locale.US, "%.6f @ %s", p, parts[1])); } catch (Exception ex) { bids.add(b); }
            }
            for (String a : base.getOrDefault("asks", java.util.Collections.emptyList())) {
                String[] parts = a.split(" @ ");
                try { double p = Double.parseDouble(parts[0]) * rate; asks.add(String.format(java.util.Locale.US, "%.6f @ %s", p, parts[1])); } catch (Exception ex) { asks.add(a); }
            }
            out.put("bids", bids); out.put("asks", asks);
            return out;
        } catch (Exception e) {
            throw new RemoteException("getOrderBookIn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.List<String> getRecentTrades(String symbol, int limit) throws RemoteException {
        try {
            return StockFetcher.getRecentTrades(symbol, limit);
        } catch (Exception e) {
            throw new RemoteException("getRecentTrades failed: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.List<String> getRecentTradesIn(String symbol, int limit, String currency) throws RemoteException {
        try {
            java.util.List<String> base = StockFetcher.getRecentTrades(symbol, limit);
            if (currency == null) return base;
            String cur = currency.trim().toUpperCase();
            if ("USD".equals(cur) || "USDT".equals(cur)) return base;
            double rate = StockFetcher.getUsdToFiatRate(cur);
            java.util.List<String> out = new java.util.ArrayList<>();
            for (String t : base) {
                try {
                    String[] parts = t.split(" @ ");
                    double p = Double.parseDouble(parts[0]) * rate;
                    out.add(String.format(java.util.Locale.US, "%.6f @ %s", p, parts[1]));
                } catch (Exception ex) { out.add(t); }
            }
            return out;
        } catch (Exception e) {
            throw new RemoteException("getRecentTradesIn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockData> getHistoricalDataIn(String symbol, String interval, int limit, String currency) throws RemoteException {
        try {
            List<StockData> base = StockFetcher.getHistoricalData(symbol, interval, limit);
            if (currency == null) return base;
            String cur = currency.trim().toUpperCase();
            if ("USD".equals(cur) || "USDT".equals(cur)) return base;
            double rate = StockFetcher.getUsdToFiatRate(cur);
            List<StockData> out = new java.util.ArrayList<>();
            for (StockData s : base) {
                StockData n = new StockData(s.getSymbol(), s.getDate(), s.getOpen() * rate, s.getHigh() * rate, s.getLow() * rate, s.getClose() * rate, s.getVolume());
                out.add(n);
            }
            return out;
        } catch (Exception e) {
            throw new RemoteException("getHistoricalDataIn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public double getUsdToFiatRate(String fiat) throws RemoteException {
        try {
            return StockFetcher.getUsdToFiatRate(fiat);
        } catch (Exception e) {
            throw new RemoteException("getUsdToFiatRate failed: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.Map<String, String> getCoinDetails(String symbol) throws RemoteException {
        try {
            return StockFetcher.getCoinDetailsFromCoinGecko(symbol);
        } catch (Exception e) {
            throw new RemoteException("getCoinDetails failed: " + e.getMessage(), e);
        }
    }

    @Override
    public StockData getLatestKline(String pairSymbol, String interval) throws RemoteException {
        try {
            // ensure manager subscribed
            RealtimeManager.getInstance().ensureSubscribed(pairSymbol, interval);
            StockData sd = RealtimeManager.getInstance().getLatest(pairSymbol, interval);
            if (sd != null) return sd;
            // fallback: return last element from historical data
            java.util.List<StockData> hist = StockFetcher.getHistoricalData(pairSymbol.replace("USDT", ""), interval, 1);
            if (hist != null && !hist.isEmpty()) return hist.get(hist.size()-1);
            return null;
        } catch (Exception e) {
            throw new RemoteException("getLatestKline failed: " + e.getMessage(), e);
        }
    }

    // Trading APIs removed
}
