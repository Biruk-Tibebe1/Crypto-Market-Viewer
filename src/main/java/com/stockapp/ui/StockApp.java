package com.stockapp.ui;

import com.stockapp.api.StockFetcher;
import com.stockapp.model.StockData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
// tooltip removed — avoid per-node tooltips for performance
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.web.WebEngine;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
// BinanceRealtime moved to server; client should not instantiate it
import com.stockapp.rmi.RmiMarketData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.prefs.Preferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Arrays;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StockApp extends Application {

    public static StockApp INSTANCE;
    public static Stage MAIN_STAGE;

    // Base64 encoded chart background image
    private static final String CHART_BACKGROUND_IMAGE = "data:image/jpeg;base64,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";

    // track open detail windows so theme changes can be propagated
    private static final List<DetailWindow> DETAIL_WINDOWS = Collections.synchronizedList(new ArrayList<>());

    private static class DetailWindow {
        final Stage stage;
        final LineChart<String, Number> priceChart;
        final BarChart<String, Number> volChart;

        DetailWindow(Stage s, LineChart<String, Number> pc, BarChart<String, Number> vc) {
            this.stage = s; this.priceChart = pc; this.volChart = vc;
        }
    }

    private final ObservableList<TrackedStock> tracked = FXCollections.observableArrayList();
    private Label statusLabel = new Label("Ready");
    private TextArea debugArea = new TextArea();
    private final DecimalFormat priceFmt = new DecimalFormat("#,##0.00");
    private final Map<String, Label> popularPriceLabels = new HashMap<>();
    private final Map<String, Label> popularChangeLabels = new HashMap<>();
    private LineChart<String, Number> priceChartField;
    private BarChart<String, Number> volumeChartField;
    // Center-area helpers (shared so other methods can update/replace children/messages)
    private javafx.scene.layout.VBox centerCharts;
    private Label centerMessage;
    // Realtime chart management
    private java.util.concurrent.ScheduledExecutorService realtimeUpdater;
    private String currentSymbol = "ETHUSDT";
    // display currency (USDT or ETB) and conversion rate
    private String displayCurrency = "USDT";
    private double usdToFiatRate = 1.0; // USD -> ETB rate when displayCurrency == "ETB"
    private java.util.concurrent.ScheduledExecutorService fxRateUpdater;
    // track current theme (false = light, true = dark)
    private boolean darkMode = false;
    // track current search filter and view state
    private List<Double> closes = new ArrayList<>();
    private String currentSearchFilter = "";
    private boolean currentShowAll = false;
    // track user favorites
    private Set<String> userFavorites = new HashSet<>();

    // UI components for integration
    private ListView<HBox> marketListView;
    private Label tickerSymbolLabel;
    private Label tickerPriceLabel;
    private Label tickerApproxLabel;
    private Label change24hLabel;
    private Label high24hLabel;
    private Label low24hLabel;
    private Label vol24hLabel;
    private ListView<HBox> sellOrdersList;
    private ListView<HBox> buyOrdersList;
    private Label lastPriceLabel;
    // header favorite star for currently selected symbol
    private Label headerFavoriteLabel;

    private void updateOrderBook(String symbol) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Map<String, List<String>> orderBook = RmiMarketData.getOrderBook(symbol, 20);
                    if (orderBook != null) {
                        List<String> asks = orderBook.get("asks");
                        List<String> bids = orderBook.get("bids");

                        Platform.runLater(() -> {
                            // Update sell orders (asks) - red
                            ObservableList<HBox> sellItems = FXCollections.observableArrayList();
                            for (int i = 0; i < Math.min(asks.size(), 10); i++) {
                                String[] parts = asks.get(i).split(" @ ");
                                double price = Double.parseDouble(parts[0]);
                                double amount = Double.parseDouble(parts[1]);
                                double total = price * amount;

                                HBox orderItem = new HBox();
                                orderItem.setPadding(new Insets(2, 16, 2, 16));
                                orderItem.setSpacing(8);
                                orderItem.setStyle("-fx-background-color: rgba(255,82,82,0.1);");

                                Label priceLabel = new Label(formatCurrency(price));
                                priceLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px; -fx-font-weight: bold;");
                                priceLabel.setPrefWidth(80);

                                Region spacer = new Region();
                                HBox.setHgrow(spacer, Priority.ALWAYS);

                                Label amountLabel = new Label(String.format("%.6f", amount));
                                amountLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                                amountLabel.setPrefWidth(80);

                                Label totalLabel = new Label(formatCurrency(total));
                                totalLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");
                                totalLabel.setPrefWidth(60);

                                orderItem.getChildren().addAll(priceLabel, spacer, amountLabel, totalLabel);
                                sellItems.add(orderItem);
                            }
                            sellOrdersList.setItems(sellItems);

                            // Update buy orders (bids) - green
                            ObservableList<HBox> buyItems = FXCollections.observableArrayList();
                            for (int i = 0; i < Math.min(bids.size(), 10); i++) {
                                String[] parts = bids.get(i).split(" @ ");
                                double price = Double.parseDouble(parts[0]);
                                double amount = Double.parseDouble(parts[1]);
                                double total = price * amount;

                                HBox orderItem = new HBox();
                                orderItem.setPadding(new Insets(2, 16, 2, 16));
                                orderItem.setSpacing(8);
                                orderItem.setStyle("-fx-background-color: rgba(54,226,123,0.1);");

                                Label priceLabel = new Label(formatCurrency(price));
                                priceLabel.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 12px; -fx-font-weight: bold;");
                                priceLabel.setPrefWidth(80);

                                Region spacer = new Region();
                                HBox.setHgrow(spacer, Priority.ALWAYS);

                                Label amountLabel = new Label(String.format("%.6f", amount));
                                amountLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                                amountLabel.setPrefWidth(80);

                                Label totalLabel = new Label(formatCurrency(total));
                                totalLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");
                                totalLabel.setPrefWidth(60);

                                orderItem.getChildren().addAll(priceLabel, spacer, amountLabel, totalLabel);
                                buyItems.add(orderItem);
                            }
                            buyOrdersList.setItems(buyItems);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error updating order book: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void populateMarketList() {
        populateMarketList(currentShowAll, currentSearchFilter); // Use current state
    }

    private void populateMarketList(boolean showAll) {
        populateMarketList(showAll, "");
    }

    private void populateMarketList(boolean showAll, String searchFilter) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Always fetch base prices in USDT from server and convert locally
                    Map<String, Double> prices = RmiMarketData.getAllPrices();
                    Platform.runLater(() -> {
                        ObservableList<HBox> items = FXCollections.observableArrayList();
                        
                        List<String> symbolsToShow;
                        if (showAll) {
                            // Show all available symbols
                            symbolsToShow = new ArrayList<>(prices.keySet());
                            // Sort alphabetically
                            symbolsToShow.sort(String::compareToIgnoreCase);
                        } else {
                            // Show user favorites, fallback to popular if no favorites
                            if (!userFavorites.isEmpty()) {
                                symbolsToShow = new ArrayList<>(userFavorites);
                                // Sort alphabetically
                                symbolsToShow.sort(String::compareToIgnoreCase);
                            } else {
                                // Default popular symbols if no user favorites
                                symbolsToShow = Arrays.asList("BTC", "ETH", "BNB", "ADA", "XRP", "SOL", "DOT", "DOGE", "AVAX", "LTC");
                            }
                        }
                        
                        // Apply search filter if provided
                        if (!searchFilter.isEmpty()) {
                            final String filter = searchFilter.toLowerCase();
                            symbolsToShow = symbolsToShow.stream()
                                .filter(sym -> sym.toLowerCase().contains(filter))
                                .collect(Collectors.toList());
                        }
                        
                        for (String sym : symbolsToShow) {
                            if (prices.containsKey(sym)) {
                                final String symbol = sym;
                                HBox item = new HBox();
                                item.setPadding(new Insets(8, 16, 8, 16));
                                item.setSpacing(8);
                                item.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(37,70,50,0.1); -fx-border-width: 0 0 1 0;");
                                item.setAlignment(Pos.CENTER_LEFT);

                                // Favorite star button
                                Label starLabel = new Label(userFavorites.contains(sym) ? "★" : "☆");
                                starLabel.setStyle("-fx-text-fill: " + (userFavorites.contains(sym) ? "#ffd700" : "rgba(255,255,255,0.3)") + "; -fx-font-size: 16px; -fx-cursor: hand;");
                                starLabel.setOnMouseClicked(e -> {
                                    toggleFavorite(sym);
                                    starLabel.setText(userFavorites.contains(sym) ? "★" : "☆");
                                    starLabel.setStyle("-fx-text-fill: " + (userFavorites.contains(sym) ? "#ffd700" : "rgba(255,255,255,0.3)") + "; -fx-font-size: 16px; -fx-cursor: hand;");
                                    e.consume(); // Prevent triggering the row click
                                });

                                Label pairLabel = new Label(sym);
                                pairLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                                pairLabel.setPrefWidth(80);

                                Region spacer = new Region();
                                HBox.setHgrow(spacer, Priority.ALWAYS);

                                Label priceLabel = new Label(formatCurrency(prices.get(sym)));
                                priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                                priceLabel.setPrefWidth(100);

                                Label changeLabel = new Label("..."); // Loading indicator
                                changeLabel.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 14px;");
                                changeLabel.setPrefWidth(80);

                                item.getChildren().addAll(starLabel, pairLabel, spacer, priceLabel, changeLabel);

                                // Add click handler to select symbol
                                item.setOnMouseClicked(e -> selectSymbol(sym));

                                items.add(item);
                                
                                // Fetch 24h ticker data asynchronously for each symbol
                                Task<Void> tickerTask = new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        try {
                                            // fetch raw USDT ticker and convert client-side
                                            Map<String, String> ticker24h = RmiMarketData.get24hTicker(symbol);
                                            String changePercent = ticker24h.get("priceChangePercent");
                                            if (changePercent != null && !changePercent.isEmpty()) {
                                                double change = Double.parseDouble(changePercent);
                                                Platform.runLater(() -> {
                                                    String color = change >= 0 ? "#36e27b" : "#ff5252";
                                                    String sign = change >= 0 ? "+" : "";
                                                    changeLabel.setText(String.format("%s%.2f%%", sign, change));
                                                    changeLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
                                                });
                                            }
                                        } catch (Exception e) {
                                            Platform.runLater(() -> {
                                                changeLabel.setText("N/A");
                                                changeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 14px;");
                                            });
                                        }
                                        return null;
                                    }
                                };
                                new Thread(tickerTask).start();
                            }
                        }
                        marketListView.setItems(items);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("Error loading market data: " + e.getMessage()));
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void selectSymbol(String symbol) {
        tickerSymbolLabel.setText(baseSymbol(symbol));
        currentSymbol = symbol + "USDT";
        // Update ticker with real data
        updateTicker(symbol);
        // Update order book
        updateOrderBook(currentSymbol);
        // Start realtime chart
        startRealtimeChart(currentSymbol);
        // Load historical data
        loadHistoricalData(symbol);
        // update header favorite star to reflect current selection
        try {
            if (headerFavoriteLabel != null) {
                String base = baseSymbol(currentSymbol);
                boolean fav = userFavorites.contains(base);
                headerFavoriteLabel.setText(fav ? "★" : "☆");
                headerFavoriteLabel.setStyle("-fx-text-fill: " + (fav ? "#ffd700" : "rgba(255,255,255,0.3)") + "; -fx-font-size: 18px; -fx-cursor: hand;");
            }
        } catch (Exception ignored) {}
    }

    private void updateTicker(String symbol) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // fetch raw USDT values from server; apply client-side conversion
                    double price = RmiMarketData.getCurrentPrice(symbol, null);
                    Map<String, String> ticker24h = RmiMarketData.get24hTicker(symbol);
                    Platform.runLater(() -> {
                        tickerPriceLabel.setText(formatCurrency(price));
                        tickerApproxLabel.setText("≈ " + formatCurrency(price));

                        if (ticker24h.containsKey("priceChangePercent")) {
                            double changePct = Double.parseDouble(ticker24h.get("priceChangePercent"));
                            change24hLabel.setText(String.format("%.2f%%", changePct));
                            change24hLabel.setStyle(changePct >= 0 ?
                                "-fx-text-fill: #36e27b; -fx-font-size: 14px; -fx-font-weight: bold;" :
                                "-fx-text-fill: #ff4757; -fx-font-size: 14px; -fx-font-weight: bold;");
                        }

                        if (ticker24h.containsKey("highPrice")) {
                            double h = Double.parseDouble(ticker24h.get("highPrice"));
                            high24hLabel.setText(formatCurrency(h));
                        }

                        if (ticker24h.containsKey("lowPrice")) {
                            double l = Double.parseDouble(ticker24h.get("lowPrice"));
                            low24hLabel.setText(formatCurrency(l));
                        }

                        if (ticker24h.containsKey("volume")) {
                            double vol = Double.parseDouble(ticker24h.get("volume"));
                            vol24hLabel.setText(formatVolume(vol));
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("Error updating ticker: " + e.getMessage()));
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private String formatVolume(double vol) {
        if (vol >= 1e9) return String.format("%.2fB", vol / 1e9);
        if (vol >= 1e6) return String.format("%.2fM", vol / 1e6);
        if (vol >= 1e3) return String.format("%.2fK", vol / 1e3);
        return String.format("%.2f", vol);
    }

    private void loadHistoricalData(String symbol) {
        Task<Void> loadHistory = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Load 1D historical data for chart display (server may return values converted to selected currency)
                    // Request raw USDT historical data and convert on client side for consistent chart scaling
                    List<StockData> data = RmiMarketData.getHistoricalData(symbol, "1d", 100);
                    Platform.runLater(() -> updateChartWithHistoricalData(data));
                } catch (Exception ex) {
                    Platform.runLater(() -> debugArea.appendText("Failed to load historical data: " + ex.getMessage() + "\n"));
                }
                return null;
            }
        };
        new Thread(loadHistory).start();
    }

    private void updateChartWithHistoricalData(List<StockData> data) {
        priceChartField.getData().clear();
        volumeChartField.getData().clear();
        closes.clear();

        XYChart.Series<String, Number> candleSeries = new XYChart.Series<>();
        candleSeries.setName("Candles");
        XYChart.Series<String, Number> sma3Series = new XYChart.Series<>();
        sma3Series.setName("SMA 3");
        XYChart.Series<String, Number> sma5Series = new XYChart.Series<>();
        sma5Series.setName("SMA 5");
        XYChart.Series<String, Number> volumeSeries = new XYChart.Series<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (StockData d : data) {
            String label = d.getDate().atZone(java.time.ZoneId.of("UTC")).toLocalDateTime().format(fmt);
            double rawClose = d.getClose();
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(label, rawClose);
            final double fOpen = d.getOpen(), fClose = d.getClose(), fHigh = d.getHigh(), fLow = d.getLow();
            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Candle candle = new Candle(fOpen, fClose, fHigh, fLow);
                    StackPane container = (StackPane) newNode;
                    container.getChildren().clear();
                    container.getChildren().add(candle);
                    Platform.runLater(() -> updateCandlesFor(priceChartField));
                }
            });
            candleSeries.getData().add(dataPoint);
            closes.add(rawClose);
            volumeSeries.getData().add(new XYChart.Data<>(label, d.getVolume()));
        }

        // Calculate SMAs
        for (int i = 0; i < data.size(); i++) {
            String label = data.get(i).getDate().atZone(java.time.ZoneId.of("UTC")).toLocalDateTime().format(fmt);
            if (i >= 2) {
                double sma3 = (closes.get(i-2) + closes.get(i-1) + closes.get(i)) / 3.0;
                sma3Series.getData().add(new XYChart.Data<>(label, sma3));
            }
            if (i >= 4) {
                double sma5 = (closes.get(i-4) + closes.get(i-3) + closes.get(i-2) + closes.get(i-1) + closes.get(i)) / 5.0;
                sma5Series.getData().add(new XYChart.Data<>(label, sma5));
            }
        }

        priceChartField.getData().addAll(candleSeries, sma3Series, sma5Series);
        volumeChartField.getData().add(volumeSeries);

        // wire SMA class names for CSS
        for (XYChart.Series<String, Number> s : priceChartField.getData()) {
            final String nm = s.getName() == null ? "" : s.getName();
            s.nodeProperty().addListener((obs, oldN, newN) -> {
                if (newN != null) {
                    if (nm.contains("SMA 3")) newN.getStyleClass().add("sma3");
                    if (nm.contains("SMA 5")) newN.getStyleClass().add("sma5");
                }
            });
        }

        // set Y-axis bounds based on raw USDT data scaled to selected currency so charts match across currencies
        try {
            NumberAxis y = (NumberAxis) priceChartField.getYAxis();
                double minRaw = Double.MAX_VALUE, maxRaw = Double.MIN_VALUE;
                for (StockData sd : data) {
                    double v = sd.getClose();
                    if (v < minRaw) minRaw = v;
                    if (v > maxRaw) maxRaw = v;
                }
                double lower = Math.max(0, minRaw - (maxRaw - minRaw) * 0.12);
                double upper = maxRaw + (maxRaw - minRaw) * 0.12;
                y.setAutoRanging(false);
                y.setLowerBound(lower);
                y.setUpperBound(upper);
                y.setTickUnit((upper - lower) / 8.0);
                // show converted tick labels (ETB or USDT) but keep numeric axis values in USDT so shape stays identical
                y.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                    @Override public String toString(Number object) { return formatCurrency(object.doubleValue()); }
                    @Override public Number fromString(String string) { try { return priceFmt.parse(string.replaceAll("[^0-9.,-]","")); } catch (Exception e) { return 0; } }
                });
        } catch (Exception ignored) {}

        updateCandlesFor(priceChartField);
    }

    private void updateSMAs(String label) {
        if (closes.size() >= 3) {
            double sma3 = (closes.get(closes.size()-3) + closes.get(closes.size()-2) + closes.get(closes.size()-1)) / 3.0;
            XYChart.Series<String, Number> sma3Series = (XYChart.Series<String, Number>) priceChartField.getData().get(1);
            sma3Series.getData().add(new XYChart.Data<>(label, sma3));
            if (sma3Series.getData().size() > 300) sma3Series.getData().remove(0);
        }
        if (closes.size() >= 5) {
            double sma5 = (closes.get(closes.size()-5) + closes.get(closes.size()-4) + closes.get(closes.size()-3) + closes.get(closes.size()-2) + closes.get(closes.size()-1)) / 5.0;
            XYChart.Series<String, Number> sma5Series = (XYChart.Series<String, Number>) priceChartField.getData().get(2);
            sma5Series.getData().add(new XYChart.Data<>(label, sma5));
            if (sma5Series.getData().size() > 300) sma5Series.getData().remove(0);
        }
    }

    private void applyChartTheme(LineChart<String, Number> priceChart, BarChart<String, Number> volChart) {
        String bgColor = darkMode ? "#112117" : "#ffffff";
        String textColor = darkMode ? "white" : "black";
        String gridColor = darkMode ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.1)";

        priceChart.setStyle("-fx-background-color: " + bgColor + ";");
        volChart.setStyle("-fx-background-color: " + bgColor + ";");

        // You can add more theming here if needed
    }

    private void startRealtimeChart(String symbol) {
        // Close existing realtime updater
        try { if (realtimeUpdater != null) realtimeUpdater.shutdownNow(); } catch (Exception ignored) {}
        realtimeUpdater = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        currentSymbol = symbol;
        // Ensure server is subscribed and then poll latest kline every 3 seconds
        realtimeUpdater.scheduleAtFixedRate(() -> {
            try {
                final String pair = symbol.toUpperCase().endsWith("USDT") ? symbol.toUpperCase() : symbol.toUpperCase() + "USDT";
                com.stockapp.model.StockData k = com.stockapp.rmi.RmiMarketData.getLatestKline(pair, "1d");
                if (k == null) return;
                long start = k.getDate() == null ? System.currentTimeMillis() : k.getDate().atZone(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli();
                double open = k.getOpen(); double high = k.getHigh(); double low = k.getLow(); double close = k.getClose(); double vol = k.getVolume();
                String sym = pair;
                if (sym.equals(currentSymbol)) {
                    Platform.runLater(() -> {
                        try {
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
                            String label = java.time.Instant.ofEpochMilli(start).atZone(java.time.ZoneId.of("UTC")).toLocalDateTime().format(fmt);
                            double rawOpen = open;
                            double rawHigh = high;
                            double rawLow = low;
                            double rawClose = close;
                            // convert prices to display currency if needed
                            final double rate = (displayCurrency != null && !"USDT".equals(displayCurrency)) ? usdToFiatRate : 1.0;
                            final double convOpen = rawOpen * rate;
                            final double convHigh = rawHigh * rate;
                            final double convLow = rawLow * rate;
                            final double convClose = rawClose * rate;
                            if (priceChartField.getData().size() > 0) {
                                XYChart.Series<String, Number> candleSeries = (XYChart.Series<String, Number>) priceChartField.getData().get(0);
                                if (candleSeries != null) {
                                    if (!candleSeries.getData().isEmpty()) {
                                        XYChart.Data<String, Number> lastCandleData = candleSeries.getData().get(candleSeries.getData().size() - 1);
                                        if (lastCandleData.getXValue().equals(label)) {
                                            Node node = lastCandleData.getNode();
                                            if (node instanceof StackPane) {
                                                StackPane container = (StackPane) node;
                                                container.getChildren().clear();
                                                Candle newCandle = new Candle(convOpen, convClose, convHigh, convLow);
                                                container.getChildren().add(newCandle);
                                                Platform.runLater(() -> updateCandlesFor(priceChartField));
                                            }
                                        } else {
                                            XYChart.Data<String, Number> newData = new XYChart.Data<>(label, rawClose);
                                            final double fOpen = rawOpen, fClose = rawClose, fHigh = rawHigh, fLow = rawLow;
                                            newData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                                if (newNode != null) {
                                                    Candle candle = new Candle(fOpen, fClose, fHigh, fLow);
                                                    StackPane container = (StackPane) newNode;
                                                    container.getChildren().clear();
                                                    container.getChildren().add(candle);
                                                    Platform.runLater(() -> updateCandlesFor(priceChartField));
                                                }
                                            });
                                            candleSeries.getData().add(newData);
                                            closes.add(rawClose);
                                            updateSMAs(label);
                                            if (candleSeries.getData().size() > 300) candleSeries.getData().remove(0);
                                        }
                                    } else {
                                        XYChart.Data<String, Number> newData = new XYChart.Data<>(label, rawClose);
                                        final double fOpen = rawOpen, fClose = rawClose, fHigh = rawHigh, fLow = rawLow;
                                        newData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                            if (newNode != null) {
                                                Candle candle = new Candle(fOpen, fClose, fHigh, fLow);
                                                StackPane container = (StackPane) newNode;
                                                container.getChildren().clear();
                                                container.getChildren().add(candle);
                                                Platform.runLater(() -> updateCandlesFor(priceChartField));
                                            }
                                        });
                                        candleSeries.getData().add(newData);
                                        closes.add(rawClose);
                                        updateSMAs(label);
                                    }
                                }
                            }
                            try {
                                if (tickerPriceLabel != null) tickerPriceLabel.setText(formatCurrency(close));
                                if (lastPriceLabel != null) lastPriceLabel.setText(formatCurrency(close));
                            } catch (Exception ignored) {}
                        } catch (Exception ignored) {}
                    });
                }
            } catch (Exception ignored) {}
        }, 0, 3, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Override
    public void start(Stage stage) {
        // Prompt user for server IP and initialize RMI client
        String lastHost = Preferences.userNodeForPackage(StockApp.class).get("server.ip", "localhost");
        TextInputDialog dlg = new TextInputDialog(lastHost);
        dlg.setTitle("Connect to server");
        dlg.setHeaderText("Enter the RMI server IP address");
        dlg.setContentText("Server IP:");
        java.util.Optional<String> res = dlg.showAndWait();
        if (!res.isPresent() || res.get().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "No server IP provided. Exiting.");
            Platform.exit();
            return;
        }
        String host = res.get().trim();
        Preferences.userNodeForPackage(StockApp.class).put("server.ip", host);
        try {
            RmiMarketData.init(host, 1099);
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, "RMI server not available: " + ex.getMessage());
            Platform.exit();
            return;
        }

        INSTANCE = this;
        MAIN_STAGE = stage;

        // Load user favorites
        loadFavorites();

        // Create the main layout
        BorderPane root = new BorderPane();

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Main content: HBox with left sidebar, center, right sidebar
        HBox mainContent = new HBox();
        VBox leftSidebar = createLeftSidebar();
        VBox centerArea = createCenterArea();
        VBox rightSidebar = createRightSidebar();
        mainContent.getChildren().addAll(leftSidebar, centerArea, rightSidebar);
        HBox.setHgrow(centerArea, Priority.ALWAYS);
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1400, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/style-dark.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CryptoMarket");
        stage.show();

        // Populate initial data
        populateMarketList();
        selectSymbol("BTC");
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(16));
        header.setSpacing(24);
        header.setStyle("-fx-background-color: #162a1f; -fx-border-color: #254632; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo and title
        HBox logoBox = new HBox();
        logoBox.setSpacing(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        // Logo SVG placeholder, using a label for now
        Label logo = new Label("▲");
        logo.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 32px;");
        Label title = new Label("CryptoMarket");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        logoBox.getChildren().addAll(logo, title);

        // Separator
        Region sep1 = new Region();
        sep1.setPrefWidth(6);
        sep1.setStyle("-fx-background-color: #254632;");

        // Nav links
        HBox nav = new HBox();
        nav.setSpacing(16);
        nav.setAlignment(Pos.CENTER_LEFT);
        Label markets = new Label("Markets");
        markets.setStyle("-fx-text-fill: #36e27b; -fx-font-weight: bold; -fx-font-size: 14px;");
        nav.getChildren().addAll(markets);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search
        HBox searchBox = new HBox();
        searchBox.setSpacing(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 16, 0, 12));
        searchBox.setStyle("-fx-background-color: #0e1a14; -fx-border-color: #254632; -fx-border-radius: 24px;");
        Label searchIcon = new Label("search");
        searchIcon.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 20px;");
        TextField searchField = new TextField();
        searchField.setPromptText("Search (Cmd+K)");
        searchField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: transparent;");
        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSearchFilter = newValue != null ? newValue : "";
            populateMarketList(currentShowAll, currentSearchFilter);
        });
        searchBox.getChildren().addAll(searchIcon, searchField);

        // Currency selector (USDT / ETB)
        ComboBox<String> currencyCombo = new ComboBox<>(FXCollections.observableArrayList("USDT", "ETB"));
        currencyCombo.setValue(displayCurrency);
        currencyCombo.setPrefWidth(84);
        currencyCombo.setStyle("-fx-background-color: #0e1a14; -fx-text-fill: white;");
        currencyCombo.setOnAction(evt -> {
            String sel = currencyCombo.getValue();
            if (sel == null) sel = "USDT";
            displayCurrency = sel;
            if ("ETB".equalsIgnoreCase(sel)) {
                // fetch current ETB rate synchronously so UI updates immediately
                fetchAndSetUsdToFiatRate("ETB");
                startFiatRateUpdater();
            } else {
                stopFiatRateUpdater();
                usdToFiatRate = 1.0;
            }
            refreshCurrencyDependentUI();
            // also update any open detail windows' orderbook displays (best-effort)
            synchronized (DETAIL_WINDOWS) {
                for (DetailWindow dw : DETAIL_WINDOWS) {
                    try { applyChartTheme(dw.priceChart, dw.volChart); } catch (Exception ignored) {}
                }
            }
        });

        header.getChildren().addAll(logoBox, sep1, nav, spacer, searchBox, currencyCombo);
        return header;
    }

    private VBox createLeftSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(288);
        sidebar.setStyle("-fx-background-color: #162a1f; -fx-border-color: #254632; -fx-border-width: 0 1 0 0;");

        // Tabs
        HBox tabs = new HBox();
        tabs.setStyle("-fx-border-color: #254632; -fx-border-width: 0 0 1 0;");
        Button favoritesBtn = new Button("Favorites");
        favoritesBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-color: #36e27b; -fx-border-width: 0 0 2 0;");
        Button allBtn = new Button("All");
        allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 14px;");
        
        favoritesBtn.setOnAction(e -> {
            // Switch to favorites/popular view
            favoritesBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-color: #36e27b; -fx-border-width: 0 0 2 0;");
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 14px;");
            currentShowAll = false;
            populateMarketList(currentShowAll, currentSearchFilter);
        });
        allBtn.setOnAction(e -> {
            // Switch to all cryptocurrencies view
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-color: #36e27b; -fx-border-width: 0 0 2 0;");
            favoritesBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 14px;");
            currentShowAll = true;
            populateMarketList(currentShowAll, currentSearchFilter);
        });
        allBtn.setOnMouseEntered(e -> {
            if (!allBtn.getStyle().contains("-fx-border-color")) {
                allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
            }
        });
        allBtn.setOnMouseExited(e -> {
            if (!allBtn.getStyle().contains("-fx-border-color")) {
                allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 14px;");
            }
        });
        tabs.getChildren().addAll(favoritesBtn, allBtn);
        HBox.setHgrow(allBtn, Priority.ALWAYS);

        // Filters (removed USDT, BTC, ETH buttons)
        HBox filters = new HBox();
        filters.setPadding(new Insets(12));
        filters.setSpacing(8);
        // No buttons added

        // List Header
        HBox listHeader = new HBox();
        listHeader.setPadding(new Insets(8, 16, 4, 16));
        Label pairLabel = new Label("Pair");
        pairLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Label priceLabel = new Label("Price");
        priceLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
        Region spacer2 = new Region();
        spacer2.setPrefWidth(16);
        Label changeLabel = new Label("24h%");
        changeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
        listHeader.getChildren().addAll(pairLabel, spacer1, priceLabel, spacer2, changeLabel);

        // List
        marketListView = new ListView<>();
        marketListView.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(marketListView, Priority.ALWAYS);

        sidebar.getChildren().addAll(tabs, filters, listHeader, marketListView);
        return sidebar;
    }

    private VBox createCenterArea() {
        VBox center = new VBox();
        center.setStyle("-fx-background-color: #112117;");

        // Ticker Header
        HBox tickerHeader = new HBox();
        tickerHeader.setPadding(new Insets(16));
        tickerHeader.setSpacing(24);
        tickerHeader.setStyle("-fx-background-color: #162a1f; -fx-border-color: #254632; -fx-border-width: 0 0 1 0;");
        tickerHeader.setAlignment(Pos.CENTER_LEFT);

        HBox symbolBox = new HBox();
        symbolBox.setSpacing(8);
        symbolBox.setAlignment(Pos.CENTER_LEFT);
        // Header favorite star for current symbol
        headerFavoriteLabel = new Label(userFavorites.contains(baseSymbol(currentSymbol)) ? "★" : "☆");
        headerFavoriteLabel.setStyle("-fx-text-fill: " + (userFavorites.contains(baseSymbol(currentSymbol)) ? "#ffd700" : "rgba(255,255,255,0.3)") + "; -fx-font-size: 18px; -fx-cursor: hand;");
        headerFavoriteLabel.setOnMouseClicked(evt -> {
            try {
                String base = baseSymbol(currentSymbol);
                if (base != null && !base.isEmpty()) {
                    toggleFavorite(base);
                    boolean fav = userFavorites.contains(base);
                    headerFavoriteLabel.setText(fav ? "★" : "☆");
                    headerFavoriteLabel.setStyle("-fx-text-fill: " + (fav ? "#ffd700" : "rgba(255,255,255,0.3)") + "; -fx-font-size: 18px; -fx-cursor: hand;");
                }
            } catch (Exception ignored) {}
        });

        tickerSymbolLabel = new Label("ETH/USDT");
        tickerSymbolLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label perp = new Label("PERP");
        perp.setStyle("-fx-background-color: #0e1a14; -fx-border-color: #254632; -fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px; -fx-padding: 2 6;");
        symbolBox.getChildren().addAll(headerFavoriteLabel, tickerSymbolLabel, perp);

        VBox priceBox = new VBox();
        priceBox.setAlignment(Pos.CENTER_LEFT);
        tickerPriceLabel = new Label(formatCurrency(42150.00));
        tickerPriceLabel.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 20px; -fx-font-weight: bold;");
        tickerApproxLabel = new Label("≈ $42,150.00");
        tickerApproxLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
        priceBox.getChildren().addAll(tickerPriceLabel, tickerApproxLabel);

        // Change boxes
        VBox change24Box = new VBox();
        change24Box.setAlignment(Pos.CENTER_LEFT);
        change24Box.setPadding(new Insets(0, 0, 0, 16));
        change24Box.setStyle("-fx-border-color: rgba(37,70,50,0.3); -fx-border-width: 0 0 0 1;");
        Label changeLabel = new Label("24h Change");
        changeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-text-transform: uppercase;");
        change24hLabel = new Label("+3.25%");
        change24hLabel.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 14px; -fx-font-weight: bold;");
        change24Box.getChildren().addAll(changeLabel, change24hLabel);

        VBox high24Box = new VBox();
        high24Box.setAlignment(Pos.CENTER_LEFT);
        high24Box.setPadding(new Insets(0, 0, 0, 16));
        high24Box.setStyle("-fx-border-color: rgba(37,70,50,0.3); -fx-border-width: 0 0 0 1;");
        Label highLabel = new Label("24h High");
        highLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-text-transform: uppercase;");
        high24hLabel = new Label("42,800.00");
        high24hLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        high24Box.getChildren().addAll(highLabel, high24hLabel);

        VBox low24Box = new VBox();
        low24Box.setAlignment(Pos.CENTER_LEFT);
        low24Box.setPadding(new Insets(0, 0, 0, 16));
        low24Box.setStyle("-fx-border-color: rgba(37,70,50,0.3); -fx-border-width: 0 0 0 1;");
        Label lowLabel = new Label("24h Low");
        lowLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-text-transform: uppercase;");
        low24hLabel = new Label("40,500.00");
        low24hLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        low24Box.getChildren().addAll(lowLabel, low24hLabel);

        VBox vol24Box = new VBox();
        vol24Box.setAlignment(Pos.CENTER_LEFT);
        vol24Box.setPadding(new Insets(0, 0, 0, 16));
        vol24Box.setStyle("-fx-border-color: rgba(37,70,50,0.3); -fx-border-width: 0 0 0 1;");
        Label volLabel = new Label("24h Vol(USDT)");
        volLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-text-transform: uppercase;");
        vol24hLabel = new Label("1.25B");
        vol24hLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        vol24Box.getChildren().addAll(volLabel, vol24hLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        tickerHeader.getChildren().addAll(symbolBox, priceBox, change24Box, high24Box, low24Box, vol24Box, spacer);

        // Chart Controls (fixed 1D view)
        HBox chartControls = new HBox();
        chartControls.setPadding(new Insets(8, 16, 8, 16));
        chartControls.setSpacing(8);
        chartControls.setStyle("-fx-background-color: rgba(14,26,20,0.3); -fx-border-color: #254632; -fx-border-width: 0 0 1 0;");
        chartControls.setAlignment(Pos.CENTER_LEFT);
        Label fixedInterval = new Label("1D");
        fixedInterval.setStyle("-fx-text-fill: #36e27b; -fx-font-weight: bold; -fx-font-size: 12px;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        chartControls.getChildren().addAll(fixedInterval, spacer2);

        // Chart Area
        StackPane chartArea = new StackPane();
        chartArea.setStyle("-fx-background-color: #112117;");
        VBox.setVgrow(chartArea, Priority.ALWAYS);

        // Set background image for chart area
        try {
            Image backgroundImage = new Image(CHART_BACKGROUND_IMAGE);
            BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
            );
            chartArea.setBackground(new Background(bgImage));
        } catch (Exception e) {
            // Fallback to solid color if image fails to load
            chartArea.setStyle("-fx-background-color: #112117;");
        }

        // Create the price chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        priceChartField = new LineChart<>(xAxis, yAxis);
        priceChartField.setAnimated(false);
        priceChartField.setCreateSymbols(false);
        priceChartField.setPrefSize(760, 460);
        priceChartField.setStyle("-fx-background-color: #112117;");

        // Create volume chart
        CategoryAxis volXAxis = new CategoryAxis();
        NumberAxis volYAxis = new NumberAxis();
        volumeChartField = new BarChart<>(volXAxis, volYAxis);
        volumeChartField.setAnimated(false);
        volumeChartField.setLegendVisible(false);
        volumeChartField.setPrefHeight(140);
        volumeChartField.setStyle("-fx-background-color: #112117;");

        VBox chartsVBox = new VBox(6, priceChartField, volumeChartField);
        chartArea.getChildren().add(chartsVBox);

        // expose center charts container and a reusable center message for state updates
        this.centerCharts = chartsVBox;
        this.centerMessage = new Label("");
        this.centerMessage.getStyleClass().add("center-message");

        center.getChildren().addAll(tickerHeader, chartControls, chartArea);
        return center;
    }

    // Change chart timeframe and refresh data/styles
    private void changeChartTimeframe(String interval, Button m1, Button m5, Button m15, Button h1, Button h4, Button d1) {
        try {
            Button[] all = new Button[] { m1, m5, m15, h1, h4, d1 };
            for (Button b : all) {
                if (b != null) b.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
            }
            Button sel = null;
            switch (interval) {
                case "1m": sel = m1; break;
                case "5m": sel = m5; break;
                case "15m": sel = m15; break;
                case "1h": sel = h1; break;
                case "4h": sel = h4; break;
                case "1d": sel = d1; break;
            }
            if (sel != null) sel.setStyle("-fx-background-color: #36e27b; -fx-text-fill: #0e1a14; -fx-font-size: 12px; -fx-font-weight: bold;");

            // reload historical data for current symbol with chosen interval
            String base = baseSymbol(currentSymbol);
            if (base == null || base.isEmpty()) return;
            // choose a reasonable limit for display
            int limit = interval.endsWith("m") ? 200 : 200;
            fetchAndShow(base, chooseApiKey(), interval, limit);
        } catch (Exception ignored) {}
    }

    private VBox createRightSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(320);
        sidebar.setStyle("-fx-background-color: #162a1f; -fx-border-color: #254632; -fx-border-width: 0 0 0 1;");

        // Order Book Header
        HBox obHeader = new HBox();
        obHeader.setPadding(new Insets(8, 16, 8, 16));
        obHeader.setStyle("-fx-border-color: #254632; -fx-border-width: 0 0 1 0;");
        Label obTitle = new Label("Order Book");
        obTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        obHeader.getChildren().add(obTitle);

        // Order Book Header Labels
        HBox obLabels = new HBox();
        obLabels.setPadding(new Insets(4, 16, 2, 16));
        Label priceLabel = new Label("Price(USDT)");
        priceLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Label amountLabel = new Label("Amount(Quantity)");
        amountLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
        Region spacer2 = new Region();
        spacer2.setPrefWidth(16);
        Label totalLabel = new Label("Total");
        totalLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-transform: uppercase;");
        obLabels.getChildren().addAll(priceLabel, spacer1, amountLabel, spacer2, totalLabel);

        // Sells List
        sellOrdersList = new ListView<>();
        sellOrdersList.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(sellOrdersList, Priority.ALWAYS);

        // Last Price
        HBox lastPriceBox = new HBox();
        lastPriceBox.setPadding(new Insets(8, 16, 8, 16));
        lastPriceBox.setSpacing(8);
        lastPriceBox.setStyle("-fx-background-color: #0e1a14; -fx-border-color: #254632; -fx-border-width: 1 0;");
        lastPriceBox.setAlignment(Pos.CENTER);
        lastPriceLabel = new Label("--");
        lastPriceLabel.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label arrow = new Label("↑");
        arrow.setStyle("-fx-text-fill: #36e27b; -fx-font-size: 16px;");
        Label approxPrice = new Label("--");
        approxPrice.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
        lastPriceBox.getChildren().addAll(lastPriceLabel, arrow, approxPrice);

        // Buys List
        buyOrdersList = new ListView<>();
        buyOrdersList.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(buyOrdersList, Priority.ALWAYS);

        sidebar.getChildren().addAll(obHeader, obLabels, sellOrdersList, lastPriceBox, buyOrdersList);
        return sidebar;
    }

    // Called from SettingsDialog to apply theme centrally
    public static void applyThemeToApp(String theme) {
        if (INSTANCE != null) INSTANCE.applyAppTheme(theme);
    }

    private void applyAppTheme(String theme) {
        try {
            if (MAIN_STAGE == null) return;
            Scene sc = MAIN_STAGE.getScene();
            if (sc == null) return;
            sc.getStylesheets().clear();
            sc.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            switch (theme.toLowerCase()) {
                case "dark":
                    sc.getStylesheets().add(getClass().getResource("/style-dark.css").toExternalForm());
                    darkMode = true;
                    break;
                case "light":
                    sc.getStylesheets().add(getClass().getResource("/style-light.css").toExternalForm());
                    darkMode = false;
                    break;
                case "solarized":
                    sc.getStylesheets().add(getClass().getResource("/style-solarized.css").toExternalForm());
                    darkMode = false;
                    break;
                case "highcontrast":
                case "high-contrast":
                    sc.getStylesheets().add(getClass().getResource("/style-highcontrast.css").toExternalForm());
                    darkMode = true;
                    break;
                case "ocean":
                case "nightblue":
                    sc.getStylesheets().add(getClass().getResource("/style-nightblue.css").toExternalForm());
                    darkMode = true;
                    break;
                default:
                    sc.getStylesheets().add(getClass().getResource("/style-light.css").toExternalForm());
                    darkMode = false;
            }
            // re-apply chart theming to reflect darkMode
            applyChartTheme(priceChartField, volumeChartField);
            // clear known inline styles so CSS themes can take effect on market/console
            try {
                Scene scn = MAIN_STAGE.getScene();
                if (scn != null) {
                    Node pb = scn.lookup("#popularBar");
                    if (pb != null) pb.setStyle("");
                    Node mb = scn.lookup("#marketBox");
                    if (mb != null) mb.setStyle("");
                    Node br = scn.lookup("#brandLabel");
                    if (br != null) br.setStyle("");
                    Node da = scn.lookup("#debugArea");
                    if (da != null) da.setStyle("");
                }
            } catch (Exception ignored) {}
            // subtle fade to smooth theme swap
            try {
                Scene scn = MAIN_STAGE.getScene();
                if (scn != null && scn.getRoot() != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(220), scn.getRoot());
                    ft.setFromValue(0.92);
                    ft.setToValue(1.0);
                    ft.play();
                }
            } catch (Exception ignored) {}

            // persist theme choice
            try {
                Preferences prefs = Preferences.userNodeForPackage(StockApp.class);
                prefs.put("app.theme", theme);
            } catch (Exception ignored) {}

            // update any open detail windows
            synchronized (DETAIL_WINDOWS) {
                for (DetailWindow dw : DETAIL_WINDOWS) {
                    try {
                        Scene ds = dw.stage.getScene();
                        if (ds != null) {
                            ds.getStylesheets().clear();
                            ds.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                            if (darkMode) ds.getStylesheets().add(getClass().getResource("/style-dark.css").toExternalForm());
                            else ds.getStylesheets().add(getClass().getResource("/style-light.css").toExternalForm());
                        }
                    } catch (Exception ignored) {}
                    try { applyChartTheme(dw.priceChart, dw.volChart); } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    public static void registerDetailWindow(Stage s, LineChart<String, Number> pc, BarChart<String, Number> vc) {
        DETAIL_WINDOWS.add(new DetailWindow(s, pc, vc));
    }

    public static void unregisterDetailWindow(Stage s) {
        synchronized (DETAIL_WINDOWS) {
            DETAIL_WINDOWS.removeIf(dw -> dw.stage == s);
        }
    }

    

    private String chooseApiKey() {
        // No inline API key field; default to empty (CoinGecko public)
        return "";
    }

    // Return base symbol without USDT suffix (e.g., BTCUSDT -> BTC)
    private String baseSymbol(String sym) {
        if (sym == null) return "";
        String s = sym.trim().toUpperCase();
        if (s.endsWith("USDT")) return s.substring(0, s.length() - 4);
        return s;
    }

    // Convert a price value (which is in USDT) to the currently selected display currency
    private double convertPriceValue(double usdtPrice) {
        if ("ETB".equalsIgnoreCase(displayCurrency)) {
            return usdtPrice * usdToFiatRate;
        }
        return usdtPrice;
    }

    // Format a USDT price into a display string for the selected currency
    private String formatCurrency(double usdtPrice) {
        double v = convertPriceValue(usdtPrice);
        return String.format("%s %s", displayCurrency, priceFmt.format(v));
    }

    // Fetch USD->fiat rate once and update state (sync method called from background tasks)
    private void fetchAndSetUsdToFiatRate(String fiat) {
        try {
            double r = com.stockapp.rmi.RmiMarketData.getUsdToFiatRate(fiat);
            if (r > 0) usdToFiatRate = r;
        } catch (Exception e) {
            // on failure keep previous rate
            debugArea.appendText("Failed to fetch fiat rate: " + e.getMessage() + "\n");
        }
    }

    // Start periodic fiat rate refresh (every 5 minutes) when ETB selected
    private void startFiatRateUpdater() {
        stopFiatRateUpdater();
        fxRateUpdater = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        fxRateUpdater.scheduleAtFixedRate(() -> fetchAndSetUsdToFiatRate("ETB"), 0, 5, java.util.concurrent.TimeUnit.MINUTES);
    }

    private void stopFiatRateUpdater() {
        try {
            if (fxRateUpdater != null) fxRateUpdater.shutdownNow();
        } catch (Exception ignored) {}
        fxRateUpdater = null;
    }

    // Refresh UI elements that depend on selected currency
    private void refreshCurrencyDependentUI() {
        try {
            populateMarketList(currentShowAll, currentSearchFilter);
            // update popular quick prices as well
            updatePopularPrices();
            // refresh ticker and orderbook for current symbol
            String base = baseSymbol(currentSymbol);
            if (base != null && !base.isEmpty()) {
                updateTicker(base);
                updateOrderBook(currentSymbol);
                fetchAndShow(base, chooseApiKey(), "1d", 200);
            }
        } catch (Exception ignored) {}
    }

    private void fetchAndUpdatePrice(TrackedStock ts, String apiKey) {
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Platform.runLater(() -> {
                        statusLabel.setText("Fetching price for " + ts.getSymbol());
                        debugArea.appendText("[fetch] price " + ts.getSymbol() + "\n");
                    });
                    double price = RmiMarketData.getCurrentPrice(ts.getSymbol(), null);
                    Platform.runLater(() -> {
                        ts.setPrice(price);
                        statusLabel.setText("Price updated for " + ts.getSymbol());
                        Label pl = popularPriceLabels.get(ts.getSymbol());
                        if (pl != null) pl.setText(formatCurrency(price));
                        debugArea.appendText("[ok] " + ts.getSymbol() + " price=" + price + "\n");
                    });
                } catch (Exception ex) {
                    final String msg = ex.getMessage();
                    Platform.runLater(() -> {
                        statusLabel.setText("Fetch error for " + ts.getSymbol());
                        debugArea.appendText("[err] " + ts.getSymbol() + " -> " + msg + "\n");
                        showAlert(AlertType.ERROR, "Fetch error: " + msg);
                    });
                }
                return null;
            }
        };
        new Thread(t).start();
    }

    private void updatePopularPrices() {
        String key = chooseApiKey();
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() {
                for (String sym : Arrays.asList("BTC", "ETH", "SOL", "XRP", "ADA", "DOGE")) {
                    try {
                        double price = RmiMarketData.getCurrentPrice(sym, null);
                        Platform.runLater(() -> {
                            Label pl = popularPriceLabels.get(sym);
                            if (pl != null) pl.setText(formatCurrency(price));
                        });
                        Thread.sleep(5000); // 5 second delay between requests to avoid rate limiting
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                return null;
            }
        };
        new Thread(t).start();
    }

    private void fetchAndShow(String symbol, String apiKey, String interval, int limit) {
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Request raw USDT historical data and convert on client side for consistent chart scaling
                    List<StockData> data = RmiMarketData.getHistoricalData(symbol, interval, limit);
                    // no local storage/caching: if no data, show message and exit
                    if (data == null || data.isEmpty()) {
                        Platform.runLater(() -> {
                            centerCharts.getChildren().clear();
                            centerMessage.setText("No historical data available for " + symbol + " (try another symbol or check network)");
                        });
                        return null;
                    }

                    // Downsample if dataset is large to improve rendering performance
                    final int MAX_POINTS = 120; // tuned for reasonable performance
                    if (data.size() > MAX_POINTS) {
                        data = downsampleData(data, MAX_POINTS);
                        debugArea.appendText("[perf] downsampled to " + data.size() + " points\n");
                    }
                    // make a final reference for lambdas below
                    final java.util.List<StockData> finalData = data;

                    DateTimeFormatter fmt;
                    if (interval != null && (interval.endsWith("m") || interval.contains("h"))) {
                        fmt = DateTimeFormatter.ofPattern("HH:mm");
                    } else {
                        fmt = DateTimeFormatter.ofPattern("MM-dd");
                    }

                    // Build candlestick nodes and SMA lines
                    XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
                    priceSeries.setName(symbol + " Close");
                    XYChart.Series<String, Number> sma3 = new XYChart.Series<>();
                    sma3.setName("SMA 3");
                    XYChart.Series<String, Number> volSeries = new XYChart.Series<>();
                    volSeries.setName(symbol + " Volume");

                    List<Double> closes = new java.util.ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        StockData sd = data.get(i);
                        String label = sd.getDate().format(fmt);
                        // price point for line overlay (we'll hide it if using candlesticks)
                        double rawClose = sd.getClose();
                        double rawOpen = sd.getOpen();
                        double rawHigh = sd.getHigh();
                        double rawLow = sd.getLow();
                        priceSeries.getData().add(new XYChart.Data<>(label, rawClose));
                        closes.add(rawClose);
                        volSeries.getData().add(new XYChart.Data<>(label, sd.getVolume()));

                        // SMA 3
                        if (i >= 2) {
                            double sum = 0; int cnt = 0;
                            for (int j = i-2; j <= i; j++) { sum += closes.get(j); cnt++; }
                            sma3.getData().add(new XYChart.Data<>(label, sum / cnt));
                        } else {
                            sma3.getData().add(new XYChart.Data<>(label, null));
                        }
                        // SMA-5 removed
                    }

                    // Create candlestick series (one series where each data has a Candle node)
                    XYChart.Series<String, Number> candleSeries = new XYChart.Series<>();
                    candleSeries.setName(symbol + " Candles");
                    for (int i = 0; i < data.size(); i++) {
                        StockData sd = data.get(i);
                        String label = sd.getDate().format(fmt);
                        XYChart.Data<String, Number> d = new XYChart.Data<>(label, sd.getClose());
                        final double open = sd.getOpen();
                        final double close = sd.getClose();
                        final double high = sd.getHigh();
                        final double low = sd.getLow();
                        // volume value available if needed
                        final long vol = sd.getVolume();
                        // create a candle node when plotted
                            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                if (newNode != null) {
                                    // Lightweight candle node (no per-node tooltip for performance)
                                    Candle candle = new Candle(open, close, high, low);
                                    StackPane container = (StackPane) newNode;
                                    container.getChildren().clear();
                                    container.getChildren().add(candle);
                                    // compute accurate pixel positions using axis display positions once layout is ready
                                    Platform.runLater(() -> {
                                        try {
                                            NumberAxis y = (NumberAxis) priceChartField.getYAxis();
                                            double yClose = y.getDisplayPosition(close);
                                            double yOpen = y.getDisplayPosition(open);
                                            double yHigh = y.getDisplayPosition(high);
                                            double yLow = y.getDisplayPosition(low);
                                            // body height and translate relative to data node's center (which aligns to 'close')
                                            double bodyHeight = Math.max(3, Math.abs(yOpen - yClose));
                                            double bodyTranslateY = (yOpen - yClose) / 2.0;
                                            candle.setBodyHeight(bodyHeight);
                                            candle.setBodyTranslateY(bodyTranslateY);
                                            double wickStart = yHigh - yClose;
                                            double wickEnd = yLow - yClose;
                                            candle.setWick(wickStart, wickEnd);
                                        } catch (Exception ignored) {}
                                    });
                                }
                            });
                        candleSeries.getData().add(d);
                    }

                    // compute axis bounds using raw USDT values then scale by current display currency rate
                    double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                    long maxVol = 0;
                    for (StockData sd : data) {
                        double v = sd.getClose(); // raw USDT
                        if (v < min) min = v;
                        if (v > max) max = v;
                        if (sd.getVolume() > maxVol) maxVol = sd.getVolume();
                    }
                    // scale bounds according to selected currency
                    double scale = ("ETB".equalsIgnoreCase(displayCurrency) ? usdToFiatRate : 1.0);
                    min = min * scale;
                    max = max * scale;
                    double padding = (max - min) * 0.12;
                    double lower = Math.max(0, min - padding);
                    double upper = max + padding;

                    final double finalLower = lower;
                    final double finalUpper = upper;
                    final long finalMaxVol = maxVol;

                    Platform.runLater(() -> {
                        // show candles and SMA lines
                        priceChartField.getData().clear();
                        priceChartField.getData().add(candleSeries);
                        priceChartField.getData().add(sma3);
                        // ensure series nodes get proper style classes (SMA lines)
                        for (XYChart.Series<String, Number> s : priceChartField.getData()) {
                            final String nm = s.getName() == null ? "" : s.getName();
                            s.nodeProperty().addListener((obs, oldN, newN) -> {
                                if (newN != null) {
                                    if (nm.contains("SMA 3")) newN.getStyleClass().add("sma3");
                                }
                            });
                        }
                        volumeChartField.getData().clear();
                        volumeChartField.getData().add(volSeries);
                        // ensure candles are laid out according to current axis/display positions
                        updateAllCandles();
                        debugArea.appendText("[hist] " + symbol + " points=" + (finalData == null ? 0 : finalData.size()) + "\n");

                        // scale Y-axis to data range
                        try {
                            NumberAxis y = (NumberAxis) priceChartField.getYAxis();
                            y.setAutoRanging(false);
                            y.setLowerBound(finalLower);
                            y.setUpperBound(finalUpper);
                            y.setTickUnit((finalUpper - finalLower) / 8.0);
                        } catch (Exception ignore) {}
                        try {
                            NumberAxis vy = (NumberAxis) volumeChartField.getYAxis();
                            vy.setAutoRanging(false);
                            vy.setLowerBound(0);
                            vy.setUpperBound(finalMaxVol);
                            vy.setTickUnit(Math.max(1, finalMaxVol / 4.0));
                        } catch (Exception ignore) {}

                        // compute percent change for card (first -> last)
                        if (finalData != null && finalData.size() >= 2) {
                            double first = finalData.get(0).getClose();
                            double last = finalData.get(finalData.size()-1).getClose();
                            double pct = ((last - first) / first) * 100.0;
                            Label cl = popularChangeLabels.get(symbol);
                            if (cl != null) {
                                String arrow = pct >= 0 ? "▲" : "▼";
                                cl.setText(String.format("%s %+.2f%%", arrow, pct));
                                cl.getStyleClass().removeAll("pos","neg");
                                cl.getStyleClass().add(pct >= 0 ? "pos" : "neg");
                            }
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Fetch historical failed: " + ex.getMessage()));
                }
                return null;
            }
        };
        t.setOnSucceeded(evt -> Platform.runLater(() -> statusLabel.setText("Chart updated for " + symbol)));
        new Thread(t).start();
    }

    // Detect Windows system theme (returns true if system uses dark mode for apps)
    private boolean detectSystemDarkMode() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) return false;
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command",
                    "(Get-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize' -ErrorAction SilentlyContinue).AppsUseLightTheme");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder out = new StringBuilder();
            String line;
            long start = System.currentTimeMillis();
            while ((line = r.readLine()) != null) {
                out.append(line.trim());
                // safety break after 2 seconds
                if (System.currentTimeMillis() - start > 2000) break;
            }
            try { p.destroy(); } catch (Exception ignored) {}
            String s = out.toString();
            if (s.isEmpty()) return false;
            try {
                int v = Integer.parseInt(s.replaceAll("\\D", ""));
                return v == 0; // 0 = dark, 1 = light
            } catch (NumberFormatException nfe) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    // Downsample OHLCV data into at most maxPoints buckets (preserves OHLC semantics)
    private List<StockData> downsampleData(List<StockData> src, int maxPoints) {
        if (src == null || src.size() <= maxPoints) return src;
        List<StockData> out = new java.util.ArrayList<>();
        int n = src.size();
        int bucket = (int) Math.ceil((double) n / maxPoints);
        for (int i = 0; i < n; i += bucket) {
            int end = Math.min(i + bucket, n);
            StockData first = src.get(i);
            StockData last = src.get(end - 1);
            double high = Double.NEGATIVE_INFINITY;
            double low = Double.POSITIVE_INFINITY;
            long vol = 0;
            for (int j = i; j < end; j++) {
                StockData s = src.get(j);
                if (s.getHigh() > high) high = s.getHigh();
                if (s.getLow() < low) low = s.getLow();
                vol += s.getVolume();
            }
            // keep timestamp of first point in bucket
            java.time.LocalDateTime dt = first.getDate();
            StockData agg = new StockData(first.getSymbol(), dt, first.getOpen(), last.getClose(), high, low, vol);
            out.add(agg);
        }
        return out;
    }

    // Simple candlestick node
    private static class Candle extends StackPane {
        private final Rectangle body = new Rectangle();
        private final Line wick = new Line();
        // store OHLC for later recompute
        private final double open, close, high, low;

        public Candle(double open, double close, double high, double low) {
            this.open = open;
            this.close = close;
            this.high = high;
            this.low = low;
            body.setWidth(12);
            body.setArcWidth(3);
            body.setArcHeight(3);
            // colors and stroke are handled by CSS classes (.candle-up / .candle-down and .candle-wick)
            body.getStyleClass().add("candle-rect");
            if (close >= open) {
                getStyleClass().add("candle-up");
            } else {
                getStyleClass().add("candle-down");
            }
            wick.setStartX(body.getWidth() / 2);
            wick.setEndX(body.getWidth() / 2);
            wick.getStyleClass().add("candle-wick");
            getChildren().addAll(wick, body);
        }

        public double getOpen() { return open; }
        public double getClose() { return close; }
        public double getHigh() { return high; }
        public double getLow() { return low; }

        public void setBodyHeight(double h) {
            body.setHeight(Math.max(3, Math.abs(h)));
        }

        public void setBodyTranslateY(double ty) {
            body.setTranslateY(ty);
        }

        public void setWick(double startY, double endY) {
            wick.setStartY(startY);
            wick.setEndY(endY);
        }
    }

    // Recompute pixel positions for all candles currently plotted
    private void updateAllCandles() {
        try {
            if (priceChartField == null) return;
            NumberAxis y = (NumberAxis) priceChartField.getYAxis();
            for (XYChart.Series<String, Number> s : priceChartField.getData()) {
                for (XYChart.Data<String, Number> d : s.getData()) {
                    Node n = d.getNode();
                    if (n == null) continue;
                    if (!(n instanceof StackPane)) continue;
                    StackPane container = (StackPane) n;
                    Candle candle = null;
                    for (Node ch : container.getChildren()) {
                        if (ch instanceof Candle) { candle = (Candle) ch; break; }
                    }
                    if (candle == null) continue;
                    // compute positions using stored OHLC
                    double open = candle.getOpen();
                    double close = candle.getClose();
                    double high = candle.getHigh();
                    double low = candle.getLow();
                    double yClose = y.getDisplayPosition(close);
                    double yOpen = y.getDisplayPosition(open);
                    double yHigh = y.getDisplayPosition(high);
                    double yLow = y.getDisplayPosition(low);
                    double bodyHeight = Math.max(3, Math.abs(yOpen - yClose));
                    double bodyTranslateY = (yOpen - yClose) / 2.0;
                    double wickStart = yHigh - yClose;
                    double wickEnd = yLow - yClose;
                    candle.setBodyHeight(bodyHeight);
                    candle.setBodyTranslateY(bodyTranslateY);
                    candle.setWick(wickStart, wickEnd);
                }
            }
        } catch (Exception ignored) {}
    }

    // Recompute candles for a given LineChart (used by detail page)
    private void updateCandlesFor(LineChart<String, Number> chart) {
        try {
            if (chart == null) return;
            NumberAxis y = (NumberAxis) chart.getYAxis();
            for (XYChart.Series<String, Number> s : chart.getData()) {
                for (XYChart.Data<String, Number> d : s.getData()) {
                    Node n = d.getNode();
                    if (n == null) continue;
                    if (!(n instanceof StackPane)) continue;
                    StackPane container = (StackPane) n;
                    Candle candle = null;
                    for (Node ch : container.getChildren()) {
                        if (ch instanceof Candle) { candle = (Candle) ch; break; }
                    }
                    if (candle == null) continue;
                    double open = candle.getOpen();
                    double close = candle.getClose();
                    double high = candle.getHigh();
                    double low = candle.getLow();
                    double yClose = y.getDisplayPosition(close);
                    double yOpen = y.getDisplayPosition(open);
                    double yHigh = y.getDisplayPosition(high);
                    double yLow = y.getDisplayPosition(low);
                    double bodyHeight = Math.max(3, Math.abs(yOpen - yClose));
                    double bodyTranslateY = (yOpen - yClose) / 2.0;
                    double wickStart = yHigh - yClose;
                    double wickEnd = yLow - yClose;
                    candle.setBodyHeight(bodyHeight);
                    candle.setBodyTranslateY(bodyTranslateY);
                    candle.setWick(wickStart, wickEnd);
                }
            }
        } catch (Exception ignored) {}
    }

    // add debug area and layout adjustments after chart is created
    @Override
    public void init() {
        // configure debug area
        debugArea.setEditable(false);
        debugArea.setWrapText(true);
        debugArea.setPrefRowCount(6);
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private void simulateSelected(TrackedStock ts) {
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    double price = RmiMarketData.getCurrentPrice(ts.getSymbol(), null);
                    for (int i = 0; i < 10; i++) {
                        price = price * (1 + (Math.random() - 0.5) * 0.08);
                        // simulation only — no local storage
                        new StockData(ts.getSymbol(), java.time.LocalDateTime.now().plusDays(i), price, price, price, price, 0);
                    }
                    Platform.runLater(() -> showAlert(AlertType.INFORMATION, "Simulation complete for " + ts.getSymbol()));
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Simulation error: " + ex.getMessage()));
                }
                return null;
            }
        };
        new Thread(t).start();
    }

    private void openDetailPage(String symbol) {
        Stage s = new Stage();
        BorderPane p = new BorderPane();
        p.setPadding(new Insets(10));

        Label title = new Label(symbol + " — Details");
        title.getStyleClass().add("detail-title");

        // top: title + summary stats
        HBox infoBox = new HBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label("Last: —");
        priceLabel.getStyleClass().add("price-label");
        Label changeLabel = new Label("(0.00%)");
        Label highLowLabel = new Label("H: —  L: —");
        Label volLabel = new Label("Vol: —");
        infoBox.getChildren().addAll(priceLabel, changeLabel, highLowLabel, volLabel);

        VBox topBox = new VBox(6, title, infoBox);
        p.setTop(topBox);

        // Left: market list
        ListView<String> marketList = new ListView<>();
        marketList.setPrefWidth(240);
        Task<Void> loadMarkets = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    List<String> all = RmiMarketData.getAllSymbols();
                    Platform.runLater(() -> marketList.getItems().setAll(all));
                } catch (Exception ex) {
                    Platform.runLater(() -> marketList.getItems().add("Failed to load markets: " + ex.getMessage()));
                }
                return null;
            }
        };
        new Thread(loadMarkets).start();

        // center: candlestick + volume charts
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        LineChart<String, Number> priceChart = new LineChart<>(x, y);
        priceChart.setAnimated(false);
        priceChart.setCreateSymbols(false);
        priceChart.setPrefSize(760, 460);

        CategoryAxis vx = new CategoryAxis();
        NumberAxis vy = new NumberAxis();
        BarChart<String, Number> volChart = new BarChart<>(vx, vy);
        volChart.setLegendVisible(false);
        volChart.setPrefHeight(140);

        VBox centerCharts = new VBox(6, priceChart, volChart);

        // Right: order book & trades
        VBox rightPanel = new VBox(8);
        rightPanel.setPrefWidth(300);
        rightPanel.setPadding(new Insets(6));
        Label obTitle = new Label("Order Book");
        ListView<String> bidsView = new ListView<>();
        ListView<String> asksView = new ListView<>();
        HBox obCols = new HBox(6, new VBox(new Label("Bids"), bidsView), new VBox(new Label("Asks"), asksView));
        Label tradesTitle = new Label("Recent Trades");
        ListView<String> tradesView = new ListView<>();
        rightPanel.getChildren().addAll(obTitle, obCols, tradesTitle, tradesView);

        p.setLeft(marketList);
        p.setCenter(centerCharts);
        p.setRight(rightPanel);

        // center message for empty/error states
        Label centerMessage = new Label("");
        centerMessage.getStyleClass().add("center-message");
        // load data for the requested symbol
        Label extInfoLabel = new Label("");
        extInfoLabel.setWrapText(true);
        extInfoLabel.getStyleClass().add("ext-info");
        // ensure extended info placeholder exists under charts
        // (it will be added when data arrives)
        // initial placeholders while loading
        bidsView.getItems().setAll("Loading bids...");
        asksView.getItems().setAll("Loading asks...");
        tradesView.getItems().setAll("Loading trades...");

        Task<Void> loadDetail = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // 24h ticker
                    Map<String, String> t24 = RmiMarketData.get24hTicker(symbol);
                    Platform.runLater(() -> {
                        try {
                            String last = t24.getOrDefault("lastPrice", "");
                            if (last != null && !last.isEmpty()) {
                                double lp = Double.parseDouble(last);
                                priceLabel.setText("Last: " + formatCurrency(lp));
                            } else {
                                priceLabel.setText("Last: —");
                            }
                        } catch (Exception ex) {
                            priceLabel.setText("Last: —");
                        }
                        changeLabel.setText("(" + t24.getOrDefault("priceChangePercent", "0.00") + "%)");
                        try {
                            String h = t24.getOrDefault("highPrice", "");
                            String l = t24.getOrDefault("lowPrice", "");
                            if (h != null && !h.isEmpty()) highLowLabel.setText("H: " + formatCurrency(Double.parseDouble(h)) + "  L: " + formatCurrency(Double.parseDouble(l)));
                        } catch (Exception ignored) {}
                        volLabel.setText("Vol: " + t24.getOrDefault("volume", "—"));
                    });

                    // CoinGecko detailed info (ATH/ATL, genesis_date)
                    Map<String, String> cg = null;
                    String cgError = null;
                    try {
                        cg = com.stockapp.rmi.RmiMarketData.getCoinDetails(symbol);
                    } catch (Exception e) {
                        debugArea.appendText("CoinGecko fetch failed: " + e.getMessage() + "\n");
                    }

                    // klines for chart (respect selected interval)
                    String interval = "1d";
                    int limit = 200;
                    // Request raw USDT historical data and convert on client side for consistent chart scaling
                    List<StockData> data = RmiMarketData.getHistoricalData(symbol, interval, limit);
                    // Downsample large datasets for detail page to improve responsiveness
                    final int MAX_POINTS = 120;
                    if (data != null && data.size() > MAX_POINTS) {
                        data = downsampleData(data, MAX_POINTS);
                        final int ds = data.size();
                        Platform.runLater(() -> debugArea.appendText("[perf] detail downsampled to " + ds + " points\n"));
                    }
                    final java.util.List<StockData> finalData = data;
                    // if API returned nothing, show message (no DB cache)
                    if (data == null || data.isEmpty()) {
                        Platform.runLater(() -> {
                            centerCharts.getChildren().clear();
                            centerMessage.setText("No historical data available for " + symbol + " (try another symbol or check network)");
                            centerCharts.getChildren().add(centerMessage);
                        });
                    }
                    if (data != null && !data.isEmpty()) {
                        // if t24 didn't provide lastPrice, use last close
                        if ((t24 == null || t24.getOrDefault("lastPrice", "").isEmpty() || t24.get("lastPrice").equals(""))) {
                            double lastClose = data.get(data.size()-1).getClose();
                            Platform.runLater(() -> priceLabel.setText("Last: " + formatCurrency(lastClose)));
                        }
                    }
                    DateTimeFormatter fmt;
                    if (interval != null && (interval.endsWith("m") || interval.contains("h"))) {
                        fmt = DateTimeFormatter.ofPattern("HH:mm");
                    } else {
                        fmt = DateTimeFormatter.ofPattern("MM-dd");
                    }
                    XYChart.Series<String, Number> sma3 = new XYChart.Series<>(); sma3.setName("SMA 3");
                    XYChart.Series<String, Number> volSeries = new XYChart.Series<>();
                    List<Double> closes = new java.util.ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        StockData sd = data.get(i);
                        String label = sd.getDate().format(fmt);
                        closes.add(sd.getClose());
                        volSeries.getData().add(new XYChart.Data<>(label, sd.getVolume()));
                        if (i >= 2) {
                            double sum = 0; int cnt = 0; for (int j = i-2; j <= i; j++){ sum += closes.get(j); cnt++; }
                            sma3.getData().add(new XYChart.Data<>(label, sum / cnt));
                        } else sma3.getData().add(new XYChart.Data<>(label, null));
                        // SMA-5 removed
                    }

                    // candle series
                    XYChart.Series<String, Number> candleSeries = new XYChart.Series<>();
                    candleSeries.setName(symbol + " Candles");
                    for (StockData sd : data) {
                        String label = sd.getDate().format(fmt);
                        XYChart.Data<String, Number> dd = new XYChart.Data<>(label, sd.getClose());
                        final double open = sd.getOpen(), close = sd.getClose(), high = sd.getHigh(), low = sd.getLow();
                        dd.nodeProperty().addListener((obs, oldN, newN) -> {
                            if (newN != null) {
                                Candle c = new Candle(open, close, high, low);
                                StackPane cont = (StackPane)newN;
                                cont.getChildren().clear(); cont.getChildren().add(c);
                                Platform.runLater(() -> {
                                    try {
                                        NumberAxis yAxis = (NumberAxis) priceChart.getYAxis();
                                        double yClose = yAxis.getDisplayPosition(close);
                                        double yOpen = yAxis.getDisplayPosition(open);
                                        double yHigh = yAxis.getDisplayPosition(high);
                                        double yLow = yAxis.getDisplayPosition(low);
                                        double bodyH = Math.max(3, Math.abs(yOpen - yClose));
                                        double bodyTy = (yOpen - yClose) / 2.0;
                                        c.setBodyHeight(bodyH);
                                        c.setBodyTranslateY(bodyTy);
                                        c.setWick(yHigh - yClose, yLow - yClose);
                                    } catch (Exception ignored) {}
                                });
                            }
                        });
                        candleSeries.getData().add(dd);
                    }

                    // compute axis bounds and update UI
                    if (data != null && !data.isEmpty()) {
                        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                        long maxVol = 0;
                        for (StockData sd : data) {
                            double v = sd.getClose();
                            if (v < min) min = v;
                            if (v > max) max = v;
                            if (sd.getVolume() > maxVol) maxVol = sd.getVolume();
                        }
                        double padding = (max - min) * 0.12;
                        double lower = Math.max(0, min - padding);
                        double upper = max + padding;
                        final double finalLower = lower, finalUpper = upper; final long finalMaxVol = maxVol;

                        Platform.runLater(() -> {
                            try {
                                NumberAxis yy = (NumberAxis) priceChart.getYAxis();
                                yy.setAutoRanging(false);
                                yy.setLowerBound(finalLower);
                                yy.setUpperBound(finalUpper);
                                yy.setTickUnit((finalUpper - finalLower) / 8.0);
                            } catch (Exception ignore) {}
                            try {
                                NumberAxis vyy = (NumberAxis) volChart.getYAxis();
                                vyy.setAutoRanging(false);
                                vyy.setLowerBound(0);
                                vyy.setUpperBound(finalMaxVol);
                                vyy.setTickUnit(Math.max(1, finalMaxVol / 4.0));
                            } catch (Exception ignore) {}
                            priceChart.getData().clear();
                            priceChart.getData().add(candleSeries);
                            priceChart.getData().add(sma3);
                            // wire SMA class names for CSS
                            for (XYChart.Series<String, Number> s : priceChart.getData()) {
                                final String nm = s.getName() == null ? "" : s.getName();
                                s.nodeProperty().addListener((obs, oldN, newN) -> {
                                    if (newN != null) {
                                        if (nm.contains("SMA 3")) newN.getStyleClass().add("sma3");
                                    }
                                });
                            }
                            volChart.getData().clear();
                            volChart.getData().add(volSeries);
                            // recompute candle pixel positions for this detail chart
                            updateCandlesFor(priceChart);
                        });
                    }
                        // if we have CoinGecko details, show extended info
                        if (cg != null) {
                            final String ath = cg.getOrDefault("ath", "—");
                            final String athDate = cg.getOrDefault("ath_date", "—");
                            final String atl = cg.getOrDefault("atl", "—");
                            final String atlDate = cg.getOrDefault("atl_date", "—");
                            final String genesis = cg.getOrDefault("genesis_date", "—");
                            final String mcap = cg.getOrDefault("market_cap", "—");
                            // show extended info label on FX thread
                            Platform.runLater(() -> {
                                extInfoLabel.setText(String.format("ATH: $%s (at %s)   ATL: $%s (at %s)   Genesis: %s   MarketCap: $%s", ath, athDate, atl, atlDate, genesis, mcap));
                                if (!centerCharts.getChildren().contains(extInfoLabel)) centerCharts.getChildren().add(extInfoLabel);
                            });
                        } else if (cgError != null) {
                            final String _cgErr = cgError;
                            Platform.runLater(() -> {
                                extInfoLabel.setText("Extended coin details unavailable: " + _cgErr);
                                if (!centerCharts.getChildren().contains(extInfoLabel)) centerCharts.getChildren().add(extInfoLabel);
                            });
                        }

                    // order book + trades (initial population; periodic updater will refresh)
                    try {
                        Map<String, List<String>> ob = RmiMarketData.getOrderBook(symbol, 20);
                        List<String> bids = ob.getOrDefault("bids", java.util.Collections.emptyList());
                        List<String> asks = ob.getOrDefault("asks", java.util.Collections.emptyList());
                        List<String> trades = RmiMarketData.getRecentTrades(symbol, 40);
                        Platform.runLater(() -> {
                            bidsView.getItems().setAll(bids);
                            asksView.getItems().setAll(asks);
                            tradesView.getItems().setAll(trades);
                        });
                    } catch (Exception ex) {
                        String msg = "Order book / trades unavailable: " + ex.getMessage();
                        Platform.runLater(() -> {
                            bidsView.getItems().setAll(msg);
                            asksView.getItems().setAll(msg);
                            tradesView.getItems().setAll(msg);
                        });
                    }

                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert(AlertType.ERROR, "Detail load failed: " + ex.getMessage()));
                }
                return null;
            }
        };
        new Thread(loadDetail).start();

        // start a periodic updater for order book and recent trades while the detail window is open
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                Map<String, List<String>> ob = RmiMarketData.getOrderBook(symbol, 20);
                List<String> bids = ob.getOrDefault("bids", java.util.Collections.emptyList());
                List<String> asks = ob.getOrDefault("asks", java.util.Collections.emptyList());
                List<String> trades = RmiMarketData.getRecentTrades(symbol, 40);
                Platform.runLater(() -> {
                    bidsView.getItems().setAll(bids);
                    asksView.getItems().setAll(asks);
                    tradesView.getItems().setAll(trades);
                });
            } catch (Exception ex) {
                String msg = "Order book update failed: " + ex.getMessage();
                Platform.runLater(() -> {
                    bidsView.getItems().setAll(msg);
                    asksView.getItems().setAll(msg);
                    tradesView.getItems().setAll(msg);
                });
            }
        }, 5, 3, TimeUnit.SECONDS);

        s.setOnCloseRequest(evt -> {
            try { executor.shutdownNow(); } catch (Exception ignored) {}
        });

        Scene scene = new Scene(p, 1200, 680);
        // apply same stylesheets as main window so detail follows current theme
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            if (darkMode) {
                scene.getStylesheets().add(getClass().getResource("/style-dark.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/style-light.css").toExternalForm());
            }
        } catch (Exception ex) {
            System.err.println("Could not apply detail styles: " + ex.getMessage());
        }
        s.setScene(scene);
        s.setTitle(symbol + " — Market Details");
        // register detail window so theme changes propagate
        registerDetailWindow(s, priceChart, volChart);
        s.setOnCloseRequest(evt -> unregisterDetailWindow(s));
        s.show();
        // apply chart theming after the scene is shown so lookup() succeeds
        applyChartTheme(priceChart, volChart);
        // start realtime polling (via RMI) for this detail window
        try {
            final String pair = symbol.toUpperCase().endsWith("USDT") ? symbol.toUpperCase() : symbol.toUpperCase() + "USDT";
            final java.util.concurrent.ScheduledExecutorService detailUpdater = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
            detailUpdater.scheduleAtFixedRate(() -> {
                try {
                    com.stockapp.model.StockData k = com.stockapp.rmi.RmiMarketData.getLatestKline(pair, "1d");
                    if (k == null) return;
                    long start = k.getDate() == null ? System.currentTimeMillis() : k.getDate().atZone(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli();
                    final double close = k.getClose();
                    final double vol = k.getVolume();
                    Platform.runLater(() -> {
                        try {
                            DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("HH:mm");
                            String label = java.time.Instant.ofEpochMilli(start).atZone(java.time.ZoneId.of("UTC")).toLocalDateTime().format(fmt2);
                            if (priceChart.getData().size() > 0) {
                                XYChart.Series<String, Number> cs = (XYChart.Series<String, Number>) priceChart.getData().get(0);
                                if (cs != null && !cs.getData().isEmpty()) {
                                    XYChart.Data<String, Number> last = cs.getData().get(cs.getData().size()-1);
                                    if (last.getXValue().equals(label)) {
                                        last.setYValue(close);
                                    } else {
                                        cs.getData().add(new XYChart.Data<>(label, close));
                                        if (cs.getData().size() > 300) cs.getData().remove(0);
                                    }
                                }
                            }
                            if (volChart.getData().size() > 0) {
                                XYChart.Series<String, Number> vs = (XYChart.Series<String, Number>) volChart.getData().get(0);
                                if (vs != null && !vs.getData().isEmpty()) {
                                    XYChart.Data<String, Number> lastv = vs.getData().get(vs.getData().size()-1);
                                    if (lastv.getXValue().equals(label)) {
                                        lastv.setYValue((Number)vol);
                                    } else {
                                        vs.getData().add(new XYChart.Data<>(label, vol));
                                        if (vs.getData().size() > 300) vs.getData().remove(0);
                                    }
                                }
                            }
                            updateCandlesFor(priceChart);
                        } catch (Exception ignored) {}
                    });
                } catch (Exception ignored) {}
            }, 0, 3, java.util.concurrent.TimeUnit.SECONDS);
            s.setOnCloseRequest(evt -> { try { detailUpdater.shutdownNow(); } catch (Exception ignored) {} });
        } catch (Exception ex) {
            debugArea.appendText("[rt] realtime polling failed: " + ex.getMessage() + "\n");
        }
    }

    private void showAlert(AlertType t, String message) {
        new Alert(t, message).showAndWait();
    }

    public static void main(String[] args) { launch(args); }

    // JavaScript bridge for WebView integration
    public static class JavaBridge {
        private WebEngine webEngine;
        private String currentSymbol = "BTC";
        private ScheduledExecutorService updater;

        public JavaBridge(WebEngine we) {
            this.webEngine = we;
        }

        public void fetchData(String symbol) {
        currentSymbol = symbol;
        if (updater != null) {
            updater.shutdown();
        }
        updateData(symbol);
        updater = Executors.newSingleThreadScheduledExecutor();
        updater.scheduleAtFixedRate(() -> updateData(currentSymbol), 10, 10, TimeUnit.SECONDS);
        }

        public void getTopSymbols() {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Map<String, Double> prices = RmiMarketData.getAllPrices();
                        List<String> topSymbols = Arrays.asList("BTC", "ETH", "SOL", "XRP", "ADA", "DOGE", "BNB", "LTC");
                        StringBuilder sb = new StringBuilder("[");
                        for (String sym : topSymbols) {
                            Double price = prices.get(sym);
                            if (price != null) {
                                sb.append(String.format("{symbol:'%s',price:%.2f},", sym, price));
                            }
                        }
                        if (sb.length() > 1) sb.setLength(sb.length() - 1);
                        sb.append("]");
                        Platform.runLater(() -> {
                            webEngine.executeScript("updateMarketList(" + sb.toString() + ")");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            webEngine.executeScript("alert('Error loading top symbols: " + e.getMessage().replace("'", "\\'") + "')");
                        });
                    }
                    return null;
                }
            };
            new Thread(task).start();
        }

        public void getAllSymbols() {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        List<String> symbols = RmiMarketData.getAllSymbols();
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < Math.min(symbols.size(), 100); i++) { // limit to 100
                            sb.append(String.format("'%s',", symbols.get(i)));
                        }
                        if (sb.length() > 1) sb.setLength(sb.length() - 1);
                        sb.append("]");
                        Platform.runLater(() -> {
                            webEngine.executeScript("updateMarketList(" + sb.toString() + ")");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            webEngine.executeScript("alert('Error loading all symbols: " + e.getMessage().replace("'", "\\'") + "')");
                        });
                    }
                    return null;
                }
            };
            new Thread(task).start();
        }

        private void updateData(String symbol) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        // Fetch ticker data
                        Map<String, String> ticker = RmiMarketData.get24hTicker(symbol);
                        if (ticker != null) {
                            double price = Double.parseDouble(ticker.get("lastPrice"));
                            double change = Double.parseDouble(ticker.get("priceChangePercent"));
                            double high = Double.parseDouble(ticker.get("highPrice"));
                            double low = Double.parseDouble(ticker.get("lowPrice"));
                            double vol = Double.parseDouble(ticker.get("volume"));
                            String tickerJson = String.format("{price:%.2f,change:%.2f,high:%.2f,low:%.2f,vol:%.2f}", price, change, high, low, vol);
                            Platform.runLater(() -> {
                                webEngine.executeScript("updateTicker(" + tickerJson + ")");
                            });
                        }

                        // TODO: Update chart if needed

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            webEngine.executeScript("alert('Error updating data: " + e.getMessage().replace("'", "\\'") + "')");
                        });
                    }
                    return null;
                }
            };
            new Thread(task).start();
        }
    }

    // Favorites management methods
    private void loadFavorites() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(StockApp.class);
            String favoritesStr = prefs.get("user.favorites", "");
            if (!favoritesStr.isEmpty()) {
                userFavorites.clear();
                String[] favorites = favoritesStr.split(",");
                for (String fav : favorites) {
                    if (!fav.trim().isEmpty()) {
                        userFavorites.add(fav.trim());
                    }
                }
            }
        } catch (Exception e) {
            // Default favorites if loading fails
            userFavorites.addAll(Arrays.asList("BTC", "ETH", "BNB"));
        }
    }

    private void saveFavorites() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(StockApp.class);
            String favoritesStr = String.join(",", userFavorites);
            prefs.put("user.favorites", favoritesStr);
        } catch (Exception ignored) {}
    }

    private void toggleFavorite(String symbol) {
        if (userFavorites.contains(symbol)) {
            userFavorites.remove(symbol);
        } else {
            userFavorites.add(symbol);
        }
        saveFavorites();
        // Refresh the market list if we're in favorites view
        if (!currentShowAll) {
            populateMarketList(currentShowAll, currentSearchFilter);
        }
    }

    // Simple observable holder for table
    public static class TrackedStock {
        private final javafx.beans.property.SimpleStringProperty symbol = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleDoubleProperty price = new javafx.beans.property.SimpleDoubleProperty();

        public TrackedStock(String s, double p) { symbol.set(s); price.set(p); }
        public String getSymbol() { return symbol.get(); }
        public void setSymbol(String s) { symbol.set(s); }
        public double getPrice() { return price.get(); }
        public void setPrice(double p) { price.set(p); }
    }
}
