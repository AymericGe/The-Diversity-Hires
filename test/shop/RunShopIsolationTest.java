
package shop;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import currency.PurchaseResult;
import engine.Core;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunShopIsolationTest {

    @Test
    void runPurchasesDoNotModifyPermanentAccount() {

        PlayerAssets accountAssets = new PlayerAssets();

        assertTrue(accountAssets.restore(
                new PlayerAssetsState(
                        1000,
                        Set.of("ship.speedster"),
                        null)));

        PlayerAssets runAssets = new PlayerAssets();

        assertTrue(runAssets.restore(
                new PlayerAssetsState(500, null, null)));

        ShopController runShop =
                ShopController.forRun(
                        runAssets,
                        Core.getFileManager());

        assertEquals(
                4,
                runShop.getProducts().size());

        assertEquals(
                PurchaseResult.SUCCESS,
                runShop.purchase(
                        ShopProducts.RAPID_BURST_ID));

        assertEquals(250, runShop.getBalance());

        assertTrue(runAssets.isUnlocked(
                ShopProducts.RAPID_BURST_ID));

        assertFalse(accountAssets.isUnlocked(
                ShopProducts.RAPID_BURST_ID));

        assertEquals(1000, accountAssets.getBalance());

        assertTrue(accountAssets.isUnlocked(
                "ship.speedster"));

        assertEquals(
                ShopController.SaveStatus.NOT_NEEDED,
                runShop.getSaveStatus());
    }

    @Test
    void runShopCannotPurchasePermanentShips() {

        ShopController runShop =
                ShopController.forRun(
                        new PlayerAssets(),
                        Core.getFileManager());

        assertEquals(
                PurchaseResult.CATEGORY_MISMATCH,
                runShop.purchase("ship.speedster"));
    }

    @Test
    void accountShopContainsOnlyShips() {

        assertEquals(3,
                ShopProducts.accountProducts().size());

        assertEquals(10,
                ShopProducts.runProducts().size());

        assertTrue(
                ShopProducts.runProducts().stream()
                        .noneMatch(p ->
                                p.getShipType() != null));
    }
}
