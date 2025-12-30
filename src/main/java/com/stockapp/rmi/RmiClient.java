package com.stockapp.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiClient {
    private final MarketDataService service;

    public RmiClient(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.service = (MarketDataService) registry.lookup("MarketDataService");
    }

    public MarketDataService getService() { return service; }
}
