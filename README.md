# SmorkiItemBlacklist

**A high-performance, fully configurable item blacklist plugin for Minecraft servers.**

![Version](https://img.shields.io/badge/version-1.0.0-00A3E0?style=for-the-badge)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-00FFFF?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Folia-00A3E0?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-lightgrey?style=for-the-badge)

---

## Overview

SmorkiItemBlacklist lets server administrators block specific items server-wide — preventing players from placing, dropping, picking up, or moving them through inventories. Built on an optimized `HashSet` lookup engine, the plugin performs blacklist checks in constant time, so performance stays smooth no matter how large the list grows.

## Features

- **Instant item checks** — Every lookup runs at O(1) speed thanks to a `HashSet`-backed blacklist engine.
- **Live reload, zero downtime** — Update your blacklist or settings and apply them instantly with a single command, no restart required.
- **Granular event control** — Independently toggle blocking for inventory clicks, item drops, item pickups, and block placement.
- **Bypass permission support** — Grant trusted players or staff a permission node to ignore the blacklist entirely.
- **Modern text formatting** — All messages use Adventure's MiniMessage format, supporting gradients and hex colors — no legacy color codes.
- **Fully configurable** — Every toggle, message, permission node, and blacklisted item lives in a single clean `config.yml`.
- **Minimal, readable config** — No clutter, no walls of comments — just what you need to configure the plugin quickly.

## Installation

1. Download the latest `SmorkiItemBlacklist-1.0.0.jar` from the Releases page.
2. Drop the jar into your server's `plugins/` folder.
3. Start or reload your server.
4. Open `plugins/SmorkiItemBlacklist/config.yml` and customize it to your needs.
5. Apply changes without restarting:
   ```
   /sib reload
   ```

## Commands

| Command | Description | Permission |
|---|---|---|
| `/smorkiblacklist reload` (aliases: `/sib`, `/itemblacklist`) | Reloads the config and rebuilds the blacklist instantly | `smorki.blacklist.admin` |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `smorki.blacklist.admin` | Allows use of the reload command | `op` |
| `smorki.blacklist.bypass` | Allows a player to bypass the blacklist entirely | `op` |

## Configuration

```yaml
settings:
  enabled: true
  bypass-permission: "smorki.blacklist.bypass"
  block-inventory-click: true
  block-drop: true
  block-pickup: true
  block-place: true

messages:
  prefix: "<gradient:#00A3E0:#00FFFF>Item Blacklist</gradient> <white>»</white> "
  reload-success: "<green>Configuration reloaded successfully."
  reload-no-permission: "<red>You do not have permission to reload this plugin."
  item-blocked: "<red>That item is blacklisted and cannot be used."

blacklist:
  - "minecraft:tnt"
  - "minecraft:command_block"
  - "minecraft:barrier"
  - "minecraft:spawner"
```

Item names in the `blacklist` list accept the `minecraft:` namespace prefix or plain material names — both are parsed automatically.

## How It Works

On startup, the plugin reads your `blacklist` entries, strips any namespace prefixes, and resolves each one against a `Material`. Valid materials are stored in memory for instant lookups. Whenever `/sib reload` is run, this process repeats from scratch — clearing the old data and rebuilding it from the current `config.yml`, so changes take effect immediately.

## Support

Found a bug or have a feature request? Open an issue on the repository's Issues tab.

## License

Licensed under the MIT License. See the `LICENSE` file for details.

---

**Author:** Smorki
