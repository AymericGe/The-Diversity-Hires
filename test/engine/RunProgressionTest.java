
package engine;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import currency.RewardSystem;
import entity.ShipType;
import shop.ShopProducts;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunProgressionTest {

    @Test
    void runStartsWithZeroCreditsAndNoUpgrades() {

        RunProgression run = new RunProgression();

        assertEquals(0, run.getBalance());

        assertTrue(
                run.snapshot().getUnlockedContentIds().isEmpty());

        assertFalse(run.getUpgrades().hasRapidBurst());

        assertEquals(
                ShipType.STANDARD.getMaxLives(),
                run.getUpgrades().getMaxLives(ShipType.STANDARD));
    }

    @Test
    void resetClearsOnlyRunProgression() {

        // Simulate an account with permanent progress.
        PlayerAssets account = new PlayerAssets();

        assertTrue(account.restore(
                new PlayerAssetsState(
                        1000,
                        Set.of("ship.speedster"),
                        null)));

        RunProgression run = new RunProgression();

        // Simulate purchased temporary upgrades.
        assertTrue(run.getAssets().restore(
                new PlayerAssetsState(
                        500,
                        Set.of(
                                "upgrade.hull.1",
                                ShopProducts.RAPID_BURST_ID),
                        null)));

        assertEquals(500, run.getBalance());

        assertTrue(run.getUpgrades().hasRapidBurst());

        assertEquals(
                5,
                run.getUpgrades().getMaxLives(ShipType.STANDARD));

        // Start a new run.
        run.reset();

        assertEquals(0, run.getBalance());

        assertFalse(run.getUpgrades().hasRapidBurst());

        assertTrue(
                run.snapshot().getUnlockedContentIds().isEmpty());

        assertEquals(
                4,
                run.getUpgrades().getMaxLives(ShipType.STANDARD));

        // Permanent account data must remain unchanged.
        assertEquals(1000, account.getBalance());

        assertTrue(account.isUnlocked("ship.speedster"));
    }

    @Test
    void resetPreservesReferencesUsedByRewardSystems() {

        RunProgression run = new RunProgression();

        PlayerAssets sharedAssets = run.getAssets();

        RewardSystem rewards =
                new RewardSystem(sharedAssets);

        assertTrue(rewards.rewardEnemy("A"));
        assertTrue(rewards.settle());

        assertEquals(10, run.getBalance());

        run.reset();

        // RewardSystem must still reference the same assets.
        assertSame(sharedAssets, run.getAssets());

        assertEquals(0, run.getBalance());

        assertTrue(rewards.rewardEnemy("A"));
        assertTrue(rewards.settle());

        assertEquals(10, run.getBalance());
    }
}
