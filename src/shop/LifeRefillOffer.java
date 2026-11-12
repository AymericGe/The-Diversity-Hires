
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
    public static final String PRODUCT_ID = "run.life_refill";

    private final RunLifePurchases purchases;
    private final PlayerAssets assets;
    private final IntSupplier currentHp;
    private final IntSupplier maxHp;
    private final LongSupplier activeTime;
    private final Runnable restoreLife;
    private int knownMaxHp;

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
        this.knownMaxHp = maxHp.getAsInt();
    }

    public boolean isUnlocked() {
        return PermanentUpgrade.HULL.getLevel(
                assets.snapshot())
                == PermanentUpgrade.HULL.getMaxLevel();
    }

    /**
     * Fill newly purchased Hull slots with actual HP.
     * Existing missing HP is not restored.
     */
    public void fillPurchasedHullSlots() {
        int newMaxHp = maxHp.getAsInt();
        int gainedSlots = Math.max(
                0, newMaxHp - knownMaxHp);

        for (int i = 0; i < gainedSlots; i++) {
            if (currentHp.getAsInt() >= newMaxHp) {
                break;
            }

            restoreLife.run();
        }

        knownMaxHp = newMaxHp;
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
            return "Life Refill | Requires Hull 3/3";
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

        return "Life Refill | "
                + RunLifePurchases.LIFE_PRICE
                + " C | HP "
                + currentHp.getAsInt()
                + "/" + maxHp.getAsInt()
                + " | Left: " + available;
    }
}
