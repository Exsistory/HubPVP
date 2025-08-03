package exsistory.dev.hubpvp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PVPListener implements Listener {

    private final HubPVP plugin;
    private final PVPManager pvpManager;
    private final ConfigManager configManager;

    public PVPListener(HubPVP plugin) {
        this.plugin = plugin;
        this.pvpManager = plugin.getPVPManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && pvpManager.isPVPEnabled(killer) && pvpManager.isPVPEnabled(victim)) {
            pvpManager.handleKill(killer, victim);
        }

        if (!pvpManager.isPVPEnabled(victim)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        pvpManager.resetPlayerOnDeath(victim);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        pvpManager.handleCountdownDamage(victim);
        pvpManager.handleCountdownDamage(attacker);

        if (configManager.shouldBlockPVPWhenDisabled()) {
            boolean victimHasPVP = pvpManager.isPVPEnabled(victim);
            boolean attackerHasPVP = pvpManager.isPVPEnabled(attacker);

            if (!victimHasPVP || !attackerHasPVP) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getPVPBlockedMessage()));
                return;
            }
        } else {
            if (!pvpManager.isPVPEnabled(victim) || !pvpManager.isPVPEnabled(attacker)) {
                event.setCancelled(true);
                return;
            }
        }

        pvpManager.enterCombat(victim);
        pvpManager.enterCombat(attacker);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        Material swordMaterial = configManager.getSwordMaterial();
        if (item.getType() == swordMaterial) {
            String activationMode = configManager.getActivationMode();

            if ("RIGHT_CLICK".equals(activationMode) &&
                    (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

                event.setCancelled(true);
                handlePVPToggle(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item == null) return;

        String activationMode = configManager.getActivationMode();
        if ("HOLD".equals(activationMode) && item.getType() == configManager.getSwordMaterial()) {
            if (!pvpManager.isPVPEnabled(player) && !pvpManager.isInCountdown(player)) {
                handlePVPToggle(player);
            }
        }
    }

    private void handlePVPToggle(Player player) {
        if (pvpManager.isInCountdown(player)) {
            pvpManager.cancelCountdown(player);
        } else if (pvpManager.isPVPEnabled(player)) {
            pvpManager.disablePVP(player);
        } else {
            pvpManager.enablePVP(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (pvpManager.isPVPEnabled(player)) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            if (isPVPEquipment(clickedItem) || isPVPEquipment(cursorItem)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getEquipmentLockedMessage()));
                return;
            }
        }

        int swordSlot = configManager.getSwordSlot();
        if (event.getSlot() == swordSlot && event.getClickedInventory() == player.getInventory()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == configManager.getSwordMaterial()) {
                event.setCancelled(true);
                handlePVPToggle(player);
            }
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        if (isPVPSword(clickedItem) || isPVPSword(cursorItem)) {
            if (!pvpManager.isPVPEnabled(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getEquipmentLockedMessage()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (pvpManager.isPVPEnabled(player)) {
            ItemStack draggedItem = event.getOldCursor();
            if (isPVPEquipment(draggedItem)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getEquipmentLockedMessage()));
                return;
            }
        }

        ItemStack draggedItem = event.getOldCursor();
        if (isPVPSword(draggedItem) && !pvpManager.isPVPEnabled(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getEquipmentLockedMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (pvpManager.isPVPEnabled(player) && isPVPEquipment(droppedItem)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getEquipmentLockedMessage()));
            return;
        }

        if (isPVPSword(droppedItem) && !pvpManager.isPVPEnabled(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getEquipmentLockedMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (pvpManager.isInCombat(player)) {
            for (String blockedCommand : configManager.getBlockedCommands()) {
                if (command.startsWith("/" + blockedCommand.toLowerCase())) {
                    event.setCancelled(true);
                    long timeLeft = pvpManager.getCombatTimeLeft(player) / 1000;
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            configManager.getCommandBlockedMessage().replace("{time}", String.valueOf(timeLeft))));
                    return;
                }
            }
        }

        if (pvpManager.isPVPEnabled(player) &&
                (command.startsWith("/fly") || command.startsWith("/flight"))) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getFlightBlockedMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (pvpManager.isInCountdown(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            pvpManager.handleCountdownMovement(player, from, to);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (pvpManager.isInCountdown(player)) {
            pvpManager.cancelCountdown(player);
        }

        if (configManager.getDisabledWorlds().contains(player.getWorld().getName()) &&
                pvpManager.isPVPEnabled(player)) {
            pvpManager.disablePVP(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                pvpManager.setupPlayerForPVP(player);
            }
        }, 40L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (pvpManager.isPVPEnabled(player)) {
            pvpManager.disablePVP(player);
        }

        pvpManager.cleanupPlayerData(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                pvpManager.handlePlayerRespawn(player);
            }
        }, 1L);
    }

    private boolean isPVPEquipment(ItemStack item) {
        if (item == null) return false;

        Material type = item.getType();

        if (type == configManager.getHelmetMaterial() ||
                type == configManager.getChestplateMaterial() ||
                type == configManager.getLeggingsMaterial() ||
                type == configManager.getBootsMaterial()) {
            return true;
        }

        return type == configManager.getSwordMaterial();
    }

    private boolean isPVPSword(ItemStack item) {
        if (item == null) return false;
        return item.getType() == configManager.getSwordMaterial();
    }
}