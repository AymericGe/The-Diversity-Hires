
package currency;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunLifePurchasesTest {

    private PlayerAssets fundedAssets(int credits) {
        PlayerAssets assets = new PlayerAssets();

        assertTrue(assets.restore(
                new PlayerAssetsState(
                        credits,
                        Set.of(
                                "upgrade.hull.1",
                                "upgrade.hull.2",
                                "upgrade.hull.3"),
                        null)));

        return assets;
    }

    @Test
    void campaignAllowsOnlyThreePurchases() {
        PlayerAssets assets = fundedAssets(1000);

        RunLifePurchases lives =
                new RunLifePurchases(
                        RunLifePurchases.Mode.CAMPAIGN);

        for (int index = 0; index < 3; index++) {
            assertEquals(
                    RunLifePurchases.Result.SUCCESS,
                    lives.tryBuy(
                            assets, 2 + index, 7, 0L));
        }

        assertEquals(250, assets.getBalance());

        assertEquals(
                RunLifePurchases.Result.LIMIT_REACHED,
                lives.tryBuy(assets, 5, 7, 60_000L));

        assertEquals(250, assets.getBalance());
    }

    @Test
    void endlessCooldownStartsOnlyAfterThirdPurchase() {
        PlayerAssets assets = fundedAssets(2000);

        RunLifePurchases lives =
                new RunLifePurchases(
                        RunLifePurchases.Mode.ENDLESS);

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                lives.tryBuy(assets, 1, 7, 1000L));

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                lives.tryBuy(assets, 2, 7, 2000L));

        // Even after 60 seconds, no cooldown starts
        // until the third purchase.
        assertEquals(
                0L,
                lives.getRemainingCooldownMillis(62_000L));

        assertEquals(
                1,
                lives.getAvailablePurchases(62_000L));

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                lives.tryBuy(assets, 3, 7, 62_000L));

        assertEquals(
                30_000L,
                lives.getRemainingCooldownMillis(62_000L));

        assertEquals(
                RunLifePurchases.Result.COOLDOWN_ACTIVE,
                lives.tryBuy(assets, 4, 7, 62_000L));

        assertEquals(
                1L,
                lives.getRemainingCooldownMillis(91_999L));

        // A new purchase cycle becomes available.
        assertEquals(
                3,
                lives.getAvailablePurchases(92_000L));

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                lives.tryBuy(assets, 4, 7, 92_000L));

        assertEquals(1000, assets.getBalance());
    }

    @Test
    void purchasesRequireHullLevelThree() {
        PlayerAssets assets = new PlayerAssets();

        assertTrue(assets.restore(
                new PlayerAssetsState(500, null, null)));

        RunLifePurchases lives =
                new RunLifePurchases(
                        RunLifePurchases.Mode.CAMPAIGN);

        assertEquals(
                RunLifePurchases.Result.HULL_NOT_MAXED,
                lives.tryBuy(assets, 2, 4, 0L));

        assertEquals(500, assets.getBalance());
    }

    @Test
    void fullHealthCannotBePurchased() {
        PlayerAssets assets = fundedAssets(500);

        RunLifePurchases lives =
                new RunLifePurchases(
                        RunLifePurchases.Mode.ENDLESS);

        assertEquals(
                RunLifePurchases.Result.FULL_HEALTH,
                lives.tryBuy(assets, 7, 7, 1000L));

        assertEquals(500, assets.getBalance());

        assertEquals(
                3,
                lives.getAvailablePurchases(1000L));
    }

    @Test
    void newRunResetsPurchaseLimits() {
        PlayerAssets assets = fundedAssets(1000);

        RunLifePurchases lives =
                new RunLifePurchases(
                        RunLifePurchases.Mode.CAMPAIGN);

        lives.tryBuy(assets, 1, 7, 0L);
        lives.tryBuy(assets, 2, 7, 0L);
        lives.tryBuy(assets, 3, 7, 0L);

        assertEquals(0, lives.getAvailablePurchases(0L));

        lives.reset();

        assertEquals(3, lives.getAvailablePurchases(0L));
    }
}
