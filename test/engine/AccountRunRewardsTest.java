
package engine;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AccountRunRewardsTest {

    @TempDir
    Path temp;

    @Test
    void accountCreditsAreSavedWithoutLosingShips()
            throws Exception {

        PlayerAssets account = new PlayerAssets();

        assertTrue(account.restore(
                new PlayerAssetsState(
                        100,
                        Set.of("ship.speedster"),
                        null)));

        Path save = temp.resolve("account.properties");

        AccountRunRewards rewards = new AccountRunRewards(
                account,
                Core.getFileManager(),
                save);

        assertEquals(
                SettlementResult.SUCCESS,
                rewards.awardRun("campaign-1", 90));

        assertEquals(190, account.getBalance());

        assertTrue(account.isUnlocked("ship.speedster"));

        assertEquals(
                190,
                Core.getFileManager()
                        .loadPlayerAssets(save).getBalance());

        // Same run cannot pay twice.
        assertEquals(
                SettlementResult.SUCCESS,
                rewards.awardRun("campaign-1", 90));

        assertEquals(190, account.getBalance());

        // Another run may earn more Credits.
        assertEquals(
                SettlementResult.SUCCESS,
                rewards.awardRun("endless-1", 25));

        assertEquals(215, account.getBalance());
    }

    @Test
    void failedSaveCanRetryWithoutDuplicateCredits()
            throws Exception {

        Path save = temp.resolve("blocked");
        Files.createDirectory(save);
        Path blocker = Files.createFile(
                save.resolve("blocker"));

        PlayerAssets account = new PlayerAssets();

        AccountRunRewards rewards = new AccountRunRewards(
                account,
                Core.getFileManager(),
                save);

        assertEquals(
                SettlementResult.SAVE_FAILED,
                rewards.awardRun("campaign-1", 30));

        assertEquals(30, account.getBalance());

        Files.delete(blocker);
        Files.delete(save);

        assertEquals(
                SettlementResult.SUCCESS,
                rewards.retryPendingRewards());

        assertEquals(30, account.getBalance());

        assertEquals(
                30,
                Core.getFileManager()
                        .loadPlayerAssets(save).getBalance());
    }
}
