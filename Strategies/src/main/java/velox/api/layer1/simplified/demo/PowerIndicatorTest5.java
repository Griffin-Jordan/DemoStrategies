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
@Layer1StrategyName("PowerIndicatorT5")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class PowerIndicatorTest5 implements CustomModule, IntervalListener, SnapshotEndListener, DepthDataListener {

    private Indicator buyVolumeIndicator;
    private Indicator sellVolumeIndicator;
    private Indicator profitIndicator;

    private double totalBuyVolume = 0; // Total buy volume in the current interval
    private double totalSellVolume = 0; // Total sell volume in the current interval

    private double profit = 0; // Current profit
    private double cash = 10000; // Starting cash
    private double position = 0; // Current long position in units
    private double shortPosition = 0; // Current short position in units
    private double lastPrice = 0; // Last trade price

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        // Register indicators and set their colors
        buyVolumeIndicator = api.registerIndicator("Buy Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        buyVolumeIndicator.setColor(Color.GREEN);

        sellVolumeIndicator = api.registerIndicator("Sell Volume", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        sellVolumeIndicator.setColor(Color.RED);

        profitIndicator = api.registerIndicator("Profit", Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY);
        profitIndicator.setColor(Color.BLUE);
    }

    @Override
    public void stop() {
        // Clean up resources if needed
    }

    @Override
    public void onSnapshotEnd() {
        // This method is called when the snapshot ends
        System.out.println("Snapshot Ended: Order book is ready for processing.");
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        // This method is called when there is a change in the order book
        if (size > 0) {
            if (isBid) {
                // Add to total buy volume if it's a bid
                totalBuyVolume += size;
            } else {
                // Add to total sell volume if it's an ask
                totalSellVolume += size;
            }
            // Update the last price
            lastPrice = price;
        }
    }

    @Override
    public long getInterval() {
        // Define the interval for the onInterval method
        return Intervals.INTERVAL_1_SECOND;
    }

    @Override
    public void onInterval() {
        // Add the volumes to the indicators
        buyVolumeIndicator.addPoint(totalBuyVolume);
        sellVolumeIndicator.addPoint(totalSellVolume);

        // Trading logic
        if (totalBuyVolume > totalSellVolume) {
            // Simulate buying if buy volume is greater than sell volume
            double unitsToBuy = 1000 / lastPrice;
            if (cash >= 1000) {
                cash -= 1000;
                position += unitsToBuy;
                System.out.println("Bought " + unitsToBuy + " units at price " + lastPrice);
            }
            // Close short positions if any
            if (shortPosition > 0) {
                double unitsToCover = 1000 / lastPrice;
                if (cash >= unitsToCover * lastPrice) {
                    cash -= unitsToCover * lastPrice;
                    shortPosition -= unitsToCover;
                    System.out.println("Covered short " + unitsToCover + " units at price " + lastPrice);
                }
            }
        } else if (totalSellVolume > totalBuyVolume) {
            // Simulate selling if sell volume is greater than buy volume
            double unitsToSell = 1000 / lastPrice;
            if (position >= unitsToSell) {
                cash += 1000;
                position -= unitsToSell;
                System.out.println("Sold " + unitsToSell + " units at price " + lastPrice);
            } else {
                // Simulate short selling if not enough position to sell
                double unitsToShort = 1000 / lastPrice;
                cash += 1000;
                shortPosition += unitsToShort;
                System.out.println("Shorted " + unitsToShort + " units at price " + lastPrice);
            }
        }

        // Calculate current profit including short positions
        profit = cash + (position * lastPrice) - (shortPosition * lastPrice) - 10000; // Initial cash was 10000
        profitIndicator.addPoint(profit);

        // Reset volumes for the next interval
        totalBuyVolume = 0;
        totalSellVolume = 0;
    }
}