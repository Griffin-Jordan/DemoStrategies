package velox.api.layer1.simplified.demo;

import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.*;

import java.awt.*;

@Layer1SimpleAttachable
@Layer1StrategyName("Enhanced Power Indicator")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class PowerIndicator2 implements CustomModule, IntervalListener, SnapshotEndListener, DepthDataListener {

    private Indicator buyVolumeIndicator;
    private Indicator sellVolumeIndicator;
    private Indicator signalIndicator;

    private double totalBuyVolume = 0;
    private double totalSellVolume = 0;

    private static final double MAX_VOLUME_DOMINANCE = 5000; // Example maximum volume dominance for normalization

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        buyVolumeIndicator = api.registerIndicator("Buy Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        buyVolumeIndicator.setColor(Color.GREEN);

        sellVolumeIndicator = api.registerIndicator("Sell Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellVolumeIndicator.setColor(Color.RED);

        signalIndicator = api.registerIndicator("Volume Signal", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
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
            } else {
                totalSellVolume += size;
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

        // Normalize the dominance score to a scale of -5 to 5
        double normalizedScore = Math.max(-5, Math.min(5, (dominance / MAX_VOLUME_DOMINANCE) * 5));

        // Generate buy/sell signals based on the normalized score
        signalIndicator.addPoint(normalizedScore);

        // Set color based on the normalized score
        if (normalizedScore > 0) {
            // Bright green for strong buy signals
            int greenIntensity = (int) (255 * (normalizedScore / 5)); // Max intensity at normalizedScore = 5
            signalIndicator.setColor(new Color(0, greenIntensity, 0));
        } else if (normalizedScore < 0) {
            // Bright red for strong sell signals
            int redIntensity = (int) (255 * (-normalizedScore / 5)); // Max intensity at normalizedScore = -5
            signalIndicator.setColor(new Color(redIntensity, 0, 0));
        } else {
            // Neutral color
            signalIndicator.setColor(Color.GRAY);
        }

// Reset volumes for the next interval
        totalBuyVolume = 0;
        totalSellVolume = 0;
    }
}