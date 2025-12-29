package com.stockapp.rmi;

import java.util.List;
import java.util.Map;

public class TestRmiClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1099;
        if (args.length > 0) host = args[0];
        if (args.length > 1) {
            try { port = Integer.parseInt(args[1]); } catch (Exception ignored) {}
        }
        try {
            System.out.println("Initializing RMI client to " + host + ":" + port);
            RmiMarketData.init(host, port);
            System.out.println("RMI initialized.");
            Map<String, Double> prices = RmiMarketData.getAllPrices();
            System.out.println("getAllPrices -> count=" + (prices==null?0:prices.size()));
            List<String> syms = RmiMarketData.getAllSymbols();
            System.out.println("getAllSymbols -> count=" + (syms==null?0:syms.size()));
        } catch (Exception e) {
            System.err.println("RMI test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
