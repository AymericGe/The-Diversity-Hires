
package engine;

import currency.PlayerAssets;
import currency.PurchaseResult;
import entity.BulletType;
import entity.Ship;
import entity.ShipType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import shop.ShopController;
import shop.ShopProducts;
import shop.ShopTestFixtures;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CrossShipUpgradeIntegrationTest {

    @TempDir
    Path temp;

    @Test
    void purchasedRapidBurstIsAvailableForOtherShips()
            throws Exception {

        PlayerAssets assets =
                ShopTestFixtures.fundedAssets();

        ShopTestFixtures.SaveFile save =
                new ShopTestFixtures.SaveFile(
                        Core.getFileManager(),
                        temp.resolve("assets.properties"));

        ShopController shop =
                ShopTestFixtures.upgradeController(assets, save);

        // Before purchasing, Rapid Burst is locked.
        CampaignUpgrades before =
                CampaignUpgrades.from(shop.snapshot());

        assertFalse(before.hasRapidBurst());

        // Purchase Rapid Burst for 250 Credits.
        assertEquals(
                PurchaseResult.SUCCESS,
                shop.purchase(ShopProducts.RAPID_BURST_ID));

        assertEquals(750, shop.getBalance());

        // The next stage receives updated upgrades.
        CampaignUpgrades nextStage =
                CampaignUpgrades.from(shop.snapshot());

        assertTrue(nextStage.hasRapidBurst());

        // Test both supported ships.
        ShipType[] supportedShips = {
                ShipType.STANDARD,
                ShipType.TANK
        };

        for (ShipType type : supportedShips) {

            Ship ship = new Ship(200, 400, type);

            nextStage.initializeShip(ship);

            // Original weapon remains available.
            assertEquals(
                    type.getBulletType(),
                    ship.getCurrentBulletType());

            // Switch to Rapid Burst.
            ship.setBulletType(BulletType.RAPID);

            assertEquals(
                    BulletType.RAPID,
                    ship.getCurrentBulletType());

            // Switch back to the original weapon.
            ship.setBulletType(type.getBulletType());

            assertEquals(
                    type.getBulletType(),
                    ship.getCurrentBulletType());
        }

        // The upgrade cannot be purchased twice.
        assertEquals(
                PurchaseResult.ALREADY_UNLOCKED,
                shop.purchase(ShopProducts.RAPID_BURST_ID));

        assertEquals(750, shop.getBalance());
    }
}
