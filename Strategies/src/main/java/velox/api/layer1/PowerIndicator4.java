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
@Layer1StrategyName("Enhanced Power Indicator4")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class PowerIndicator4 implements CustomModule, IntervalListener, SnapshotEndListener, DepthDataListener {

    private Indicator buyVolumeIndicator;
    private Indicator sellVolumeIndicator;
    private Indicator signalIndicator;
    private Indicator buySignalMarker;
    private Indicator sellSignalMarker;

    private double totalBuyVolume = 0;
    private double totalSellVolume = 0;
    private double maxVolumeDominance = 0;

    private static final int WINDOW_SIZE = 60; // Example window size in seconds (1 minute)
    private static final double SIGNAL_THRESHOLD = 8.0; // Threshold for placing markers
    private Queue<Double> dominanceWindow = new LinkedList<>();

    private int latestBidPrice = 0;
    private int latestAskPrice = 0;

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        buyVolumeIndicator = api.registerIndicator("Buy Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        buyVolumeIndicator.setColor(Color.GREEN);

        sellVolumeIndicator = api.registerIndicator("Sell Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellVolumeIndicator.setColor(Color.RED);

        signalIndicator = api.registerIndicator("Volume Signal", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);

        // Registering new indicators for buy and sell signal markers
        buySignalMarker = api.registerIndicator("Buy Signal Marker", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellSignalMarker = api.registerIndicator("Sell Signal Marker", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
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
        double normalizedScore = Math.max(-10, Math.min(10, (dominance / maxVolumeDominance) * 10));

        // Generate buy/sell signals based on the normalized score
        signalIndicator.addPoint(normalizedScore);

        // Set color based on the normalized score
        if (normalizedScore > 0) {
            // Bright green for strong buy signals
            int greenIntensity = (int) (255 * (normalizedScore / 10)); // Max intensity at normalizedScore = 10
            signalIndicator.setColor(new Color(0, greenIntensity, 0));
        } else if (normalizedScore < 0) {
            // Bright red for strong sell signals
            int redIntensity = (int) (255 * (-normalizedScore / 10)); // Max intensity at normalizedScore = -10
            signalIndicator.setColor(new Color(redIntensity, 0, 0));
        } else {
            // Neutral color
            signalIndicator.setColor(Color.GRAY);
        }

        // Place visual markers for strong buy/sell signals
        if (normalizedScore >= SIGNAL_THRESHOLD) {
            placeMarker("Strong Buy Signal Detected!", true);
            sellSignalMarker.addPoint(0); // Clear sell signal marker
        } else if (normalizedScore <= -SIGNAL_THRESHOLD) {
            placeMarker("Strong Sell Signal Detected!", false);
            buySignalMarker.addPoint(0); // Clear buy signal marker
        }

        // Reset volumes for the next interval
        totalBuyVolume = 0;
        totalSellVolume = 0;
    }

    private void placeMarker(String message, boolean isBuySignal) {
        // Implement alert mechanism (e.g., pop-up, sound)
        System.out.println(message); // Simple console output for illustration
        // You can replace this with more sophisticated alert mechanisms (e.g., UI pop-up, sound notification)

        // Determine the price to use for the marker
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
}