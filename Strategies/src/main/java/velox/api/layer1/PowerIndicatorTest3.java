package velox.api.layer1.simplified.demo;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.*;
import velox.api.layer1.common.Log;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

@Layer1SimpleAttachable
@Layer1StrategyName("VWAP Trading Strategy")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class PowerIndicatorTest3 implements CustomModule, IntervalListener, SnapshotEndListener, DepthDataListener {

    private Indicator buyVolumeIndicator;
    private Indicator sellVolumeIndicator;
    private Indicator buySignalMarker;
    private Indicator sellSignalMarker;
    private Indicator profitIndicator;
    private Indicator vwapIndicator;

    private double totalBuyVolume = 0;
    private double totalSellVolume = 0;

    private int latestBidPrice = 0;
    private int latestAskPrice = 0;

    private int sharesOwned = 0;
    private double totalProfit = 0.0;
    private static final double TRANSACTION_COST = 0.01; // Example transaction cost per share

    private double vwapTotalVolume = 0;
    private double vwapCumulativePriceVolume = 0;

    private static final double VWAP_BUY_THRESHOLD = 0.95; // Buy when the price is below 95% of VWAP
    private static final double VWAP_SELL_THRESHOLD = 1.05; // Sell when the price is above 105% of VWAP

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        buyVolumeIndicator = api.registerIndicator("Buy Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        buyVolumeIndicator.setColor(Color.GREEN);

        sellVolumeIndicator = api.registerIndicator("Sell Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellVolumeIndicator.setColor(Color.RED);

        // Registering new indicators for buy and sell signal markers
        buySignalMarker = api.registerIndicator("Buy Signal Marker", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellSignalMarker = api.registerIndicator("Sell Signal Marker", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);

        // Registering an indicator for profit
        profitIndicator = api.registerIndicator("Profit", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        profitIndicator.setColor(Color.BLUE);

        // Registering an indicator for VWAP
        vwapIndicator = api.registerIndicator("VWAP", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        vwapIndicator.setColor(Color.ORANGE);

        Log.info("VWAP Trading Strategy initialized");
    }

    @Override
    public void stop() {
        // Clean up resources if needed
    }

    @Override
    public void onSnapshotEnd() {
        Log.info("Snapshot Ended: Order book is ready for processing.");
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        if (size > 0) {
            if (isBid) {
                totalBuyVolume += size;
                latestBidPrice = price; // Store the latest bid price
            } else {
                totalSellVolume += size;
                latestAskPrice = price; // Store the latest ask price
            }

            // Update VWAP calculation
            double tradePrice = (latestBidPrice + latestAskPrice) / 2.0;
            double tradeVolume = totalBuyVolume + totalSellVolume;
            vwapCumulativePriceVolume += tradePrice * tradeVolume;
            vwapTotalVolume += tradeVolume;
            double vwap = vwapCumulativePriceVolume / vwapTotalVolume;
            vwapIndicator.addPoint(vwap);
        }
    }

    @Override
    public long getInterval() {
        return Intervals.INTERVAL_1_SECOND;
    }

    @Override
    public void onInterval() {
        // Add the volumes to the indicators
        buyVolumeIndicator.addPoint(totalBuyVolume);
        sellVolumeIndicator.addPoint(totalSellVolume);

        // Calculate VWAP
        double vwap = vwapCumulativePriceVolume / vwapTotalVolume;

        // Get the current price
        double currentPrice = (latestBidPrice + latestAskPrice) / 2.0;

        // Buy condition
        if (currentPrice < vwap * VWAP_BUY_THRESHOLD) {
            placeMarker("VWAP Buy Signal Detected!", true);
        }

        // Sell condition
        if (sharesOwned > 0 && currentPrice > vwap * VWAP_SELL_THRESHOLD) {
            placeMarker("VWAP Sell Signal Detected!", false);
        }

        // Reset volumes for the next interval
        totalBuyVolume = 0;
        totalSellVolume = 0;
    }

    private void placeMarker(String message, boolean isBuySignal) {
        // Determine the price to use for the marker
        double markerPrice = isBuySignal ? latestBidPrice : latestAskPrice;

        if (isBuySignal) {
            buySignalMarker.addPoint(markerPrice);
            buySignalMarker.setColor(Color.GREEN);
            sellSignalMarker.addPoint(0); // Clear the sell signal marker

            // Execute buy trade
            buyShares(100, markerPrice);
            Log.info(message + " Bought 100 shares at " + markerPrice);
        } else {
            sellSignalMarker.addPoint(markerPrice);
            sellSignalMarker.setColor(Color.RED);
            buySignalMarker.addPoint(0); // Clear the buy signal marker

            // Execute sell trade
            sellShares(100, markerPrice);
            Log.info(message + " Sold 100 shares at " + markerPrice);
        }
    }

    private void buyShares(int quantity, double price) {
        sharesOwned += quantity;
        totalProfit -= (quantity * price) + (quantity * TRANSACTION_COST); // Subtract the cost of buying shares and transaction costs from total profit
        updateProfitIndicator();
    }

    private void sellShares(int quantity, double price) {
        if (sharesOwned >= quantity) {
            sharesOwned -= quantity;
            totalProfit += (quantity * price) - (quantity * TRANSACTION_COST); // Add the revenue from selling shares and subtract transaction costs to total profit
            updateProfitIndicator();
        }
    }

    private void updateProfitIndicator() {
        profitIndicator.addPoint(totalProfit);
        Log.info("Updated Profit: " + totalProfit);
    }
}