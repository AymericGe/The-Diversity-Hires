package entity;

import engine.DrawManager.SpriteType;
import engine.GameColors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnemyShipColorTest {

    @Test
    @DisplayName("Enemy type A has color ENEMY_A")
    void enemyAHasColorA() {
        // Arrange
        SpriteType type = SpriteType.EnemyShipA1;
        // Act
        EnemyShip enemy = new EnemyShip(100, 100, type);
        // Assert
        assertEquals(GameColors.ENEMY_A, enemy.getColor());
    }

    @Test
    @DisplayName("Enemy type B has color ENEMY_B")
    void enemyBHasColorB() {
        EnemyShip enemy = new EnemyShip(100, 100, SpriteType.EnemyShipB1);
        assertEquals(GameColors.ENEMY_B, enemy.getColor());
    }

    @Test
    @DisplayName("Enemy type C has color ENEMY_C")
    void enemyCHasColorC() {
        EnemyShip enemy = new EnemyShip(100, 100, SpriteType.EnemyShipC1);
        assertEquals(GameColors.ENEMY_C, enemy.getColor());
    }

    @Test
    @DisplayName("Second animation frame keeps the same color")
    void secondFrameSameColor() {
        EnemyShip enemy = new EnemyShip(100, 100, SpriteType.EnemyShipB2);
        assertEquals(GameColors.ENEMY_B, enemy.getColor());
    }

    @Test
    @DisplayName("Bonus ship has color ENEMY_SPECIAL")
    void bonusShipHasSpecialColor() {
        EnemyShip bonus = new EnemyShip();
        assertEquals(GameColors.ENEMY_SPECIAL, bonus.getColor());
    }
}
