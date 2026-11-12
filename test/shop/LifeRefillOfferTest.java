
package shop;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import currency.RunLifePurchases;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LifeRefillOfferTest {

    @Test
    void successfulPurchaseRestoresHpAndDeductsCredits() {
        PlayerAssets assets = new PlayerAssets();

        assertTrue(assets.restore(
                new PlayerAssetsState(
                        1000,
                        Set.of(
                                "upgrade.hull.1",
                                "upgrade.hull.2",
                                "upgrade.hull.3"),
                        null)));

        AtomicInteger hp = new AtomicInteger(4);
        AtomicInteger maxHp = new AtomicInteger(7);
        AtomicLong activeTime = new AtomicLong(1000L);

        RunLifePurchases purchases =
                new RunLifePurchases(
                        RunLifePurchases.Mode.ENDLESS);

        LifeRefillOffer offer = new LifeRefillOffer(
                purchases,
                assets,
                hp::get,
                maxHp::get,
                activeTime::get,
                hp::incrementAndGet);

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                offer.buyLife());

        assertEquals(5, hp.get());
        assertEquals(750, assets.getBalance());

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                offer.buyLife());

        assertEquals(6, hp.get());
        assertEquals(500, assets.getBalance());

        assertEquals(0L,
                purchases.getRemainingCooldownMillis(
                        activeTime.get()));

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                offer.buyLife());

        assertEquals(7, hp.get());
        assertEquals(250, assets.getBalance());

        // Third purchase starts the cooldown.
        assertEquals(30_000L,
                purchases.getRemainingCooldownMillis(
                        activeTime.get()));

        // No active gameplay time passes while in Shop.
        assertEquals(30_000L,
                purchases.getRemainingCooldownMillis(
                        activeTime.get()));

        activeTime.set(31_000L);

        assertEquals(3,
                purchases.getAvailablePurchases(
                        activeTime.get()));

        // Simulate damage during gameplay.
        hp.set(4);

        assertEquals(
                RunLifePurchases.Result.SUCCESS,
                offer.buyLife());

        assertEquals(5, hp.get());
        assertEquals(0, assets.getBalance());
    }

    @Test
    void failedPurchaseNeverRestoresHp() {
        PlayerAssets assets = new PlayerAssets();

        assertTrue(assets.restore(
                new PlayerAssetsState(1000, null, null)));

        AtomicInteger hp = new AtomicInteger(2);

        LifeRefillOffer offer = new LifeRefillOffer(
                new RunLifePurchases(
                        RunLifePurchases.Mode.CAMPAIGN),
                assets,
                hp::get,
                () -> 4,
                () -> 0L,
                hp::incrementAndGet);

        assertEquals(
                RunLifePurchases.Result.HULL_NOT_MAXED,
                offer.buyLife());

        assertEquals(2, hp.get());
        assertEquals(1000, assets.getBalance());
    }
}
