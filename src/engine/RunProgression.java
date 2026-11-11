
package engine;

import currency.PlayerAssets;
import currency.PlayerAssetsState;

/**
 * Stores temporary progression for a single game run.
 *
 * Run Credits and combat upgrades are independent
 * from permanently saved account assets.
 */
public final class RunProgression {

    private final PlayerAssets runAssets;

    public RunProgression() {
        this.runAssets = new PlayerAssets();
    }

    /**
     * Shared assets used by run-specific
     * purchases and reward systems.
     */
    public PlayerAssets getAssets() {
        return runAssets;
    }

    /**
     * Returns the current run progression.
     */
    public PlayerAssetsState snapshot() {
        return runAssets.snapshot();
    }

    /**
     * Returns Credits earned during this run.
     */
    public int getBalance() {
        return runAssets.getBalance();
    }

    /**
     * Returns combat upgrades purchased in this run.
     */
    public CampaignUpgrades getUpgrades() {
        return CampaignUpgrades.from(snapshot());
    }

    /**
     * Starts a new run with zero Credits
     * and no purchased combat upgrades.
     *
     * The PlayerAssets object is preserved so
     * existing services can keep their reference.
     */
    public void reset() {
        PlayerAssetsState emptyState =
                new PlayerAssetsState(0, null, null);

        if (!runAssets.restore(emptyState)) {
            throw new IllegalStateException(
                    "Failed to reset run progression.");
        }
    }
}
