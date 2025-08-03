package exsistory.dev.hubpvp;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

public class ConfigManager {

    private final HubPVP plugin;
    private FileConfiguration config;

    public ConfigManager(HubPVP plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        setDefaults();
        plugin.saveConfig();
    }

    private void setDefaults() {
        addDefault("equipment.helmet.material", "DIAMOND_HELMET");
        addDefault("equipment.helmet.name", "&6PVP Helmet");
        addDefault("equipment.helmet.lore", Arrays.asList("&7A powerful helmet for PVP!", "&7Locked in inventory"));
        addDefault("equipment.helmet.enchantments.PROTECTION_ENVIRONMENTAL", 4);
        addDefault("equipment.helmet.enchantments.UNBREAKING", 3);

        addDefault("equipment.chestplate.material", "DIAMOND_CHESTPLATE");
        addDefault("equipment.chestplate.name", "&6PVP Chestplate");
        addDefault("equipment.chestplate.lore", Arrays.asList("&7A powerful chestplate for PVP!", "&7Locked in inventory"));
        addDefault("equipment.chestplate.enchantments.PROTECTION_ENVIRONMENTAL", 4);
        addDefault("equipment.chestplate.enchantments.UNBREAKING", 3);

        addDefault("equipment.leggings.material", "DIAMOND_LEGGINGS");
        addDefault("equipment.leggings.name", "&6PVP Leggings");
        addDefault("equipment.leggings.lore", Arrays.asList("&7Powerful leggings for PVP!", "&7Locked in inventory"));
        addDefault("equipment.leggings.enchantments.PROTECTION_ENVIRONMENTAL", 4);
        addDefault("equipment.leggings.enchantments.UNBREAKING", 3);

        addDefault("equipment.boots.material", "DIAMOND_BOOTS");
        addDefault("equipment.boots.name", "&6PVP Boots");
        addDefault("equipment.boots.lore", Arrays.asList("&7Powerful boots for PVP!", "&7Locked in inventory"));
        addDefault("equipment.boots.enchantments.PROTECTION_ENVIRONMENTAL", 4);
        addDefault("equipment.boots.enchantments.UNBREAKING", 3);

        addDefault("equipment.sword.material", "DIAMOND_SWORD");
        addDefault("equipment.sword.name", "&c&lPVP SWORD");
        addDefault("equipment.sword.inactive-lore", Arrays.asList("&7A deadly sword for PVP!", "&e&lRight-click to activate PVP mode!", "&7Or hold this sword to activate"));
        addDefault("equipment.sword.active-lore", Arrays.asList("&7A deadly sword for PVP!", "&a&lPVP MODE ACTIVE!", "&7Locked in inventory"));
        addDefault("equipment.sword.slot", 0);
        addDefault("equipment.sword.enchantments.DAMAGE_ALL", 5);
        addDefault("equipment.sword.enchantments.UNBREAKING", 3);
        addDefault("equipment.sword.enchantments.FIRE_ASPECT", 2);

        addDefault("activation.mode", "RIGHT_CLICK");
        addDefault("activation.give-sword-on-join", true);
        addDefault("activation.show-sword-message", false);
        addDefault("activation.show-death-reset-message", false);

        addDefault("countdown.enable.enabled", true);
        addDefault("countdown.enable.time", 3);
        addDefault("countdown.enable.title", "&c&lPVP ACTIVATING...");
        addDefault("countdown.enable.subtitle", "&e{time} seconds remaining");
        addDefault("countdown.enable.sound", "BLOCK_NOTE_BLOCK_PLING");
        addDefault("countdown.enable.cancel-on-move", false);
        addDefault("countdown.enable.cancel-on-damage", true);

        addDefault("countdown.disable.enabled", true);
        addDefault("countdown.disable.time", 5);
        addDefault("countdown.disable.title", "&a&lPVP DEACTIVATING...");
        addDefault("countdown.disable.subtitle", "&7{time} seconds remaining");
        addDefault("countdown.disable.sound", "BLOCK_NOTE_BLOCK_PLING");
        addDefault("countdown.disable.cancel-on-move", false);
        addDefault("countdown.disable.cancel-on-damage", true);

        addDefault("combat.time", 15);
        addDefault("combat.block-pvp-when-disabled", true);
        addDefault("combat.tag-enabled", true);
        addDefault("combat.tag-duration", 30);
        addDefault("combat.prevent-pvp-disable", true);

        addDefault("effects.glowing", true);

        addDefault("scoreboard.title", "&6&l⚔ &e&lPVP ZONE &6&l⚔");
        addDefault("scoreboard.enabled", true);
        addDefault("scoreboard.update-interval", 20);
        addDefault("scoreboard.lines", Arrays.asList(
                "&7",
                "&fStatus: %hubpvp_status_color%&l%hubpvp_status%",
                "&fCombat: %hubpvp_combat_color%&l%hubpvp_combat%",
                "&7",
                "&eKills: &f%hubpvp_kills%",
                "&bStreak: &f%hubpvp_killstreak%",
                "&7",
                "&fTop Killer:",
                "&e %hubpvp_top_1_name%: &f%hubpvp_top_1_kills% Kills",
                "&7",
                "&7play.yourserver.com"
        ));

        addDefault("messages.pvp-enabled", "&aYou have enabled PVP mode!");
        addDefault("messages.pvp-disabled", "&cYou have disabled PVP mode!");
        addDefault("messages.pvp-already-enabled", "&cYou already have PVP enabled!");
        addDefault("messages.pvp-not-enabled", "&cYou don't have PVP enabled!");
        addDefault("messages.pvp-disabled-world", "&cPVP is disabled in this world!");
        addDefault("messages.equipment-locked", "&cYou cannot move PVP equipment!");
        addDefault("messages.combat-entered", "&cYou are now in combat!");
        addDefault("messages.combat-exited", "&aYou are no longer in combat!");
        addDefault("messages.command-blocked", "&cYou cannot use commands while in combat! (&6{time}s&c remaining)");
        addDefault("messages.flight-blocked", "&cYou cannot use flight while in PVP mode!");
        addDefault("messages.kill", "&6{killer} &7killed &c{victim} &7(Kill Streak: &e{killstreak}&7)");
        addDefault("messages.countdown-enable-started", "&eActivating PVP in &c{time} &eseconds...");
        addDefault("messages.countdown-disable-started", "&eDeactivating PVP in &a{time} &eseconds...");
        addDefault("messages.countdown-cancelled", "&cCountdown cancelled!");
        addDefault("messages.pvp-blocked", "&cYou can only fight players with PVP enabled!");
        addDefault("messages.sword-received", "&aYou received your PVP sword! Right-click to activate PVP!");
        addDefault("messages.combat-tagged", "&cYou cannot disable PVP while combat tagged! &6{time}s &cremaining.");
        addDefault("messages.death-reset", "&7Your PVP status has been reset. Activate PVP again when ready!");

        addDefault("disabled-worlds", Arrays.asList("spawn", "lobby"));

        addDefault("commands.activation", Arrays.asList("say {player} activated PVP!"));
        addDefault("commands.deactivation", Arrays.asList("say {player} deactivated PVP!"));
        addDefault("commands.kill", Arrays.asList("say {killer} killed {victim}!"));
        addDefault("commands.blocked", Arrays.asList("tp", "home", "spawn", "warp", "back"));

        addDefault("commands.killstreak.5", Arrays.asList("say {player} reached a 5 kill streak!", "give {player} golden_apple 1"));
        addDefault("commands.killstreak.10", Arrays.asList("say {player} reached a 10 kill streak!", "give {player} golden_apple 3"));
        addDefault("commands.killstreak.15", Arrays.asList("say {player} is on fire with 15 kills!", "give {player} golden_apple 5"));
        addDefault("commands.killstreak.20", Arrays.asList("say {player} is unstoppable with 20 kills!", "give {player} diamond 1"));
    }

