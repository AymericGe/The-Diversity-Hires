package engine;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import currency.RewardSystem;

import java.io.IOException;
import java.nio.file.Path;

public class RewardSettlementCoordinator {

    private final RewardSystem rewardSystem;
    private final PlayerAssets playerAssets;
    private final FileManager fileManager;
    private final Path customSavePath;
    private final boolean saveToDisk;


    RewardSettlementCoordinator(
            RewardSystem rewardSystem,
            PlayerAssets playerAssets,
            FileManager fileManager,
            Path customSavePath) {

        this(rewardSystem, playerAssets, fileManager,
                customSavePath, true);
    }

    RewardSettlementCoordinator(
            RewardSystem rewardSystem,
            PlayerAssets playerAssets,
            FileManager fileManager,
            Path customSavePath,
            boolean saveToDisk) {

        if (rewardSystem == null
                || playerAssets == null
                || fileManager == null) {
            throw new IllegalArgumentException(
                    "Dependencies cannot be null.");
        }

        this.rewardSystem = rewardSystem;
        this.playerAssets = playerAssets;
        this.fileManager = fileManager;
        this.customSavePath = customSavePath;
        this.saveToDisk = saveToDisk;
    }


    /**
     * Settles accumulated rewards and saves the updated asset state to disk.
     *
     * Rejects settlement if the asset balance overflows, preserving the pending rewards.
     * If saving fails after a successful settlement, the rewards remain in the balance.
     * Use retrySave() to retry persistence without settling newly accumulated rewards.
     * Calling this method again requests a new settlement before saving.
     *
     * @return SUCCESS if rewards were settled and saved; SETTLEMENT_FAILED if the transfer to assets was rejected; SAVE_FAILED if disk persistence failed
     */
    public SettlementResult processSettlement() {
        if (!rewardSystem.settle()) {
            return SettlementResult.SETTLEMENT_FAILED;
        }

        return saveLatestAssets();
    }

    /**
     * Saves the latest shared assets without transferring or clearing pending rewards.
     * Captures a fresh snapshot on every call, including changes made after a failed save.
     * A failed retry leaves both runtime assets and pending rewards unchanged.
     *
     * @return SUCCESS if the latest snapshot was saved; SAVE_FAILED if disk persistence failed
     */
    public SettlementResult retrySave() {
        return saveLatestAssets();
    }

    private SettlementResult saveLatestAssets() {
        // Run Credits exist only during the current run.
        // Never overwrite the permanent account save.
        if (!saveToDisk) {
            return SettlementResult.SUCCESS;
        }
        PlayerAssetsState snapshot = playerAssets.snapshot();

        try {
            if (customSavePath != null) {
                fileManager.savePlayerAssets(snapshot, customSavePath);
            } else {
                fileManager.savePlayerAssets(snapshot);
            }
            return SettlementResult.SUCCESS;
        } catch (IOException | SecurityException e) {
            Core.getLogger().warning("Reward save failed; runtime assets retained: " + e);
            return SettlementResult.SAVE_FAILED;
        }
    }
}
