
## 1. Team Introduction
Our ambition is to design and implement a dynamic, robust, and extensible Item System for Space Invaders SDP[cite: 2]. The vision is to enhance gameplay depth and player engagement by introducing collectible items, power-ups, and inventory mechanics that seamlessly interact with combat and progression systems.

### Members & Roles

| Name | Role | GitHub Profile |
| :--- | :--- | :--- |
| **Leo Enaux** | Team Leader & Core Architecture | [@leoenaux](https://github.com/leoenaux) |
| **Mathéo SUHR** | Collision & Spawning Developer | [@Matheo92i](https://github.com/Matheo92i) |
| **Clarisse Majourau** | Twin Cannon Developer | [@clarissemajourau](https://github.com/clarissemajourau) |
| **Brieuc VIOUGEAT** | Chameleon Hull Developer | [@BrieucVio](https://github.com/BrieucVio) |
| **Raphaelle Amar** | Cluster Grenade Developer | [@Raphaelle94](https://github.com/Raphaelle94) |
| **Inès Safir** | Hyperfire Buff Developer | [@ines-saphir](https://github.com/ines-saphir) |
| **Manel Belfedhal** | Orbital Annihilator Developer | [@belfedhal-manel](https://github.com/belfedhal-manel) |
| **Aurele Ouary** | Inventory & Buff State Developer | [@aureleouaryPro](https://github.com/aureleouaryPro) |

---

## 2. Team Requirements
We are developing the **Item System** (Requirement 5) of the Space Invaders game[cite: 2]. This module manages the lifecycle, attributes, spawning logic, and effects of all in-game items[cite: 1, 2]. It acts as the backbone for player enhancements, temporary combat buffs, active sub-weapons, and consumable rewards during gameplay.

---

## 3. Detailed Requirements (Item Behaviors)

1. **Twin Cannon Upgrade (Double Barrel):** Spawns dynamically midway through Level 2 upon reaching a wave threshold. When collected by the ship, it upgrades the player's standard single-projectile fire into a dual-stream parallel volley for 25 seconds. If the player loses a life during this period, the buff expires immediately.
2. **Chameleon Hull (Color-Shifter):** Unlocks and spawns during Level 3. Upon collection, it causes the player's ship sprite to cycle continuously through a dynamic palette of colors every 2 seconds. While active, it grants a passive 10% movement speed increase.
3. **Cluster Grenade Launcher:** Equips the player ship with an explosive ordnance sub-weapon. Launches slow-moving grenades that detonate upon impact with an alien or upon reaching a fixed altitude, splitting into 6 fragments that deliver localized Area-of-Effect (AoE) splash damage to nearby enemy clusters.
4. **Overclocked Fire Rate (Hyperfire):** A high-intensity combat pickup that temporarily decreases the player ship's weapon firing cooldown delay by 60% (from 400ms to 160ms between shots) for a duration of 10 seconds, enabling rapid-fire suppressive bursts against advancing formations.
5. **Orbital Annihilator (Doomsday Laser):** A rare, high-tier ultimate item introduced in Level 7. When triggered, it initiates a 1.5-second charging phase followed by a full-screen vertical piercing energy beam lasting 2 seconds that vaporizes all standard enemy ranks and neutralizes incoming enemy projectiles in its corridor.

---

## 4. Dependencies on Other Teams

1. **Player & Enemy Ship Variety (Requirement 9):**
   * *Dependency:* The Twin Cannon, Cluster Grenade, and Hyperfire items directly modify ship attributes, firing intervals, and projectile spawn offsets inside `PlayerShip.java`[cite: 2].
2. **Gameplay HUD (Requirement 8):**
   * *Dependency:* Active temporary item buffs (such as the 10-second Hyperfire cooldown buff and the 25-second Twin Cannon timer) require visual timer countdowns or status icons drawn on the player HUD[cite: 2].
3. **Sound Effects / BGM (Requirement 1):**
   * *Dependency:* Audio triggers are required for item collection feedback, firing mode audio variations, grenade explosions, and the laser beam charging sound effect[cite: 2].
