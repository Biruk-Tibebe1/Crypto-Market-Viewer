package com.stockapp.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BinanceRealtime {
    private WebSocket ws;
    private final AtomicReference<Consumer<JsonObject>> handler = new AtomicReference<>();

    public void subscribeKlines(String pairSymbol, String interval, Consumer<JsonObject> onKline) {
        // pairSymbol expected like BTCUSDT
        try {
            String stream = pairSymbol.toLowerCase() + "@kline_" + interval;
            String url = "wss://stream.binance.com:9443/ws/" + stream;
            handler.set(onKline);
            HttpClient client = HttpClient.newHttpClient();
            ws = client.newWebSocketBuilder().buildAsync(URI.create(url), new WebSocket.Listener() {
                private StringBuilder sb = new StringBuilder();

                @Override
                public void onOpen(WebSocket webSocket) {
                    WebSocket.Listener.super.onOpen(webSocket);
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    sb.append(data);
                    if (last) {
                        String text = sb.toString();
                        sb.setLength(0);
                        try {
                            JsonElement el = JsonParser.parseString(text);
                            if (el != null && el.isJsonObject()) {
                                JsonObject obj = el.getAsJsonObject();
                                if (obj.has("k")) {
                                    JsonObject k = obj.getAsJsonObject("k");
                                    Consumer<JsonObject> h = handler.get();
                                    if (h != null) h.accept(k);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    try { webSocket.abort(); } catch (Exception ignored) {}
                }
            }).join();
        } catch (Exception e) {
            throw new RuntimeException("Failed to subscribe websocket: " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            if (ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
        } catch (Exception ignored) {}
        ws = null;
    }
}
