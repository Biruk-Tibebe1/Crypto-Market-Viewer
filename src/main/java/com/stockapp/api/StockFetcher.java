package com.stockapp.api;

import com.stockapp.model.StockData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockFetcher {
    // Use Binance public API (no API key for basic market data, higher limits)
    private static final String BINANCE_PRICE = "https://api.binance.com/api/v3/ticker/price?symbol=%s";
    private static final String BINANCE_KLINES_TEMPLATE = "https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=%d";

    private static final Map<String, String> PAIR_MAP = new HashMap<>();
    static {
        PAIR_MAP.put("BTC", "BTCUSDT");
        PAIR_MAP.put("ETH", "ETHUSDT");
        PAIR_MAP.put("SOL", "SOLUSDT");
        PAIR_MAP.put("XRP", "XRPUSDT");
        PAIR_MAP.put("ADA", "ADAUSDT");
        PAIR_MAP.put("DOGE", "DOGEUSDT");
        PAIR_MAP.put("BNB", "BNBUSDT");
        PAIR_MAP.put("LTC", "LTCUSDT");
    }

    // Cache for all prices to reduce API calls
    private static Map<String, Double> priceCache = new HashMap<>();
    private static long lastPriceFetch = 0;
    private static final long CACHE_DURATION = 30000; // 30 seconds

    public static double getCurrentPrice(String symbol, String unused) throws IOException {
        String pair = PAIR_MAP.getOrDefault(symbol.toUpperCase(), symbol.toUpperCase() + "USDT");
        String url = String.format(BINANCE_PRICE, pair);
        String json = httpGet(url);
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("price")) return Double.parseDouble(root.get("price").getAsString());
        } catch (Exception e) {
            throw new IOException("Invalid Binance price response: " + e.getMessage(), e);
        }
        throw new IOException("Price missing in response for " + symbol);
    }

    public static List<StockData> getHistoricalData(String symbol, String unused) throws IOException {
        // maintain compatibility: default to 1d, limit 7
        return getHistoricalData(symbol, "1d", 7);
    }

    public static List<StockData> getHistoricalData(String symbol, String interval, int limit) throws IOException {
        List<StockData> result = new ArrayList<>();
        String pair = PAIR_MAP.getOrDefault(symbol.toUpperCase(), symbol.toUpperCase() + "USDT");
        String url = String.format(BINANCE_KLINES_TEMPLATE, pair, interval, limit);
        String json = httpGet(url);
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            // Binance returns oldest->newest
            for (JsonElement el : arr) {
                JsonArray k = el.getAsJsonArray();
                long ts = k.get(0).getAsLong();
                double open = Double.parseDouble(k.get(1).getAsString());
                double high = Double.parseDouble(k.get(2).getAsString());
                double low = Double.parseDouble(k.get(3).getAsString());
                double close = Double.parseDouble(k.get(4).getAsString());
                long volume = 0L;
                try {
                    String volStr = k.get(5).getAsString();
                    // volume may be fractional; keep as long of integer part
                    volume = (long) Double.parseDouble(volStr);
                } catch (Exception ignored) {}
                java.time.LocalDateTime dt = Instant.ofEpochMilli(ts).atZone(ZoneId.of("UTC")).toLocalDateTime();
                result.add(new StockData(symbol, dt, open, high, low, close, volume));
            }
        } catch (Exception e) {
            throw new IOException("Invalid Binance klines response: " + e.getMessage(), e);
        }
        return result;
    }

    public static List<String> getAllSymbols() throws IOException {
        String url = "https://api.binance.com/api/v3/ticker/price";
        String json = httpGet(url);
        List<String> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String s = o.get("symbol").getAsString();
                // only include USDT pairs for simplicity
                if (s.endsWith("USDT")) list.add(s.substring(0, s.length() - 4));
            }
        } catch (Exception e) {
            throw new IOException("Invalid Binance all-symbols response: " + e.getMessage(), e);
        }
        return list;
    }

    public static Map<String, Double> getAllPrices() throws IOException {
        long now = System.currentTimeMillis();
        if (now - lastPriceFetch < CACHE_DURATION && !priceCache.isEmpty()) {
            return new HashMap<>(priceCache);
        }
        String url = "https://api.binance.com/api/v3/ticker/price";
        String json = httpGet(url);
        Map<String, Double> map = new HashMap<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String s = o.get("symbol").getAsString();
                if (s.endsWith("USDT")) {
                    String sym = s.substring(0, s.length() - 4);
                    double price = Double.parseDouble(o.get("price").getAsString());
                    map.put(sym, price);
                }
            }
            priceCache = map;
            lastPriceFetch = now;
        } catch (Exception e) {
            throw new IOException("Invalid Binance all-prices response: " + e.getMessage(), e);
        }
        return map;
    }

    public static Map<String, List<String>> getOrderBook(String symbol, int limit) throws IOException {
        String pair = symbol.toUpperCase();
        if (!pair.endsWith("USDT")) {
            pair = PAIR_MAP.getOrDefault(pair, pair + "USDT");
        }
        String url = String.format("https://api.binance.com/api/v3/depth?symbol=%s&limit=%d", pair, limit);
        String json = httpGet(url);
        Map<String, List<String>> map = new HashMap<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray bids = root.getAsJsonArray("bids");
            JsonArray asks = root.getAsJsonArray("asks");
            List<String> lb = new ArrayList<>();
            List<String> la = new ArrayList<>();
            for (JsonElement b : bids) {
                JsonArray arr = b.getAsJsonArray();
                lb.add(arr.get(0).getAsString() + " @ " + arr.get(1).getAsString());
            }
            for (JsonElement a : asks) {
                JsonArray arr = a.getAsJsonArray();
                la.add(arr.get(0).getAsString() + " @ " + arr.get(1).getAsString());
            }
            map.put("bids", lb);
            map.put("asks", la);
        } catch (Exception e) {
            throw new IOException("Invalid Binance depth response: " + e.getMessage(), e);
        }
        return map;
    }

    public static List<String> getRecentTrades(String symbol, int limit) throws IOException {
        String pair = PAIR_MAP.getOrDefault(symbol.toUpperCase(), symbol.toUpperCase() + "USDT");
        String url = String.format("https://api.binance.com/api/v3/trades?symbol=%s&limit=%d", pair, limit);
        String json = httpGet(url);
        List<String> trades = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String price = o.get("price").getAsString();
                String qty = o.get("qty").getAsString();
                trades.add(price + " @ " + qty);
            }
        } catch (Exception e) {
            throw new IOException("Invalid Binance trades response: " + e.getMessage(), e);
        }
        return trades;
    }

    public static Map<String, String> get24hTicker(String symbol) throws IOException {
        String pair = symbol.toUpperCase();
        if (!pair.endsWith("USDT")) {
            pair = PAIR_MAP.getOrDefault(pair, pair + "USDT");
        }
        String url = String.format("https://api.binance.com/api/v3/ticker/24hr?symbol=%s", pair);
        String json = httpGet(url);
        Map<String, String> map = new HashMap<>();
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            map.put("lastPrice", o.has("lastPrice") ? o.get("lastPrice").getAsString() : "");
            map.put("priceChangePercent", o.has("priceChangePercent") ? o.get("priceChangePercent").getAsString() : "");
            map.put("highPrice", o.has("highPrice") ? o.get("highPrice").getAsString() : "");
            map.put("lowPrice", o.has("lowPrice") ? o.get("lowPrice").getAsString() : "");
            map.put("volume", o.has("volume") ? o.get("volume").getAsString() : "");
        } catch (Exception e) {
            throw new IOException("Invalid Binance 24hr ticker response: " + e.getMessage(), e);
        }
        return map;
    }

    // --- CoinGecko helpers for detailed coin info (ATH/ATL, genesis date) ---
    private static List<JsonObject> cachedCoinList = null;

    private static synchronized void ensureCoinListLoaded() throws IOException {
        if (cachedCoinList != null) return;
        String url = "https://api.coingecko.com/api/v3/coins/list";
        String json = httpGet(url);
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            cachedCoinList = new ArrayList<>();
            for (JsonElement el : arr) cachedCoinList.add(el.getAsJsonObject());
        } catch (Exception e) {
            throw new IOException("Invalid CoinGecko coin list: " + e.getMessage(), e);
        }
    }

    private static String findCoinGeckoIdForSymbol(String symbol) throws IOException {
        ensureCoinListLoaded();
        String s = symbol.toLowerCase();
        for (JsonObject o : cachedCoinList) {
            try {
                String sym = o.has("symbol") ? o.get("symbol").getAsString().toLowerCase() : "";
                if (sym.equals(s)) return o.get("id").getAsString();
            } catch (Exception ignored) {}
        }
        // fallback: try exact id match
        for (JsonObject o : cachedCoinList) {
            try { if (o.has("id") && o.get("id").getAsString().equalsIgnoreCase(symbol)) return o.get("id").getAsString(); } catch (Exception ignored) {}
        }
        return null;
    }

    public static Map<String, String> getCoinDetailsFromCoinGecko(String symbol) throws IOException {
        String id = findCoinGeckoIdForSymbol(symbol);
        if (id == null) throw new IOException("CoinGecko id not found for symbol: " + symbol);
        String url = String.format("https://api.coingecko.com/api/v3/coins/%s?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false", id);
        String json = httpGet(url);
        Map<String, String> map = new HashMap<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject market = root.has("market_data") ? root.getAsJsonObject("market_data") : null;
            if (market != null) {
                // ATH / ATL
                if (market.has("ath") && market.getAsJsonObject("ath").has("usd")) map.put("ath", market.getAsJsonObject("ath").get("usd").getAsString());
                if (market.has("ath_date") && market.getAsJsonObject("ath_date").has("usd")) map.put("ath_date", market.getAsJsonObject("ath_date").get("usd").getAsString());
                if (market.has("atl") && market.getAsJsonObject("atl").has("usd")) map.put("atl", market.getAsJsonObject("atl").get("usd").getAsString());
                if (market.has("atl_date") && market.getAsJsonObject("atl_date").has("usd")) map.put("atl_date", market.getAsJsonObject("atl_date").get("usd").getAsString());
                if (market.has("high_24h") && market.getAsJsonObject("high_24h").has("usd")) map.put("high_24h", market.getAsJsonObject("high_24h").get("usd").getAsString());
                if (market.has("low_24h") && market.getAsJsonObject("low_24h").has("usd")) map.put("low_24h", market.getAsJsonObject("low_24h").get("usd").getAsString());
                if (market.has("market_cap") && market.getAsJsonObject("market_cap").has("usd")) map.put("market_cap", market.getAsJsonObject("market_cap").get("usd").getAsString());
            }
            if (root.has("genesis_date")) map.put("genesis_date", root.get("genesis_date").getAsString());
            if (root.has("last_updated")) map.put("last_updated", root.get("last_updated").getAsString());
        } catch (Exception e) {
            throw new IOException("Invalid CoinGecko coin detail response: " + e.getMessage(), e);
        }
        return map;
    }

    // Fetch USD -> fiat conversion rate (e.g., ETB).
    // First try Binance for a USDT<FIAT> trading pair (live crypto market rate). If not available,
    // fall back to exchangerate.host (fiat FX rates).
    public static double getUsdToFiatRate(String fiat) throws IOException {
        if (fiat == null) return 1.0;
        String code = fiat.trim().toUpperCase();
        if ("USD".equals(code) || "USDT".equals(code)) return 1.0;

        // Binance does not list all fiat pairs (e.g., ETB). For ETB prefer a fiat FX provider.
        if ("ETB".equals(code)) {
            // Allow overriding the ETB conversion rate via env var or system property for exact control.
            // Priority: system property `stockapp.usdt.etb`, then environment `STOCKAPP_USDT_ETB`.
            try {
                String prop = System.getProperty("stockapp.usdt.etb");
                if (prop == null || prop.trim().isEmpty()) prop = System.getenv("STOCKAPP_USDT_ETB");
                if (prop != null && !prop.trim().isEmpty()) {
                    try {
                        double override = Double.parseDouble(prop.trim());
                        System.err.println("[RateDebug] Using overridden ETB rate from config: " + override);
                        return override;
                    } catch (NumberFormatException nfe) {
                        System.err.println("[RateDebug] Invalid override STOCKAPP_USDT_ETB: " + prop);
                    }
                }
            } catch (Exception ignored) {}
            // exchangerate.host now requires an access key for some accounts; use open.er-api.com
            // which provides a free USD base rates endpoint.
            String urlEr = "https://open.er-api.com/v6/latest/USD";
            try {
                String json = httpGet(urlEr);
                System.err.println("[RateDebug] open.er-api.com response for ETB: " + json);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                if (root.has("rates") && root.getAsJsonObject("rates").has(code)) {
                    double val = root.getAsJsonObject("rates").get(code).getAsDouble();
                    System.err.println("[RateDebug] parsed ETB rate (open.er-api): " + val);
                    return val;
                }
            } catch (Exception e) {
                System.err.println("open.er-api.com lookup failed for ETB: " + e.getMessage());
                e.printStackTrace(System.err);
            }
            // last resort: try exchangerate.host endpoints in case user has an access key configured
            try {
                String urlFx = String.format("https://api.exchangerate.host/latest?base=USD&symbols=%s", code);
                String json = httpGet(urlFx);
                System.err.println("[RateDebug] exchangerate.host response (latest fallback) for ETB: " + json);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                if (root.has("rates") && root.getAsJsonObject("rates").has(code)) {
                    double val = root.getAsJsonObject("rates").get(code).getAsDouble();
                    System.err.println("[RateDebug] parsed ETB rate (exchangerate.host fallback): " + val);
                    return val;
                }
            } catch (Exception ignored) {}

            System.err.println("Exchange rate not found for " + code + " - returning 1.0");
            return 1.0;
        }

        // Try Binance: many fiat/USDT pairs are available as USDT<FIAT> (e.g., USDTTRY, USDTBRL).
        // Attempt USDT{code} price and interpret that as 1 USDT = price {FIAT}.
        try {
            String binanceSymbol = "USDT" + code;
            String url = String.format(BINANCE_PRICE, binanceSymbol);
            String json = httpGet(url);
            System.err.println("[RateDebug] Binance response for " + binanceSymbol + ": " + json);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("price")) {
                double p = Double.parseDouble(root.get("price").getAsString());
                // sanity check
                if (p > 0) {
                    System.err.println("[RateDebug] parsed Binance price for " + binanceSymbol + ": " + p);
                    return p;
                }
            }
        } catch (Exception e) {
            // not available on Binance or request failed; fall through to FX provider
            System.err.println("Binance fiat pair lookup failed for " + code + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }

        // Fallback: use exchangerate.host for USD -> fiat conversion
        String urlFx = String.format("https://api.exchangerate.host/latest?base=USD&symbols=%s", code);
        try {
            String json = httpGet(urlFx);
            System.err.println("[RateDebug] exchangerate.host response (latest fallback): " + json);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("rates") && root.getAsJsonObject("rates").has(code)) {
                double val = root.getAsJsonObject("rates").get(code).getAsDouble();
                System.err.println("[RateDebug] parsed rate (latest fallback): " + val);
                return val;
            }
        } catch (Exception e) {
            System.err.println("primary rate fetch failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        // Fallback convert endpoint
        try {
            String url2 = String.format("https://api.exchangerate.host/convert?from=USD&to=%s", code);
            String json2 = httpGet(url2);
            JsonObject r2 = JsonParser.parseString(json2).getAsJsonObject();
            if (r2.has("result") && !r2.get("result").isJsonNull()) {
                return r2.get("result").getAsDouble();
            }
        } catch (Exception e) {
            System.err.println("fallback rate fetch failed: " + e.getMessage());
        }

        System.err.println("Exchange rate not found for " + code + " - returning 1.0");
        return 1.0;
    }

    private static String httpGet(String urlStr) throws IOException {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Java-App/1.0");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(20_000);
            int rc = conn.getResponseCode();
            is = rc >= 200 && rc < 300 ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            String body = sb.toString();
            if (rc == 429) {
                throw new IOException("Rate limit exceeded. Please wait before making more requests. Binance API limit: 6000 weight per minute.");
            }
            if (rc < 200 || rc >= 300) {
                System.err.println("HTTP error when calling " + urlStr + ": code=" + rc + " body=" + body);
                throw new IOException("HTTP " + rc + ": " + body);
            }
            return body;
        } finally {
            if (is != null) try { is.close(); } catch (IOException ignore) {}
            if (conn != null) conn.disconnect();
        }
    }
}
