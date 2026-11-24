
package currency;

import java.util.Objects;

/**
 * Manages consumable life purchases during one game run.
 * Uses Run Credits without saving purchases to disk.
 */
public final class RunLifePurchases {

    public static final int LIFE_PRICE = 250;
    public static final int MAX_PURCHASES = 3;
    public static final long COOLDOWN_MILLIS = 30_000L;

    public enum Mode {
        CAMPAIGN,
        ENDLESS
    }

    public enum Result {
        SUCCESS,
        HULL_NOT_MAXED,
        FULL_HEALTH,
        NOT_ALIVE,
        INSUFFICIENT_FUNDS,
        LIMIT_REACHED,
        COOLDOWN_ACTIVE
    }

    private final Mode mode;
    private int purchasesUsed;
    private long cooldownStartedAt = -1L;

    public RunLifePurchases(Mode mode) {
        this.mode = Objects.requireNonNull(mode);
    }

    /**
     * Charges one life refill.
     * The game must restore one HP after SUCCESS.
     *
     * activeGameplayMillis excludes time spent in the Shop.
     */
    public Result tryBuy(
            PlayerAssets runAssets,
            int currentHp,
            int maxHp,
            long activeGameplayMillis) {

        Objects.requireNonNull(runAssets);

        if (currentHp < 0 || maxHp <= 0
                || currentHp > maxHp
                || activeGameplayMillis < 0) {
            throw new IllegalArgumentException(
                    "Invalid life purchase arguments.");
        }

        refreshCooldown(activeGameplayMillis);

        if (PermanentUpgrade.HULL.getLevel(
                runAssets.snapshot())
                < PermanentUpgrade.HULL.getMaxLevel()) {
            return Result.HULL_NOT_MAXED;
        }

        if (currentHp == 0) {
            return Result.NOT_ALIVE;
        }

        if (currentHp >= maxHp) {
            return Result.FULL_HEALTH;
        }

        if (purchasesUsed >= MAX_PURCHASES) {
            return mode == Mode.ENDLESS
                    ? Result.COOLDOWN_ACTIVE
                    : Result.LIMIT_REACHED;
        }

        if (!runAssets.deductCurrency(LIFE_PRICE)) {
            return Result.INSUFFICIENT_FUNDS;
        }

        purchasesUsed++;

        // Cooldown starts only after the THIRD purchase.
        if (mode == Mode.ENDLESS
                && purchasesUsed == MAX_PURCHASES) {
            cooldownStartedAt = activeGameplayMillis;
        }

        return Result.SUCCESS;
    }

    public int getAvailablePurchases(
            long activeGameplayMillis) {

        validateTime(activeGameplayMillis);
        refreshCooldown(activeGameplayMillis);

        return MAX_PURCHASES - purchasesUsed;
    }

    public long getRemainingCooldownMillis(
            long activeGameplayMillis) {

        validateTime(activeGameplayMillis);
        refreshCooldown(activeGameplayMillis);

        if (mode != Mode.ENDLESS
                || purchasesUsed < MAX_PURCHASES) {
            return 0L;
        }

        long elapsed =
                activeGameplayMillis - cooldownStartedAt;

        return Math.max(
                0L,
                COOLDOWN_MILLIS - Math.max(0L, elapsed));
    }

    private void refreshCooldown(long activeGameplayMillis) {
        if (mode != Mode.ENDLESS
                || purchasesUsed < MAX_PURCHASES
                || cooldownStartedAt < 0L) {
            return;
        }

        if (activeGameplayMillis >= cooldownStartedAt
                && activeGameplayMillis - cooldownStartedAt
                >= COOLDOWN_MILLIS) {

            purchasesUsed = 0;
            cooldownStartedAt = -1L;
        }
    }

    private void validateTime(long activeGameplayMillis) {
        if (activeGameplayMillis < 0L) {
            throw new IllegalArgumentException(
                    "Active game time cannot be negative.");
        }
    }

    /** Starts another run with fresh purchase limits. */
    public void reset() {
        purchasesUsed = 0;
        cooldownStartedAt = -1L;
    }
}
