
package engine;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import event.EnemyDefeatedEvent;
import event.GameplayEventPublisher;
import event.LevelCompletedEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class RunRewardIsolationTest {

    @TempDir
    Path temp;

    @Test
    void campaignRewardsDoNotOverwriteAccountSave()
            throws Exception {

        FileManager files = Core.getFileManager();
        Path savePath = temp.resolve("account.properties");

        PlayerAssets account = new PlayerAssets();
        assertTrue(account.restore(
                new PlayerAssetsState(
                        1000,
                        Set.of("ship.speedster"),
                        null)));

        files.savePlayerAssets(account.snapshot(), savePath);
        byte[] originalSave = Files.readAllBytes(savePath);

        RunProgression run = new RunProgression();

        CampaignRewards rewards = new CampaignRewards(
                run.getAssets(),
                files,
                savePath,
                false);

        rewards.startRun("campaign-run");

        GameplayEventPublisher events =
                new GameplayEventPublisher();

        try (CampaignRewards.StageSubscription subscription =
                     rewards.subscribe(events, 1)) {

            events.publish(new LevelCompletedEvent(
                    1, GameplayConfig.NORMAL));
        }

        assertEquals(100, run.getBalance());
        assertEquals(1000, account.getBalance());

        assertArrayEquals(
                originalSave,
                Files.readAllBytes(savePath));

        run.reset();

        assertEquals(0, run.getBalance());
        assertTrue(account.isUnlocked("ship.speedster"));
    }

    @Test
    void endlessRewardsRemainTemporary()
            throws Exception {

        FileManager files = Core.getFileManager();
        Path savePath = temp.resolve("endless.properties");

        RunProgression run = new RunProgression();

        EndlessRewards rewards = new EndlessRewards(
                run.getAssets(),
                files,
                savePath,
                false);

        GameplayEventPublisher events =
                new GameplayEventPublisher();

        try (EndlessRewards.RunSubscription subscription =
                     rewards.subscribe(events, "endless-run")) {

            events.publish(new EnemyDefeatedEvent(
                    "endless-run", "enemy-1"));
        }

        assertEquals(10, run.getBalance());

        // Temporary rewards must not create an account save.
        assertFalse(Files.exists(savePath));

        run.reset();

        assertEquals(0, run.getBalance());
    }
}
