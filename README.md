# 🪙 ChestTrade

<!-- Banner image: recommended size 860x200px. Replace the path below with your image. -->
<!-- ![ChestTrade Banner](images/banner.png) -->

**ChestTrade** is a lightweight, player-driven shop plugin for Paper servers. Players can set up chest-based trade shops where they offer one item in exchange for another — no economy plugin or in-game currency required. Just pure item-for-item trading.

Operators and admins can open and break any shop, giving full control over the marketplace without needing extra setup.

---

## ✨ Features

<!-- ## 📝 Features -->

- 📦 Simple chest-based player shops — place a chest, run a command or write a sign, done
- 🔄 Item-for-item trading — no economy plugin needed
- 🔐 Shops are protected; only the owner (or OP) can break or open them
- 🛠️ Lightweight and focused — no bloat, just trading
- 🔑 LuckPerms support for fine-grained permission control

---

## 📋 Requirements

| Requirement                         | Version | Notes                                       |
| ----------------------------------- | ------- | ------------------------------------------- |
| Paper / Spigot                      | 1.21.1  | Required                                    |
| Java                                | 21+     | Required                                    |
| [LuckPerms](https://luckperms.net/) | Latest  | Required for `chesttrade.create` permission |

> **Note:** Spigot may work but is untested. Paper is recommended.

---

## 📥 Installation

1. Download the latest `.jar` from the [Releases](../../releases) page
2. Drop it into your server's `/plugins/` folder
3. Make sure [LuckPerms](https://luckperms.net/) is installed
4. Restart your server
5. Assign the `chesttrade.create` permission to players who should be able to create shops

---

## 🛒 How to Use

### Creating a Shop

1. Place a **chest** where you want your shop
2. Look at the chest and run:
   ```
   /ctshop create <cost-item> <cost-amount> <product-item> <product-amount>
   ```
   Example: offer 32 wheat in exchange for 1 diamond:
   ```
   /ctshop create diamond 1 wheat 32
   ```
3. The chest is now a trade shop — other players can interact with it to complete the trade

<!-- Screenshot: show the chat command being typed and the shop being created -->
<!-- ![Creating a shop](images/create-shop.png) -->

### Using a Shop

- **Right-click** the shop chest to initiate a trade
- If you have the requested items in your inventory, the trade completes automatically

<!-- Screenshot: show the trade confirmation message in chat after a successful trade -->
<!-- ![Trading with a shop](images/trade-shop.png) -->

### Removing Your Shop

```
Shift + Axe
```

Look at your shop chest and run this command to remove it.

---

## 📜 Commands

| Command                                                                    | Description             | Permission          |
| -------------------------------------------------------------------------- | ----------------------- | ------------------- |
| `/ctshop create <cost-item> <cost-amount> <product-item> <product-amount>` | Create a new trade shop | `chesttrade.create` |
| `shift + axe`                                                              | Remove your shop        | `chesttrade.create` |
| `/ctshop info`                                                             | View info about a shop  | `chesttrade.create` |

---

## 🔑 Permissions

| Permission          | Description                                          | Default |
| ------------------- | ---------------------------------------------------- | ------- |
| `chesttrade.create` | Allows a player to create and manage their own shops | `false` |
| `ctshop.*`          | All ChestTrade permissions                           | OP only |

> Permissions are managed via [LuckPerms](https://luckperms.net/).  
> OPs can open and break any shop regardless of permissions.

### Example LuckPerms setup

Give all players in the `default` group the ability to create shops:

```
/lp group default permission set chesttrade.create true
```

<!-- Screenshot: optional — show the LuckPerms command being run in-game or in console -->
<!-- ![LuckPerms setup](images/luckperms-setup.png) -->

---

## ⚙️ Compatibility

| Software      | Supported   |
| ------------- | ----------- |
| Paper 1.21.1  | ✅          |
| Spigot 1.21.1 | ⚠️ Untested |
| Folia         | ⚠️ Untested |
| Bukkit        | ⚠️ Untested |

---

## 🐛 Issues & Suggestions

Found a bug or have a feature request? Please open an [Issue](../../issues) on GitHub and include:

- Your server software and version
- Steps to reproduce the problem
- Any relevant console errors

---

# 🛠️🚧 Under development

- This project is still in early development
- Be aware of frequent changes and updates

## 📄 License

This project is licensed under the [MIT License](LICENSE).
