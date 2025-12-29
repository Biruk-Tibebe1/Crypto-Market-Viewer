# CryptoMarket Desktop — Real-time Crypto Viewer (Java)

## Overview

CryptoMarket Desktop is a small Java desktop application that fetches and displays real-time market data (cryptocurrency) and exposes updates via Java RMI for local clients. The project demonstrates a lightweight, extensible architecture for collecting live market data, storing snapshots, and delivering them to UI or other JVM clients.

## Motivation

- Provide a simple, offline-deployable example of real-time market data ingestion and distribution using Java.
- Offer a learning project that combines networked services (RMI), external API integration, and a minimal GUI.
- Help developers prototype trading dashboards, learning tools, and testing environments without relying on heavy infrastructure.

## How it works (high level)

- A data ingestion layer retrieves live market feeds (e.g., Binance) using `BinanceRealtime.java` (or `StockFetcher.java` for other sources).
- The retrieved data is converted into domain models such as `StockData` 
- A Java RMI service (`MarketDataServiceImpl`) exposes the latest market snapshots and change notifications.
- Local UI clients (example: `ui/StockApp.java`) connect to the RMI service to display live updates.
- `RmiServerLauncher` starts the RMI registry and binds the service for clients to consume.

## Key Components

- `src/main/java/com/stockapp/api`: Live data adapters (e.g., `BinanceRealtime.java`, `StockFetcher.java`).
- `src/main/java/com/stockapp/model`: Domain models (`StockData.java`).
- `src/main/java/com/stockapp/rmi`: RMI interface and implementations (`MarketDataService.java`, `MarketDataServiceImpl.java`, `RmiServerLauncher.java`, `RmiClient.java`, `TestRmiClient.java`).
- `src/main/java/com/stockapp/ui`: Example desktop UI (`StockApp.java`).

## Features

- Real-time market data ingestion from external APIs.
- Java RMI-based distribution so multiple local JVM clients can subscribe.
- Simple GUI for visualization and testing.
- Modular codebase to add new data sources or clients easily.

## When/Why to use this project

- Educational purposes: learn RMI, data ingestion, and connecting UIs to live feeds.
- Prototyping: quickly test trading UI ideas or integration approaches without deploying services.
- Local testing: simulate real-time feeds for algorithm development and backtesting.

## Requirements

- Java 17+ (or the JDK version configured in your environment)
- Maven 3.6+ for building
- Internet access for live data adapters (or you can stub data for offline testing)

## Build & Run

1. Build the project:

```bash
mvn clean package
```

2. Start the RMI server (from project root):

```bash
# Launch RMI server
java -cp target/classes com.stockapp.rmi.RmiServerLauncher
```

3. Start the desktop UI client (in a separate terminal):

```bash
# Run the Swing/JavaFX UI (example)
java -cp target/classes com.stockapp.ui.StockApp
```

4. (Optional) Run test client to validate RMI connectivity:

```bash
java -cp target/classes com.stockapp.rmi.TestRmiClient
```

Notes:
- If you customize classpaths or package the app into a fat JAR, adapt the commands accordingly.
- The `RmiServerLauncher` prints RMI registry info and any binding names used by the service.

## Configuration

- API keys or endpoint settings (if required by a chosen data source) should be supplied via environment variables or a local properties file you add to the project.
- Storage implementations are intentionally simple; `service/Storage.java` can be swapped for a DB-backed implementation.

## Extending the Project

- Add new adapters in `api/` for more exchanges or mock sources.
- Implement persistent storage (SQL/NoSQL) by replacing or augmenting `service/Storage.java`.
- Replace the sample UI with a richer dashboard or web client that consumes the same RMI service.

## Contributing

- Fork the repository, make a focused change, and open a pull request with a clear description.
- Keep commits small and tests (or manual verification steps) documented in the PR.


## Contact

For questions or feedback, open an issue on the repository or contact the maintainers via the project GitHub page.
