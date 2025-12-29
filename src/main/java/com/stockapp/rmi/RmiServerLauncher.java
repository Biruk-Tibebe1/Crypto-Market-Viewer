package com.stockapp.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

public class RmiServerLauncher {
    public static void main(String[] args) {
        int port = 1099;
        String bindName = "MarketDataService";
        try {
            MarketDataServiceImpl impl = new MarketDataServiceImpl();
            // Determine RMI host: allow override via env or system property, else auto-detect
            String override = System.getProperty("stockapp.rmi.host");
            if (override == null || override.isBlank()) {
                override = System.getenv("STOCKAPP_RMI_HOST");
            }
            String ip = null;
            if (override != null && !override.isBlank()) {
                ip = override.trim();
            } else {
                ip = getLocalIpAddress();
            }
            if (ip == null || ip.isBlank()) {
                ip = "127.0.0.1";
            }
            System.setProperty("java.rmi.server.hostname", ip);
            try {
                Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind(bindName, impl);
            } catch (Exception e) {
                // if registry already exists, try to get it
                Registry registry = LocateRegistry.getRegistry(port);
                registry.rebind(bindName, impl);
            }
            System.out.println("RMI MarketDataService bound as '" + bindName + "' on port " + port);
            System.out.println("RMI server hostname set to: " + ip + " (clients should connect to this address)");
            // Keep the server process alive so the registry and remote object remain available
            System.out.println("RMI server running. Press CTRL+C to stop.");
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {
            }
        } catch (Exception e) {
            System.err.println("RMI server failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface iface : Collections.list(ifaces)) {
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                for (InetAddress addr : Collections.list(addrs)) {
                    if (addr.isLoopbackAddress()) continue;
                    String ip = addr.getHostAddress();
                    // prefer IPv4
                    if (ip != null && ip.indexOf(':') < 0) return ip;
                }
            }
            // fallback
            InetAddress local = InetAddress.getLocalHost();
            return local.getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }
}
