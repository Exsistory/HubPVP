# HubPVP

[![Build Status](https://github.com/Exsistory/HubPVP/workflows/Build%20and%20Release/badge.svg)](https://github.com/Exsistory/HubPVP/actions)
[![Release](https://img.shields.io/github/v/release/Exsistory/HubPVP)](https://github.com/Exsistory/HubPVP/releases)
[![Downloads](https://img.shields.io/github/downloads/Exsistory/HubPVP/total)](https://github.com/Exsistory/HubPVP/releases)
[![License](https://img.shields.io/badge/license-GNU.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://openjdk.java.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.20%2B-green.svg)](https://www.minecraft.net/)

A fun hub pvp game

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Commands](#commands)
- [Permissions](#permissions)
- [Placeholders](#placeholders)
- [API](#api)
- [Events](#events)

## Features

### Main Features
- **Dual Countdown System** - configurable countdowns for enabling and disabling PVP
- **Advanced Combat Tagging** - Prevents players from disabling PVP during combat
- **Equipment Management** - System that gives armor/sword and locks it in their inventory
- **Kill Streak System** - Track kills with customizable reward commands
- **World Management** - Disable PVP in specific worlds (spawn, lobby, etc.)

### Extra Features
- **API** - Because why not
- **PlaceholderAPI Support** - 50+ placeholders for literally everything
- **Event System** - Custom events for PVP state changes and combat
- **Live Scoreboard** - PVP statistics display

## Installation

### Requirements
- Java 8 or higher
- Bukkit/Spigot/Paper 1.8 - 1.20+
- PlaceholderAPI (optional but recommended)

### Steps
1. Download the latest `HubPVP-X.X.X.jar` from [Releases](https://github.com/Exsistory/HubPVP/releases)
2. Place the JAR file in your server's `plugins/` directory
3. Restart your server
4. Configure the plugin in `plugins/HubPVP/config.yml`
5. Reload the configuration with `/hubpvp reload`

## Configuration

<details>
<summary>Countdown Config</summary>

```yaml
countdown:
  enable:
    enabled: true
    time: 3
    title: "&c&lPVP ACTIVATING..."
    subtitle: "&e{time} seconds remaining"
    sound: "BLOCK_NOTE_BLOCK_PLING"
    cancel-on-move: false
    cancel-on-damage: true

  disable:
    enabled: true
    time: 5
    title: "&a&lPVP DEACTIVATING..."
    subtitle: "&7{time} seconds remaining..."
    sound: "BLOCK_NOTE_BLOCK_PLING"
    cancel-on-move: false
    cancel-on-damage: true
```
</details>

<details>
<summary>Combat Config</summary>

```yaml
combat:
  time: 15
  block-pvp-when-disabled: true
  tag-enabled: true
  tag-duration: 30
  prevent-pvp-disable: true
```
</details>

<details>
<summary>Equipment Config</summary>

```yaml
equipment:
  sword:
    material: DIAMOND_SWORD
    name: "&a&lPVP SWORD"
    slot: 0
    enchantments:
      DAMAGE_ALL: 5
      UNBREAKING: 3
      FIRE_ASPECT: 2
```
</details>

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pvp` | Toggle PVP mode | `hubpvp.use` |
| `/pvp enable` | Enable PVP mode | `hubpvp.use` |
| `/pvp disable` | Disable PVP mode | `hubpvp.use` |
| `/pvp stats [player]` | View PVP statistics | `hubpvp.use` |
| `/pvp top [number]` | View top PVP players | `hubpvp.use` |
| `/pvp combat [player]` | Check combat status | `hubpvp.use` |
| `/pvp reset <player>` | Reset player statistics | `hubpvp.admin.reset` |
| `/pvp reload` | Reload configuration | `hubpvp.admin.reload` |
| `/pvp list` | List PVP enabled players | `hubpvp.admin.list` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `hubpvp.*` | All HubPVP permissions | `op` |
| `hubpvp.use` | Basic permission to use PVP commands | `true` |
| `hubpvp.admin` | Access to all admin commands | `op` |
| `hubpvp.admin.reset` | Permission to reset player statistics | `op` |
| `hubpvp.admin.reload` | Permission to reload configuration | `op` |
| `hubpvp.admin.list` | Permission to list PVP players | `op` |
| `hubpvp.admin.combat` | Permission to check combat status | `op` |
| `hubpvp.fly` | Permission to use flight (re-enabled after combat) | `op` |
| `hubpvp.stats.others` | Permission to view other players' statistics | `op` |

## Placeholders

### Player Status Placeholders
| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%hubpvp_status%` | PVP status | `Enabled` / `Disabled` |
| `%hubpvp_status_boolean%` | PVP status as boolean | `true` / `false` |
| `%hubpvp_status_color%` | Color code for PVP status | `&a` / `&c` |

### Combat Placeholders
| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%hubpvp_combat%` | Combat status | `Yes` / `No` |
| `%hubpvp_combat_boolean%` | Combat status as boolean | `true` / `false` |
| `%hubpvp_combat_color%` | Color code for combat status | `&c` / `&a` |
| `%hubpvp_combat_time%` | Combat time remaining (seconds) | `30` |
| `%hubpvp_combat_time_formatted%` | Combat time formatted | `00:30` |
| `%hubpvp_combat_tag%` | Combat tag status | `Yes` / `No` |
| `%hubpvp_combat_tag_time%` | Combat tag time remaining | `30` |

### Countdown Placeholders
| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%hubpvp_countdown%` | Countdown status | `Yes` / `No` |
| `%hubpvp_countdown_type%` | Type of countdown | `Enable` / `Disable` / `None` |
| `%hubpvp_countdown_enable%` | Enable countdown status | `Yes` / `No` |
| `%hubpvp_countdown_disable%` | Disable countdown status | `Yes` / `No` |

### Statistics Placeholders
| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%hubpvp_kills%` | Total kills | `42` |
| `%hubpvp_killstreak%` | Current kill streak | `5` |
| `%hubpvp_rank%` | Player's ranking position | `3` |

### Top Player Placeholders
| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%hubpvp_top_1_name%` | Top player name | `PlayerName` |
| `%hubpvp_top_1_kills%` | Top player kills | `100` |
| `%hubpvp_top_X_name%` | Xth place player name (1-50) | `PlayerName` |
| `%hubpvp_top_X_kills%` | Xth place player kills (1-50) | `50` |

### Global Placeholders
| Placeholder | Description | Returns |
|-------------|-------------|---------|
| `%hubpvp_global_players%` | Total PVP players online | `15` |
| `%hubpvp_global_combat%` | Players currently in combat | `3` |
| `%hubpvp_global_safe%` | Players currently safe | `12` |
| `%hubpvp_global_countdown_enable%` | Players in enable countdown | `2` |
| `%hubpvp_global_countdown_disable%` | Players in disable countdown | `1` |

## API

### Getting Started

Add HubPVP as a dependency to your project:

#### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Exsistory</groupId>
        <artifactId>HubPVP</artifactId>
        <version>v3.1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Exsistory:HubPVP:v3.1.0'
}
```

### Basic Usage

#### Getting the API Instance
```java
import exsistory.dev.hubpvp.api.HubPVPAPI;
import exsistory.dev.hubpvp.api.CountdownType;

public class MyPlugin extends JavaPlugin {
    private HubPVPAPI hubPVPAPI;
    
    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("HubPVP") == null) {
            getLogger().severe("HubPVP is required but not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        hubPVPAPI = HubPVPAPI.getInstance();
    }
}
```

#### PVP Status Management
```java
// Check PVP status
boolean hasPVP = api.isPVPEnabled(player);

// Enable/Disable PVP
api.enablePVP(player);   // Starts countdown if enabled
api.disablePVP(player);  // Starts countdown if enabled

// Check combat status
boolean inCombat = api.isInCombat(player);
long timeLeft = api.getCombatTimeLeft(player);

// Manage combat manually
api.enterCombat(player);
api.exitCombat(player);
```

#### Countdown System
```java
// Check countdown status
boolean inCountdown = api.isInCountdown(player);
CountdownType type = api.getCountdownType(player);

if (type == CountdownType.ENABLE) {
    player.sendMessage("Activating PVP...");
} else if (type == CountdownType.DISABLE) {
    player.sendMessage("Deactivating PVP...");
}

// Cancel countdown
api.cancelCountdown(player);
```

#### Statistics
```java
// Get player statistics
int kills = api.getTotalKills(player);
int streak = api.getKillStreak(player);

// Reset statistics
api.resetStats(player);

// Get leaderboards
List<Player> topPlayers = api.getTopKillers(10);
Set<Player> pvpPlayers = api.getPVPPlayers();
```

#### Configuration Access
```java
// Access configuration values
boolean enableCountdown = api.isEnableCountdownEnabled();
int enableTime = api.getEnableCountdownTime();
boolean combatTag = api.isCombatTagEnabled();
int combatDuration = api.getCombatTagDuration();
```

### API Interface Reference

<details>
<summary>Complete API Methods</summary>

```java
public interface HubPVPAPI {
    // PVP Status
    boolean isPVPEnabled(Player player);
    void enablePVP(Player player);
    void disablePVP(Player player);
    
    // Combat Management
    boolean isInCombat(Player player);
    long getCombatTimeLeft(Player player);
    void enterCombat(Player player);
    void exitCombat(Player player);
    
    // Countdown System
    boolean isInCountdown(Player player);
    CountdownType getCountdownType(Player player);
    boolean isInEnableCountdown(Player player);
    boolean isInDisableCountdown(Player player);
    void cancelCountdown(Player player);
    
    // Statistics
    int getKillStreak(Player player);
    int getTotalKills(Player player);
    void resetStats(Player player);
    List<Player> getTopKillers(int limit);
    Set<Player> getPVPPlayers();
    
    // Configuration
    boolean isEnableCountdownEnabled();
    boolean isDisableCountdownEnabled();
    int getEnableCountdownTime();
    int getDisableCountdownTime();
    boolean isCombatTagEnabled();
    int getCombatTagDuration();
}
```
</details>

## Events

### PVPToggleEvent
Called when a player's PVP status changes.

```java
@EventHandler
public void onPVPToggle(PVPToggleEvent event) {
    Player player = event.getPlayer();
    
    if (event.isEnabling()) {
        // Player is enabling PVP
    } else if (event.isDisabling()) {
        // Player is disabling PVP
    }
    
    // Get toggle reason
    ToggleReason reason = event.getReason();
    
    // Cancel the event if needed
    event.setCancelled(true);
}
```

#### Toggle Reasons
- `COMMAND` - Player used command
- `SWORD_CLICK` - Player clicked PVP sword
- `SWORD_HOLD` - Player held PVP sword
- `API_CALL` - Called via API
- `FORCE` - Force enabled/disabled
- `DEATH` - Player died
- `WORLD_CHANGE` - Player changed to disabled world
- `PLUGIN_DISABLE` - Plugin disabling
