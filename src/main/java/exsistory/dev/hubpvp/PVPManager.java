package exsistory.dev.hubpvp;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PVPManager {

    public enum CountdownType {
        ENABLE, DISABLE
    }

    private static class CountdownData {
        public final CountdownType type;
        public final Location startLocation;
        public final BukkitRunnable task;

        public CountdownData(CountdownType type, Location startLocation, BukkitRunnable task) {
            this.type = type;
            this.startLocation = startLocation;
            this.task = task;
        }
    }

    private final HubPVP plugin;
    private final Map<UUID, PVPPlayer> pvpPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> combatTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> killStreaks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> totalKills = new ConcurrentHashMap<>();
    private final Map<UUID, CountdownData> countdownTasks = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerInventory> savedInventories = new ConcurrentHashMap<>();
    private final Set<String> disabledWorlds = new HashSet<>();
    private Scoreboard scoreboard;
    private Objective killsObjective;
    private BukkitRunnable scoreboardUpdater;

    public PVPManager(HubPVP plugin) {
        this.plugin = plugin;
        loadDisabledWorlds();
        startCombatTimer();
        Bukkit.getScheduler().runTaskLater(plugin, this::initializeScoreboard, 1L);
    }

    private void loadDisabledWorlds() {
        disabledWorlds.addAll(plugin.getConfigManager().getDisabledWorlds());
    }

    private void initializeScoreboard() {
        try {
            if (!plugin.getConfigManager().isScoreboardEnabled()) {
                plugin.getLogger().info("Scoreboard is disabled in config.");
                return;
            }

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) {
                plugin.getLogger().warning("ScoreboardManager is not available! Scoreboard features will be disabled.");
                return;
            }

            scoreboard = manager.getNewScoreboard();

            String scoreboardTitle = plugin.getConfigManager().getScoreboardTitle();
            killsObjective = scoreboard.registerNewObjective("pvpboard", "dummy",
                    ChatColor.translateAlternateColorCodes('&', scoreboardTitle));
            killsObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

            plugin.getLogger().info("Scoreboard initialized successfully!");

            startScoreboardUpdater();
            Bukkit.getScheduler().runTaskLater(plugin, this::refreshAllScoreboards, 20L);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize scoreboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startScoreboardUpdater() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;

        int updateInterval = plugin.getConfigManager().getScoreboardUpdateInterval();

        scoreboardUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                if (scoreboard == null || killsObjective == null) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        };

        scoreboardUpdater.runTaskTimer(plugin, updateInterval, updateInterval);
    }

    public boolean isPVPEnabled(Player player) {
        return pvpPlayers.containsKey(player.getUniqueId()) &&
                !disabledWorlds.contains(player.getWorld().getName());
    }

    public void enablePVP(Player player) {
        if (disabledWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getPVPDisabledWorldMessage()));
            return;
        }

        UUID uuid = player.getUniqueId();
        if (pvpPlayers.containsKey(uuid)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getPVPAlreadyEnabledMessage()));
            return;
        }

        cancelCountdown(player);

        if (plugin.getConfigManager().isEnableCountdownEnabled()) {
            startCountdown(player, CountdownType.ENABLE);
        } else {
            activatePVPImmediately(player);
        }
    }

    public void disablePVP(Player player) {
        UUID uuid = player.getUniqueId();

        cancelCountdown(player);

        if (!pvpPlayers.containsKey(uuid)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getPVPNotEnabledMessage()));
            return;
        }

        if (plugin.getConfigManager().shouldPreventPVPDisable() && isInCombat(player)) {
            long timeLeft = getCombatTimeLeft(player) / 1000;
            String message = plugin.getConfigManager().getCombatTaggedMessage()
                    .replace("{time}", String.valueOf(timeLeft));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (plugin.getConfigManager().isDisableCountdownEnabled()) {
            startCountdown(player, CountdownType.DISABLE);
        } else {
            deactivatePVPImmediately(player);
        }
    }

    private void activatePVPImmediately(Player player) {
        UUID uuid = player.getUniqueId();

        savePlayerInventory(player);

        PVPPlayer pvpPlayer = new PVPPlayer(player);
        pvpPlayers.put(uuid, pvpPlayer);

        giveEquipment(player);
        updatePlayerScoreboard(player);

        if (plugin.getConfigManager().isGlowingEnabled()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        }

        for (String command : plugin.getConfigManager().getActivationCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("{player}", player.getName()));
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfigManager().getPVPEnabledMessage()));
    }

    private void deactivatePVPImmediately(Player player) {
        UUID uuid = player.getUniqueId();

        pvpPlayers.remove(uuid);
        combatTime.remove(uuid);

        restorePlayerInventory(player);

        player.removePotionEffect(PotionEffectType.GLOWING);

        if (player.hasPermission("hubpvp.fly")) {
            player.setAllowFlight(true);
        }

        for (String command : plugin.getConfigManager().getDeactivationCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("{player}", player.getName()));
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfigManager().getPVPDisabledMessage()));
    }

    private void startCountdown(Player player, CountdownType type) {
        UUID uuid = player.getUniqueId();

        cancelCountdown(player);

        int countdownTime;
        String startMessage;
        String title;
        String subtitle;
        String sound;
        boolean cancelOnMove;
        boolean cancelOnDamage;

        if (type == CountdownType.ENABLE) {
            countdownTime = plugin.getConfigManager().getEnableCountdownTime();
            startMessage = plugin.getConfigManager().getEnableCountdownStartedMessage()
                    .replace("{time}", String.valueOf(countdownTime));
            title = plugin.getConfigManager().getEnableCountdownTitle();
            subtitle = plugin.getConfigManager().getEnableCountdownSubtitle();
            sound = plugin.getConfigManager().getEnableCountdownSound();
            cancelOnMove = plugin.getConfigManager().shouldEnableCancelOnMove();
            cancelOnDamage = plugin.getConfigManager().shouldEnableCancelOnDamage();
        } else {
            countdownTime = plugin.getConfigManager().getDisableCountdownTime();
            startMessage = plugin.getConfigManager().getDisableCountdownStartedMessage()
                    .replace("{time}", String.valueOf(countdownTime));
            title = plugin.getConfigManager().getDisableCountdownTitle();
            subtitle = plugin.getConfigManager().getDisableCountdownSubtitle();
            sound = plugin.getConfigManager().getDisableCountdownSound();
            cancelOnMove = plugin.getConfigManager().shouldDisableCancelOnMove();
            cancelOnDamage = plugin.getConfigManager().shouldDisableCancelOnDamage();
        }

        Location startLocation = player.getLocation().clone();

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', startMessage));

        BukkitRunnable countdownTask = new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    countdownTasks.remove(uuid);
                    return;
                }

                if (cancelOnMove) {
                    if (startLocation.distance(player.getLocation()) > 1.0) {
                        cancelCountdown(player);
                        return;
                    }
                }

                if (timeLeft <= 0) {
                    countdownTasks.remove(uuid);
                    if (type == CountdownType.ENABLE) {
                        activatePVPImmediately(player);
                    } else {
                        deactivatePVPImmediately(player);
                    }
                    cancel();
                    return;
                }

                String displayTitle = ChatColor.translateAlternateColorCodes('&', title);
                String displaySubtitle = ChatColor.translateAlternateColorCodes('&',
                        subtitle.replace("{time}", String.valueOf(timeLeft)));

                try {
                    player.sendTitle(displayTitle, displaySubtitle, 10, 20, 10);
                } catch (Exception e) {
                    player.sendMessage(displaySubtitle);
                }

                try {
                    Sound soundEnum = Sound.valueOf(sound);
                    player.playSound(player.getLocation(), soundEnum, 1.0f, 1.0f);
                } catch (Exception e) {
                }

                timeLeft--;
            }
        };

        CountdownData countdownData = new CountdownData(type, startLocation, countdownTask);
        countdownTasks.put(uuid, countdownData);
        countdownTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancelCountdown(Player player) {
        UUID uuid = player.getUniqueId();
        CountdownData data = countdownTasks.remove(uuid);
        if (data != null) {
            data.task.cancel();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getCountdownCancelledMessage()));
        }
    }

    public boolean isInCountdown(Player player) {
        return countdownTasks.containsKey(player.getUniqueId());
    }

    public CountdownType getCountdownType(Player player) {
        CountdownData data = countdownTasks.get(player.getUniqueId());
        return data != null ? data.type : null;
    }

    public boolean isInEnableCountdown(Player player) {
        CountdownType type = getCountdownType(player);
        return type == CountdownType.ENABLE;
    }

    public boolean isInDisableCountdown(Player player) {
        CountdownType type = getCountdownType(player);
        return type == CountdownType.DISABLE;
    }

    public void handleCountdownDamage(Player player) {
        CountdownData data = countdownTasks.get(player.getUniqueId());
        if (data == null) return;

        boolean shouldCancel = false;
        if (data.type == CountdownType.ENABLE && plugin.getConfigManager().shouldEnableCancelOnDamage()) {
            shouldCancel = true;
        } else if (data.type == CountdownType.DISABLE && plugin.getConfigManager().shouldDisableCancelOnDamage()) {
            shouldCancel = true;
        }

        if (shouldCancel) {
            cancelCountdown(player);
        }
    }

    public void handleCountdownMovement(Player player, Location from, Location to) {
        CountdownData data = countdownTasks.get(player.getUniqueId());
        if (data == null) return;

        boolean shouldCancel = false;
        if (data.type == CountdownType.ENABLE && plugin.getConfigManager().shouldEnableCancelOnMove()) {
            shouldCancel = true;
        } else if (data.type == CountdownType.DISABLE && plugin.getConfigManager().shouldDisableCancelOnMove()) {
            shouldCancel = true;
        }

        if (shouldCancel) {
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ() ||
                    Math.abs(from.getY() - to.getY()) > 0.1)) {
                cancelCountdown(player);
            }
        }
    }

    public void enterCombat(Player player) {
        UUID uuid = player.getUniqueId();
        boolean wasInCombat = combatTime.containsKey(uuid);

        combatTime.put(uuid, System.currentTimeMillis());

        if (!wasInCombat) {
            if (player.isFlying()) {
                player.setFlying(false);
            }
            player.setAllowFlight(false);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getCombatEnteredMessage()));
        }
    }

    public void exitCombat(Player player) {
        combatTime.remove(player.getUniqueId());

        if (player.hasPermission("hubpvp.fly")) {
            player.setAllowFlight(true);
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfigManager().getCombatExitedMessage()));
    }

    public boolean isInCombat(Player player) {
        return combatTime.containsKey(player.getUniqueId());
    }

    public long getCombatTimeLeft(Player player) {
        if (!isInCombat(player)) return 0;

        long combatStart = combatTime.get(player.getUniqueId());

        long combatDuration;
        if (plugin.getConfigManager().isCombatTagEnabled()) {
            combatDuration = plugin.getConfigManager().getCombatTagDuration() * 1000L;
        } else {
            combatDuration = plugin.getConfigManager().getCombatTime() * 1000L;
        }

        return Math.max(0, combatDuration - (System.currentTimeMillis() - combatStart));
    }

    private void startCombatTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                for (Map.Entry<UUID, Long> entry : new HashMap<>(combatTime).entrySet()) {
                    long combatDuration;
                    if (plugin.getConfigManager().isCombatTagEnabled()) {
                        combatDuration = plugin.getConfigManager().getCombatTagDuration() * 1000L;
                    } else {
                        combatDuration = plugin.getConfigManager().getCombatTime() * 1000L;
                    }

                    if (currentTime - entry.getValue() >= combatDuration) {
                        Player player = Bukkit.getPlayer(entry.getKey());
                        if (player != null && player.isOnline()) {
                            exitCombat(player);
                        } else {
                            combatTime.remove(entry.getKey());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void savePlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerInventory savedInventory = new PlayerInventory();

        savedInventory.helmet = player.getInventory().getHelmet();
        savedInventory.chestplate = player.getInventory().getChestplate();
        savedInventory.leggings = player.getInventory().getLeggings();
        savedInventory.boots = player.getInventory().getBoots();

        savedInventory.contents = player.getInventory().getContents().clone();

        try {
            savedInventory.offHand = player.getInventory().getItemInOffHand();
        } catch (Exception e) {
            savedInventory.offHand = null;
        }

        savedInventories.put(uuid, savedInventory);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        try {
            player.getInventory().setItemInOffHand(null);
        } catch (Exception e) {
        }

        player.updateInventory();
    }

    private void restorePlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerInventory savedInventory = savedInventories.remove(uuid);

        if (savedInventory == null) {
            giveInactiveSword(player);
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getInventory().setHelmet(savedInventory.helmet);
        player.getInventory().setChestplate(savedInventory.chestplate);
        player.getInventory().setLeggings(savedInventory.leggings);
        player.getInventory().setBoots(savedInventory.boots);

        if (savedInventory.contents != null) {
            player.getInventory().setContents(savedInventory.contents);
        }

        try {
            if (savedInventory.offHand != null) {
                player.getInventory().setItemInOffHand(savedInventory.offHand);
            }
        } catch (Exception e) {
        }

        player.updateInventory();
    }

    public void setupPlayerForPVP(Player player) {
        UUID uuid = player.getUniqueId();

        if (pvpPlayers.containsKey(uuid)) {
            disablePVP(player);
        }

        combatTime.remove(uuid);
        cancelCountdown(player);

        if (plugin.getConfigManager().shouldGiveSwordOnJoin()) {
            giveInactiveSword(player);
        }

        if (isScoreboardReady()) {
            updatePlayerScoreboard(player);
        }
    }

    public void handlePlayerRespawn(Player player) {
        UUID uuid = player.getUniqueId();

        pvpPlayers.remove(uuid);
        combatTime.remove(uuid);
        cancelCountdown(player);

        if (savedInventories.containsKey(uuid)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    restorePlayerInventory(player);

                    if (plugin.getConfigManager().shouldGiveSwordOnJoin()) {
                        giveInactiveSword(player);
                    }

                    if (plugin.getConfigManager().shouldShowDeathResetMessage()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfigManager().getDeathResetMessage()));
                    }

                    if (isScoreboardReady()) {
                        updatePlayerScoreboard(player);
                    }

                    plugin.getLogger().info("Restored inventory for " + player.getName() + " on respawn");
                }
            }, 1L);
        } else {
            setupPlayerForPVP(player);
        }
    }

    private void giveEquipment(Player player) {
        ConfigManager config = plugin.getConfigManager();

        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().setItem(config.getSwordSlot(), null);

        ItemStack helmet = createCustomItem(config.getHelmetMaterial(), config.getHelmetName(),
                config.getHelmetLore(), config.getHelmetEnchantments());
        ItemStack chestplate = createCustomItem(config.getChestplateMaterial(), config.getChestplateName(),
                config.getChestplateLore(), config.getChestplateEnchantments());
        ItemStack leggings = createCustomItem(config.getLeggingsMaterial(), config.getLeggingsName(),
                config.getLeggingsLore(), config.getLeggingsEnchantments());
        ItemStack boots = createCustomItem(config.getBootsMaterial(), config.getBootsName(),
                config.getBootsLore(), config.getBootsEnchantments());

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        ItemStack activeSword = createCustomItem(config.getSwordMaterial(), config.getSwordName(),
                config.getSwordActiveLore(), config.getSwordEnchantments());
        player.getInventory().setItem(config.getSwordSlot(), activeSword);

        player.updateInventory();
    }

    public void giveInactiveSword(Player player) {
        if (!plugin.getConfigManager().shouldGiveSwordOnJoin()) return;

        ConfigManager config = plugin.getConfigManager();
        int slot = config.getSwordSlot();

        player.getInventory().setItem(slot, null);

        ItemStack inactiveSword = createCustomItem(
                config.getSwordMaterial(),
                config.getSwordName(),
                config.getSwordInactiveLore(),
                new HashMap<>()
        );

        player.getInventory().setItem(slot, inactiveSword);
        player.updateInventory();

        if (config.shouldShowSwordMessage()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getSwordReceivedMessage()));
        }
    }

    private ItemStack createCustomItem(Material material, String name, List<String> lore,
                                       Map<String, Integer> enchantments) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
        }

        meta.setUnbreakable(true);
        item.setItemMeta(meta);

        if (enchantments != null) {
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                try {
                    Enchantment enchant = Enchantment.getByName(entry.getKey().toUpperCase());
                    if (enchant != null) {
                        item.addUnsafeEnchantment(enchant, entry.getValue());
                    }
                } catch (Exception e) {
                }
            }
        }

        return item;
    }

    public void resetPlayerOnDeath(Player player) {
        UUID uuid = player.getUniqueId();

        pvpPlayers.remove(uuid);
        combatTime.remove(uuid);
        cancelCountdown(player);

        player.removePotionEffect(PotionEffectType.GLOWING);

        if (player.hasPermission("hubpvp.fly")) {
            player.setAllowFlight(true);
        }

        plugin.getLogger().info("Reset PVP state for " + player.getName() + " on death");
    }

    public void handleKill(Player killer, Player victim) {
        UUID killerUUID = killer.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        killStreaks.put(victimUUID, 0);

        int currentStreak = killStreaks.getOrDefault(killerUUID, 0) + 1;
        killStreaks.put(killerUUID, currentStreak);
        totalKills.put(killerUUID, totalKills.getOrDefault(killerUUID, 0) + 1);

        updatePlayerScoreboard(killer);
        updatePlayerScoreboard(victim);

        String killMessage = plugin.getConfigManager().getKillMessage()
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName())
                .replace("{killstreak}", String.valueOf(currentStreak));

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', killMessage));

        Map<Integer, List<String>> streakCommands = plugin.getConfigManager().getKillstreakCommands();
        if (streakCommands.containsKey(currentStreak)) {
            for (String command : streakCommands.get(currentStreak)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("{player}", killer.getName())
                                .replace("{killstreak}", String.valueOf(currentStreak)));
            }
        }

        for (String command : plugin.getConfigManager().getKillCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("{killer}", killer.getName())
                            .replace("{victim}", victim.getName()));
        }
    }

    private void updatePlayerScoreboard(Player player) {
        if (scoreboard == null || killsObjective == null) return;
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;

        try {
            player.setScoreboard(scoreboard);

            for (String entry : scoreboard.getEntries()) {
                scoreboard.resetScores(entry);
            }

            List<String> lines = plugin.getConfigManager().getScoreboardLines();
            if (lines.isEmpty()) return;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                line = replacePlaceholders(player, line);

                if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    try {
                        line = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, line);
                    } catch (Exception e) {
                    }
                }

                line = ChatColor.translateAlternateColorCodes('&', line);

                String entry = line;
                if (line.length() > 40) {
                    entry = line.substring(0, 40);
                }

                StringBuilder uniqueEntry = new StringBuilder(entry);
                for (int j = 0; j < i; j++) {
                    uniqueEntry.append("§r");
                }

                Score score = killsObjective.getScore(uniqueEntry.toString());
                score.setScore(lines.size() - i);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update scoreboard for " + player.getName() + ": " + e.getMessage());
        }
    }

    private String replacePlaceholders(Player player, String text) {
        if (text == null) return "";

        text = text.replace("%hubpvp_killstreak%", String.valueOf(getKillStreak(player)));
        text = text.replace("%hubpvp_kills%", String.valueOf(getTotalKills(player)));
        text = text.replace("%hubpvp_status%", isPVPEnabled(player) ? "Enabled" : "Disabled");
        text = text.replace("%hubpvp_status_color%", isPVPEnabled(player) ? "&a" : "&c");
        text = text.replace("%hubpvp_combat%", isInCombat(player) ? "Yes" : "No");
        text = text.replace("%hubpvp_combat_color%", isInCombat(player) ? "&c" : "&a");
        text = text.replace("%hubpvp_countdown%", isInCountdown(player) ? "Yes" : "No");
        text = text.replace("%hubpvp_countdown_color%", isInCountdown(player) ? "&e" : "&a");

        if (isCombatTagEnabled() && isInCombat(player)) {
            text = text.replace("%hubpvp_combat_tag%", "Yes");
            text = text.replace("%hubpvp_combat_tag_time%", String.valueOf(getCombatTimeLeft(player) / 1000));
        } else {
            text = text.replace("%hubpvp_combat_tag%", "No");
            text = text.replace("%hubpvp_combat_tag_time%", "0");
        }

        List<Player> topPlayers = getTopKillers(10);
        for (int i = 1; i <= 10; i++) {
            if (i <= topPlayers.size()) {
                Player topPlayer = topPlayers.get(i - 1);
                text = text.replace("%hubpvp_top_" + i + "_name%", topPlayer.getName());
                text = text.replace("%hubpvp_top_" + i + "_kills%", String.valueOf(getTotalKills(topPlayer)));
            } else {
                text = text.replace("%hubpvp_top_" + i + "_name%", "None");
                text = text.replace("%hubpvp_top_" + i + "_kills%", "0");
            }
        }

        return text;
    }

    public void refreshAllScoreboards() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;
        if (scoreboard == null || killsObjective == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player);
        }
    }

    public void refreshScoreboardTitle() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) {
            plugin.getLogger().info("Scoreboard is disabled in config.");
            return;
        }

        if (scoreboard == null || killsObjective == null) {
            plugin.getLogger().warning("Cannot refresh scoreboard title - scoreboard not initialized!");
            return;
        }

        try {
            killsObjective.unregister();

            String newTitle = plugin.getConfigManager().getScoreboardTitle();
            killsObjective = scoreboard.registerNewObjective("pvpboard", "dummy",
                    ChatColor.translateAlternateColorCodes('&', newTitle));
            killsObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

            if (scoreboardUpdater != null) {
                scoreboardUpdater.cancel();
            }
            startScoreboardUpdater();

            refreshAllScoreboards();

            plugin.getLogger().info("Scoreboard refreshed with title: " + newTitle);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to refresh scoreboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disableAllPVP() {
        for (UUID uuid : new HashSet<>(countdownTasks.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                cancelCountdown(player);
            }
        }

        for (UUID uuid : new HashSet<>(pvpPlayers.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                disablePVP(player);
            }
        }

        savedInventories.clear();

        if (scoreboardUpdater != null) {
            scoreboardUpdater.cancel();
            scoreboardUpdater = null;
        }
    }

    public int getKillStreak(Player player) {
        return killStreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public int getTotalKills(Player player) {
        return totalKills.getOrDefault(player.getUniqueId(), 0);
    }

    public Set<Player> getPVPPlayers() {
        Set<Player> players = new HashSet<>();
        for (UUID uuid : pvpPlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    public void resetStats(Player player) {
        UUID uuid = player.getUniqueId();
        killStreaks.put(uuid, 0);
        totalKills.put(uuid, 0);
        updatePlayerScoreboard(player);
    }

    public List<Player> getTopKillers(int limit) {
        return totalKills.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> Bukkit.getPlayer(entry.getKey()))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(ArrayList::new, (list, player) -> list.add(player), ArrayList::addAll);
    }

    public boolean isScoreboardReady() {
        return scoreboard != null && killsObjective != null && plugin.getConfigManager().isScoreboardEnabled();
    }

    public boolean isCombatTagEnabled() {
        return plugin.getConfigManager().isCombatTagEnabled();
    }

    public void cleanupPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        savedInventories.remove(uuid);
        combatTime.remove(uuid);
        cancelCountdown(player);
    }

    private static class PVPPlayer {
        private final UUID uuid;
        private final String name;
        private final long enabledTime;

        public PVPPlayer(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
            this.enabledTime = System.currentTimeMillis();
        }
    }

    private static class PlayerInventory {
        public ItemStack helmet;
        public ItemStack chestplate;
        public ItemStack leggings;
        public ItemStack boots;
        public ItemStack[] contents;
        public ItemStack offHand;
    }
}