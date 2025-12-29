package com.stockapp.rmi;

import com.google.gson.JsonObject;
import com.stockapp.api.BinanceRealtime;
import com.stockapp.model.StockData;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RealtimeManager {
    private static final RealtimeManager INSTANCE = new RealtimeManager();
    private final Map<String, StockData> latest = new ConcurrentHashMap<>(); // key = SYMBOL:INTERVAL
    private final Map<String, BinanceRealtime> subs = new ConcurrentHashMap<>();

    private RealtimeManager() {}

    public static RealtimeManager getInstance() { return INSTANCE; }

    public void ensureSubscribed(String pairSymbol, String interval) {
        String key = pairSymbol + ":" + interval;
        if (subs.containsKey(key)) return;
        try {
            BinanceRealtime br = new BinanceRealtime();
            br.subscribeKlines(pairSymbol, interval, k -> {
                try {
                    long ts = k.has("t") ? k.get("t").getAsLong() : System.currentTimeMillis();
                    double open = k.has("o") ? Double.parseDouble(k.get("o").getAsString()) : 0.0;
                    double high = k.has("h") ? Double.parseDouble(k.get("h").getAsString()) : 0.0;
                    double low = k.has("l") ? Double.parseDouble(k.get("l").getAsString()) : 0.0;
                    double close = k.has("c") ? Double.parseDouble(k.get("c").getAsString()) : 0.0;
                    long volume = k.has("v") ? (long)Double.parseDouble(k.get("v").getAsString()) : 0L;
                    LocalDateTime dt = Instant.ofEpochMilli(ts).atZone(ZoneId.of("UTC")).toLocalDateTime();
                    StockData sd = new StockData(pairSymbol.replace("USDT",""), dt, open, high, low, close, volume);
                    latest.put(key, sd);
                } catch (Exception ignored) {}
            });
            subs.put(key, br);
        } catch (Exception e) {
            // ignore subscribe errors; caller can fallback to polling historical
        }
    }

    public StockData getLatest(String pairSymbol, String interval) {
        String key = pairSymbol + ":" + interval;
        return latest.get(key);
    }

    public void stopAll() {
        for (BinanceRealtime b : subs.values()) {
            try { b.close(); } catch (Exception ignored) {}
        }
        subs.clear();
        latest.clear();
    }
}
