package velox.api.layer1.simplified.demo;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

@Layer1SimpleAttachable
@Layer1StrategyName("Enhanced Power Indicator T4")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class PowerIndicatorTest4 implements CustomModule, IntervalListener, SnapshotEndListener, DepthDataListener {

    private Indicator buyVolumeIndicator;
    private Indicator sellVolumeIndicator;
    private Indicator signalIndicator;
    private Indicator buySignalMarker;
    private Indicator sellSignalMarker;
    private Indicator profitIndicator;  // New indicator for profit

    private double totalBuyVolume = 0;
    private double totalSellVolume = 0;
    private double maxVolumeDominance = 0;
    private double normalizedScore = 0;

    private static final int WINDOW_SIZE = 60; // Example window size in seconds (1 minute)
    private static final double SIGNAL_THRESHOLD = 5.0; // Lower threshold for more frequent signals
    private Queue<Double> dominanceWindow = new LinkedList<>();

    private int latestBidPrice = 0;
    private int latestAskPrice = 0;

    private Api api;
    private InstrumentInfo instrumentInfo;

    private int boughtShares = 0;
    private double buyPrice = 0;
    private double totalProfit = 0;

    private static final double INVESTMENT_AMOUNT = 1000.0; // Investment amount in dollars

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        this.api = api;
        this.instrumentInfo = instrumentInfo;

        buyVolumeIndicator = api.registerIndicator("Buy Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        buyVolumeIndicator.setColor(Color.GREEN);

        sellVolumeIndicator = api.registerIndicator("Sell Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellVolumeIndicator.setColor(Color.RED);

        signalIndicator = api.registerIndicator("Volume Signal", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);

        buySignalMarker = api.registerIndicator("Buy Signal Marker", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellSignalMarker = api.registerIndicator("Sell Signal Marker", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);

        profitIndicator = api.registerIndicator("Profit", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);  // Register profit indicator
        profitIndicator.setColor(Color.BLUE);
    }

    @Override
    public void stop() {
        // Clean up resources if needed
    }

    @Override
    public void onSnapshotEnd() {
        // This method will be called when the snapshot ends
        System.out.println("Snapshot Ended: Order book is ready for processing.");
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

        // Determine volume dominance
        double dominance = totalBuyVolume - totalSellVolume;

        // Update the rolling window with the current dominance
        dominanceWindow.add(Math.abs(dominance));
        if (dominanceWindow.size() > WINDOW_SIZE) {
            dominanceWindow.poll();
        }

        // Calculate the dynamic maximum volume dominance
        maxVolumeDominance = dominanceWindow.stream().max(Double::compareTo).orElse(1.0);

        // Normalize the dominance score to a scale of -10 to 10 using the dynamic maximum
        normalizedScore = Math.max(-10, Math.min(10, (dominance / maxVolumeDominance) * 10));

        // Generate buy/sell signals based on the normalized score
        signalIndicator.addPoint(normalizedScore);

        // Set color based on the normalized score
        if (normalizedScore > 0) {
            int greenIntensity = (int) (255 * (normalizedScore / 10)); // Max intensity at normalizedScore = 10
            signalIndicator.setColor(new Color(0, greenIntensity, 0));
        } else if (normalizedScore < 0) {
            int redIntensity = (int) (255 * (-normalizedScore / 10)); // Max intensity at normalizedScore = -10
            signalIndicator.setColor(new Color(redIntensity, 0, 0));
        } else {
            signalIndicator.setColor(Color.GRAY);
        }

        // Place visual markers for strong buy/sell signals
        if (normalizedScore >= SIGNAL_THRESHOLD) {
            placeMarker("Buy Signal Detected!", true);
            sellSignalMarker.addPoint(0); // Clear sell signal marker
        } else if (normalizedScore <= -SIGNAL_THRESHOLD) {
            placeMarker("Sell Signal Detected!", false);
            buySignalMarker.addPoint(0); // Clear buy signal marker
        }

        // Implement market making logic
        implementMarketMakingStrategy();

        // Reset volumes for the next interval
        totalBuyVolume = 0;
        totalSellVolume = 0;

        // Update the profit indicator at every interval
        profitIndicator.addPoint(totalProfit);
    }

    private void placeMarker(String message, boolean isBuySignal) {
        System.out.println(message);

        double markerPrice = isBuySignal ? latestBidPrice : latestAskPrice;

        if (isBuySignal) {
            buySignalMarker.addPoint(markerPrice);
            buySignalMarker.setColor(Color.GREEN);
            sellSignalMarker.addPoint(0); // Clear the sell signal marker
        } else {
            sellSignalMarker.addPoint(markerPrice);
            sellSignalMarker.setColor(Color.RED);
            buySignalMarker.addPoint(0); // Clear the buy signal marker
        }
    }

    private void implementMarketMakingStrategy() {
        // Logic to simulate buying and selling shares

        int buyOrderPrice = latestBidPrice - 1; // Adjust this based on your strategy
        int sellOrderPrice = latestAskPrice + 1; // Adjust this based on your strategy

        // Example: Simulate buying shares
        if (shouldBuy()) {
            buyShares(buyOrderPrice); // Simulate buying shares worth $1000
        }

        // Example: Simulate selling shares
        if (shouldSell()) {
            sellShares(sellOrderPrice); // Simulate selling shares worth $1000
        }
    }

    private boolean shouldBuy() {
        // Implement your logic to decide whether to simulate buying shares
        return normalizedScore >= (SIGNAL_THRESHOLD / 2); // Lower threshold for more frequent buys
    }

    private boolean shouldSell() {
        // Implement your logic to decide whether to simulate selling shares
        return normalizedScore <= -(SIGNAL_THRESHOLD / 2); // Lower threshold for more frequent sells
    }

    private void buyShares(int price) {
        int quantity = (int) (INVESTMENT_AMOUNT / price);
        boughtShares += quantity;
        buyPrice = price;
        System.out.println("Simulated buying " + quantity + " shares at " + price);
    }

    private void sellShares(int price) {
        if (boughtShares > 0) {
            int quantity = (int) (INVESTMENT_AMOUNT / buyPrice); // Use the initial quantity bought
            double profit = (price - buyPrice) * quantity;
            totalProfit += profit;
            boughtShares -= quantity;
            System.out.println("Simulated selling " + quantity + " shares at " + price + ". Profit: " + profit);
            System.out.println("Total profit: " + totalProfit);
            // Update the profit indicator
            profitIndicator.addPoint(totalProfit);
        } else {
            System.out.println("Not enough shares to sell.");
        }
    }
}
