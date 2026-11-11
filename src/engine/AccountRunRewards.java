
package engine;

import currency.PlayerAssets;
import currency.RewardSystem;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Grants permanent Account Credits after completed runs.
 * Prevents repeated rewards for the same run ID.
 */
final class AccountRunRewards {

    private static final String REWARD_ID = "ACCOUNT_RUN_REWARD";

    private final RewardSystem rewards;
    private final RewardSettlementCoordinator settlement;
    private final Set<String> processedRuns = new HashSet<>();

    private SettlementResult lastResult = SettlementResult.SUCCESS;

    AccountRunRewards(PlayerAssets assets, FileManager files) {
        this(assets, files, null);
    }

    AccountRunRewards(PlayerAssets assets, FileManager files,
                      Path savePath) {

        rewards = new RewardSystem(assets);

        settlement = new RewardSettlementCoordinator(
                rewards, assets, files, savePath);
    }

    SettlementResult awardRun(String runId, int credits) {

        if (runId == null || runId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Run ID cannot be empty.");
        }

        if (credits < 0) {
            throw new IllegalArgumentException(
                    "Credits cannot be negative.");
        }

        if (processedRuns.contains(runId)) {
            return retryPendingRewards();
        }

        if (credits > 0) {
            if (!rewards.setEnemyReward(REWARD_ID, credits)
                    || !rewards.rewardEnemy(REWARD_ID)) {
                return SettlementResult.SETTLEMENT_FAILED;
            }
        }

        processedRuns.add(runId);
        return retryPendingRewards();
    }

    SettlementResult retryPendingRewards() {

        if (rewards.getPendingCurrency() > 0) {
            lastResult = settlement.processSettlement();
            return lastResult;
        }

        if (lastResult == SettlementResult.SAVE_FAILED) {
            lastResult = settlement.retrySave();
        }

        return lastResult;
    }
}