    private void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public Material getHelmetMaterial() {
        return getMaterial("equipment.helmet.material", Material.DIAMOND_HELMET);
    }

    public String getHelmetName() {
        return config.getString("equipment.helmet.name", "&6PVP Helmet");
    }

    public List<String> getHelmetLore() {
        return config.getStringList("equipment.helmet.lore");
    }

    public Map<String, Integer> getHelmetEnchantments() {
        return getEnchantments("equipment.helmet.enchantments");
    }

    public Material getChestplateMaterial() {
        return getMaterial("equipment.chestplate.material", Material.DIAMOND_CHESTPLATE);
    }

    public String getChestplateName() {
        return config.getString("equipment.chestplate.name", "&6PVP Chestplate");
    }

    public List<String> getChestplateLore() {
        return config.getStringList("equipment.chestplate.lore");
    }

    public Map<String, Integer> getChestplateEnchantments() {
        return getEnchantments("equipment.chestplate.enchantments");
    }

    public Material getLeggingsMaterial() {
        return getMaterial("equipment.leggings.material", Material.DIAMOND_LEGGINGS);
    }

    public String getLeggingsName() {
        return config.getString("equipment.leggings.name", "&6PVP Leggings");
    }

    public List<String> getLeggingsLore() {
        return config.getStringList("equipment.leggings.lore");
    }

