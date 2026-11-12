
package shop;

import currency.PermanentUpgrade;
import currency.PlayerAssets;
import currency.RunLifePurchases;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Connects life purchases to the current game's HP.
 */
public final class LifeRefillOffer {

    private final RunLifePurchases purchases;
    private final PlayerAssets assets;
    private final IntSupplier currentHp;
    private final IntSupplier maxHp;
    private final LongSupplier activeTime;
    private final Runnable restoreLife;

    public LifeRefillOffer(
            RunLifePurchases purchases,
            PlayerAssets assets,
            IntSupplier currentHp,
            IntSupplier maxHp,
            LongSupplier activeTime,
            Runnable restoreLife) {

        this.purchases = Objects.requireNonNull(purchases);
        this.assets = Objects.requireNonNull(assets);
        this.currentHp = Objects.requireNonNull(currentHp);
        this.maxHp = Objects.requireNonNull(maxHp);
        this.activeTime = Objects.requireNonNull(activeTime);
        this.restoreLife = Objects.requireNonNull(restoreLife);
    }

    public RunLifePurchases.Result buyLife() {
        RunLifePurchases.Result result = purchases.tryBuy(
                assets,
                currentHp.getAsInt(),
                maxHp.getAsInt(),
                activeTime.getAsLong());

        if (result == RunLifePurchases.Result.SUCCESS) {
            restoreLife.run();
        }

        return result;
    }

    public String getStatusLine() {
        long time = activeTime.getAsLong();

        int hull = PermanentUpgrade.HULL.getLevel(
                assets.snapshot());

        if (hull < PermanentUpgrade.HULL.getMaxLevel()) {
            return "L: Life Refill | Requires Hull 3/3";
        }

        long cooldown =
                purchases.getRemainingCooldownMillis(time);

        if (cooldown > 0) {
            long seconds = (cooldown + 999L) / 1000L;
            return "Life Refill | Cooldown: "
                    + seconds + "s";
        }

        int available =
                purchases.getAvailablePurchases(time);

        return "L: +1 HP | "
                + RunLifePurchases.LIFE_PRICE
                + " C | HP "
                + currentHp.getAsInt()
                + "/" + maxHp.getAsInt()
                + " | Left: " + available;
    }
}
