package exsistory.dev.hubpvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PVPCommand implements CommandExecutor, TabCompleter {

    private final HubPVP plugin;
    private final PVPManager pvpManager;
    private final ConfigManager configManager;

    public PVPCommand(HubPVP plugin) {
        this.plugin = plugin;
        this.pvpManager = plugin.getPVPManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (pvpManager.isPVPEnabled(player)) {
                    pvpManager.disablePVP(player);
                } else {
                    pvpManager.enablePVP(player);
                }
            } else {
                sendHelpMessage(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "enable":
            case "on":
                handleEnable(sender);
                break;

            case "disable":
            case "off":
                handleDisable(sender);
                break;

            case "toggle":
                handleToggle(sender);
                break;

            case "stats":
                handleStats(sender, args);
                break;

            case "top":
                handleTop(sender, args);
                break;

            case "reset":
                handleReset(sender, args);
                break;

            case "reload":
                handleReload(sender);
                break;

            case "list":
                handleList(sender);
                break;

            case "combat":
                handleCombat(sender, args);
                break;

            case "help":
            case "?":
                sendHelpMessage(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /hubpvp help for assistance.");
                break;
        }

        return true;
    }

    private void handleEnable(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        pvpManager.enablePVP(player);
    }

    private void handleDisable(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        pvpManager.disablePVP(player);
    }

    private void handleToggle(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        if (pvpManager.isPVPEnabled(player)) {
            pvpManager.disablePVP(player);
        } else {
            pvpManager.enablePVP(player);
        }
    }

    private void handleStats(CommandSender sender, String[] args) {
        Player target;

        if (args.length > 1) {
            if (!sender.hasPermission("hubpvp.stats.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to view other players' stats.");
                return;
            }

            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Please specify a player name.");
                return;
            }
            target = (Player) sender;
        }

        int killStreak = pvpManager.getKillStreak(target);
        int totalKills = pvpManager.getTotalKills(target);
        boolean pvpEnabled = pvpManager.isPVPEnabled(target);
        boolean inCombat = pvpManager.isInCombat(target);

        sender.sendMessage(ChatColor.GOLD + "===== " + target.getName() + "'s PVP Stats =====");
        sender.sendMessage(ChatColor.YELLOW + "Kill Streak: " + ChatColor.WHITE + killStreak);
        sender.sendMessage(ChatColor.YELLOW + "Total Kills: " + ChatColor.WHITE + totalKills);
        sender.sendMessage(ChatColor.YELLOW + "PVP Status: " + (pvpEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "Combat Status: " + (inCombat ? ChatColor.RED + "In Combat" : ChatColor.GREEN + "Safe"));

        if (inCombat) {
            long timeLeft = pvpManager.getCombatTimeLeft(target) / 1000;
            sender.sendMessage(ChatColor.YELLOW + "Combat Time Left: " + ChatColor.WHITE + timeLeft + "s");
        }
    }

    private void handleTop(CommandSender sender, String[] args) {
        int limit = 10;
        if (args.length > 1) {
            try {
                limit = Integer.parseInt(args[1]);
                limit = Math.min(Math.max(limit, 1), 50);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number format.");
                return;
            }
        }

        List<Player> topKillers = pvpManager.getTopKillers(limit);

        if (topKillers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No PVP statistics available.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "===== Top " + limit + " PVP Players =====");
        for (int i = 0; i < topKillers.size(); i++) {
            Player player = topKillers.get(i);
            int kills = pvpManager.getTotalKills(player);
            int streak = pvpManager.getKillStreak(player);

            sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + ChatColor.WHITE + player.getName() +
                    ChatColor.GRAY + " - " + ChatColor.GREEN + kills + " kills" +
                    ChatColor.GRAY + " (" + ChatColor.GOLD + streak + " streak" + ChatColor.GRAY + ")");
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hubpvp.admin.reset")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reset stats.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hubpvp reset <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        pvpManager.resetStats(target);
        sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s PVP statistics.");
        target.sendMessage(ChatColor.YELLOW + "Your PVP statistics have been reset by " + sender.getName() + ".");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("hubpvp.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload the configuration.");
            return;
        }

        configManager.loadConfig();

        if (pvpManager.isScoreboardReady()) {
            pvpManager.refreshScoreboardTitle();
        }

        sender.sendMessage(ChatColor.GREEN + "HubPVP configuration reloaded successfully.");
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("hubpvp.admin.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list PVP players.");
            return;
        }

        List<Player> pvpPlayers = new ArrayList<>(pvpManager.getPVPPlayers());

        if (pvpPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No players currently have PVP enabled.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "===== PVP Enabled Players (" + pvpPlayers.size() + ") =====");
        for (Player player : pvpPlayers) {
            boolean inCombat = pvpManager.isInCombat(player);
            sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + player.getName() +
                    (inCombat ? ChatColor.RED + " [COMBAT]" : ChatColor.GREEN + " [SAFE]"));
        }
    }

    private void handleCombat(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Please specify a player name.");
                return;
            }

            Player player = (Player) sender;
            if (pvpManager.isInCombat(player)) {
                long timeLeft = pvpManager.getCombatTimeLeft(player) / 1000;
                sender.sendMessage(ChatColor.YELLOW + "You are in combat for " + timeLeft + " more seconds.");
            } else {
                sender.sendMessage(ChatColor.GREEN + "You are not in combat.");
            }
            return;
        }

        if (!sender.hasPermission("hubpvp.admin.combat")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to check other players' combat status.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (pvpManager.isInCombat(target)) {
            long timeLeft = pvpManager.getCombatTimeLeft(target) / 1000;
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " is in combat for " + timeLeft + " more seconds.");
        } else {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " is not in combat.");
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== HubPVP Commands =====");
        sender.sendMessage(ChatColor.YELLOW + "/pvp" + ChatColor.WHITE + " - Toggle PVP mode");
        sender.sendMessage(ChatColor.YELLOW + "/pvp enable" + ChatColor.WHITE + " - Enable PVP mode");
        sender.sendMessage(ChatColor.YELLOW + "/pvp disable" + ChatColor.WHITE + " - Disable PVP mode");
        sender.sendMessage(ChatColor.YELLOW + "/pvp stats [player]" + ChatColor.WHITE + " - View PVP statistics");
        sender.sendMessage(ChatColor.YELLOW + "/pvp top [number]" + ChatColor.WHITE + " - View top PVP players");
        sender.sendMessage(ChatColor.YELLOW + "/pvp combat [player]" + ChatColor.WHITE + " - Check combat status");

        if (sender.hasPermission("hubpvp.admin")) {
            sender.sendMessage(ChatColor.GOLD + "===== Admin Commands =====");
            sender.sendMessage(ChatColor.YELLOW + "/pvp reset <player>" + ChatColor.WHITE + " - Reset player's stats");
            sender.sendMessage(ChatColor.YELLOW + "/pvp reload" + ChatColor.WHITE + " - Reload configuration");
            sender.sendMessage(ChatColor.YELLOW + "/pvp list" + ChatColor.WHITE + " - List PVP enabled players");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("enable", "disable", "toggle", "stats", "top", "combat", "help");
            if (sender.hasPermission("hubpvp.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.addAll(Arrays.asList("reset", "reload", "list"));
            }

            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("stats") || subCommand.equals("reset") || subCommand.equals("combat")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            } else if (subCommand.equals("top")) {
                completions.addAll(Arrays.asList("5", "10", "15", "20"));
            }
        }

        return completions;
    }
}