    public Map<String, Integer> getLeggingsEnchantments() {
        return getEnchantments("equipment.leggings.enchantments");
    }

    public Material getBootsMaterial() {
        return getMaterial("equipment.boots.material", Material.DIAMOND_BOOTS);
    }

    public String getBootsName() {
        return config.getString("equipment.boots.name", "&6PVP Boots");
    }

    public List<String> getBootsLore() {
        return config.getStringList("equipment.boots.lore");
    }

    public Map<String, Integer> getBootsEnchantments() {
        return getEnchantments("equipment.boots.enchantments");
    }

    public Material getSwordMaterial() {
        return getMaterial("equipment.sword.material", Material.DIAMOND_SWORD);
    }

    public String getSwordName() {
        return config.getString("equipment.sword.name", "&cPVP Sword");
    }

    public List<String> getSwordInactiveLore() {
        return config.getStringList("equipment.sword.inactive-lore");
    }

    public List<String> getSwordActiveLore() {
        return config.getStringList("equipment.sword.active-lore");
    }

    public Map<String, Integer> getSwordEnchantments() {
        return getEnchantments("equipment.sword.enchantments");
    }

    public int getSwordSlot() {
        return config.getInt("equipment.sword.slot", 0);
    }

    public String getActivationMode() {
        return config.getString("activation.mode", "RIGHT_CLICK").toUpperCase();
    }

    public boolean shouldGiveSwordOnJoin() {
        return config.getBoolean("activation.give-sword-on-join", true);
    }

    public boolean shouldShowSwordMessage() {
        return config.getBoolean("activation.show-sword-message", false);
    }

    public boolean shouldShowDeathResetMessage() {
        return config.getBoolean("activation.show-death-reset-message", false);
    }

    public boolean isEnableCountdownEnabled() {
        return config.getBoolean("countdown.enable.enabled", true);
    }

    public int getEnableCountdownTime() {
        return config.getInt("countdown.enable.time", 3);
    }

    public String getEnableCountdownTitle() {
        return config.getString("countdown.enable.title", "&c&lPVP ACTIVATING...");
    }

    public String getEnableCountdownSubtitle() {
        return config.getString("countdown.enable.subtitle", "&e{time} seconds remaining");
    }

    public String getEnableCountdownSound() {
        return config.getString("countdown.enable.sound", "BLOCK_NOTE_BLOCK_PLING");
    }

    public boolean shouldEnableCancelOnMove() {
        return config.getBoolean("countdown.enable.cancel-on-move", false);
    }

    public boolean shouldEnableCancelOnDamage() {
        return config.getBoolean("countdown.enable.cancel-on-damage", true);
    }

    public boolean isDisableCountdownEnabled() {
        return config.getBoolean("countdown.disable.enabled", true);
    }

    public int getDisableCountdownTime() {
        return config.getInt("countdown.disable.time", 5);
    }

    public String getDisableCountdownTitle() {
        return config.getString("countdown.disable.title", "&a&lPVP DEACTIVATING...");
    }

    public String getDisableCountdownSubtitle() {
        return config.getString("countdown.disable.subtitle", "&7{time} seconds remaining");
    }

    public String getDisableCountdownSound() {
        return config.getString("countdown.disable.sound", "BLOCK_NOTE_BLOCK_PLING");
    }

    public boolean shouldDisableCancelOnMove() {
        return config.getBoolean("countdown.disable.cancel-on-move", false);
    }

    public boolean shouldDisableCancelOnDamage() {
        return config.getBoolean("countdown.disable.cancel-on-damage", true);
    }

    public int getCombatTime() {
        return config.getInt("combat.time", 15);
    }

    public boolean shouldBlockPVPWhenDisabled() {
        return config.getBoolean("combat.block-pvp-when-disabled", true);
    }

    public boolean isCombatTagEnabled() {
        return config.getBoolean("combat.tag-enabled", true);
    }

    public int getCombatTagDuration() {
        return config.getInt("combat.tag-duration", 30);
    }

    public boolean shouldPreventPVPDisable() {
        return config.getBoolean("combat.prevent-pvp-disable", true);
    }

    public boolean isGlowingEnabled() {
        return config.getBoolean("effects.glowing", true);
    }

    public String getScoreboardTitle() {
        return config.getString("scoreboard.title", "&6&l⚔ &e&lPVP ZONE &6&l⚔");
    }

