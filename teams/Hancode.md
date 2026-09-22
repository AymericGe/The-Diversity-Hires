# Team HanCode

## 1. Team Introduction

Requirements: 4. Currency System

Course: CSE2024 Software Development Practices

### Goals and Vision:

Our goal is to develop a robust, bug-free, and scalable currency system that feels rewarding, enhances the player's progression and seamlessly integrates with other gameplay mechanics. The system should integrate smoothly with other gameplay systems such as the HUD, item system, and enemy variety system.

### Members:

| Member                      | Role | GitHub |
|:----------------------------| :--- | :--- |
| Seyun Oh                    | PM / TeamLeader | https://github.com/ogaji |
| Khuvituguldur               | Developer | https://github.com/tuugy-rvn |
| Isaac de Jesus Rojas Torres | Developer | https://github.com/isaacrt54 |
| Joshua Hernández Ruiz       | Developer | https://github.com/Jperf0 |
| Anukhishig                  | Documentation | https://github.com/Anukhishig |
| Byambakhishig Khishigjin    | QA Tester | https://github.com/hishigjinb-svg |
| Hyunseung Je                | Dev Lead / Collaborator | https://github.com/HyunseungJe |
| Minkyung Yeo                | Documentation | https://github.com/yeominkyung |

---

## 2. Team Requirements

### Overall Requirement:

Currency System. Our team is responsible for managing the logic, balance, and persistence of the in-game currency earned by players during gameplay.

---

## 3. Detailed Requirements

1. Implement a core currency class to add, deduct, and track balances
   independently from the player's score. Keep balances non-negative.
2. Define reward amounts and drop rates for enemy defeats or level completion.
   Prevent duplicate rewards from the same event. The source of
   level-completion events remains to be agreed before integration.
3. Save and load the currency balance between game sessions. Validate loaded
   values and report failures without replacing valid balances or save data
   with invalid data.
4. Provide an interface for balance queries and purchase-related deductions.
   Agree with the Item System on the purchase scope of 'Shop' and the handling
   of item-delivery failures before integration.
5. Prevent overspending and invalid balance updates. Rejected operations
   must leave the balance unchanged. Test normal transactions,
   insufficient funds, and invalid input handling.

---

## 4. Dependencies on Other Teams

1. Gameplay HUD: The HUD team displays balance information supplied by our
   currency interface. Agree on how balance updates are communicated.
2. Item System: Agree on purchase requests, prices, and item-delivery results.
   Our team is responsible for currency validation and deduction.
3. Player & Enemy Ship Variety: Receive enemy-defeat notifications and the
   enemy information needed to calculate rewards. Agree on how duplicate
   notifications are handled.