    public boolean isScoreboardEnabled() {
        return config.getBoolean("scoreboard.enabled", true);
    }

    public int getScoreboardUpdateInterval() {
        return config.getInt("scoreboard.update-interval", 20);
    }

    public List<String> getScoreboardLines() {
        return config.getStringList("scoreboard.lines");
    }

    public String getPVPEnabledMessage() {
        return config.getString("messages.pvp-enabled", "&aYou have enabled PVP mode!");
    }

    public String getPVPDisabledMessage() {
        return config.getString("messages.pvp-disabled", "&cYou have disabled PVP mode!");
    }

    public String getPVPAlreadyEnabledMessage() {
        return config.getString("messages.pvp-already-enabled", "&cYou already have PVP enabled!");
    }

    public String getPVPNotEnabledMessage() {
        return config.getString("messages.pvp-not-enabled", "&cYou don't have PVP enabled!");
    }

    public String getPVPDisabledWorldMessage() {
        return config.getString("messages.pvp-disabled-world", "&cPVP is disabled in this world!");
    }

    public String getEquipmentLockedMessage() {
        return config.getString("messages.equipment-locked", "&cYou cannot move PVP equipment!");
    }

    public String getCombatEnteredMessage() {
        return config.getString("messages.combat-entered", "&cYou are now in combat!");
    }

    public String getCombatExitedMessage() {
        return config.getString("messages.combat-exited", "&aYou are no longer in combat!");
    }

    public String getCommandBlockedMessage() {
        return config.getString("messages.command-blocked", "&cYou cannot use commands while in combat! (&6{time}s&c remaining)");
    }

    public String getFlightBlockedMessage() {
        return config.getString("messages.flight-blocked", "&cYou cannot use flight while in PVP mode!");
    }

    public String getKillMessage() {
        return config.getString("messages.kill", "&6{killer} &7killed &c{victim} &7(Kill Streak: &e{killstreak}&7)");
    }

    public String getEnableCountdownStartedMessage() {
        return config.getString("messages.countdown-enable-started", "&eActivating PVP in &c{time} &eseconds...");
    }

    public String getDisableCountdownStartedMessage() {
        return config.getString("messages.countdown-disable-started", "&eDeactivating PVP in &a{time} &eseconds...");
    }

    public String getCountdownCancelledMessage() {
        return config.getString("messages.countdown-cancelled", "&cCountdown cancelled!");
    }

    public String getPVPBlockedMessage() {
        return config.getString("messages.pvp-blocked", "&cYou can only fight players with PVP enabled!");
    }

    public String getSwordReceivedMessage() {
        return config.getString("messages.sword-received", "&aYou received your PVP sword! Right-click to activate PVP!");
    }

    public String getCombatTaggedMessage() {
        return config.getString("messages.combat-tagged", "&cYou cannot disable PVP while combat tagged! &6{time}s &cremaining.");
    }

    public String getDeathResetMessage() {
        return config.getString("messages.death-reset", "&7Your PVP status has been reset. Activate PVP again when ready!");
    }

    public List<String> getDisabledWorlds() {
        return config.getStringList("disabled-worlds");
    }

    public List<String> getActivationCommands() {
        return config.getStringList("commands.activation");
    }

    public List<String> getDeactivationCommands() {
        return config.getStringList("commands.deactivation");
    }

    public List<String> getKillCommands() {
        return config.getStringList("commands.kill");
    }

    public List<String> getBlockedCommands() {
        return config.getStringList("commands.blocked");
    }

    public Map<Integer, List<String>> getKillstreakCommands() {
        Map<Integer, List<String>> commands = new HashMap<>();
        if (config.contains("commands.killstreak")) {
            for (String key : config.getConfigurationSection("commands.killstreak").getKeys(false)) {
                try {
                    int killstreak = Integer.parseInt(key);
                    List<String> commandList = config.getStringList("commands.killstreak." + key);
                    commands.put(killstreak, commandList);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid killstreak number in config: " + key);
                }
            }
        }
        return commands;
    }

    private Material getMaterial(String path, Material defaultMaterial) {
        String materialName = config.getString(path, defaultMaterial.name());
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material in config: " + materialName + " at path: " + path);
            return defaultMaterial;
        }
    }

    private Map<String, Integer> getEnchantments(String path) {
        Map<String, Integer> enchantments = new HashMap<>();
        if (config.contains(path)) {
            for (String key : config.getConfigurationSection(path).getKeys(false)) {
                int level = config.getInt(path + "." + key, 1);
                enchantments.put(key, level);
            }
        }
        return enchantments;
    }
}