package exsistory.dev.hubpvp;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import java.util.List;

public class PVPPlaceholders extends PlaceholderExpansion {

    private final HubPVP plugin;
    private final PVPManager pvpManager;

    public PVPPlaceholders(HubPVP plugin) {
        this.plugin = plugin;
        this.pvpManager = plugin.getPVPManager();
    }

    @Override
    public String getIdentifier() {
        return "hubpvp";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        switch (params.toLowerCase()) {
            case "killstreak":
            case "streak":
                return String.valueOf(pvpManager.getKillStreak(player));

            case "kills":
            case "totalkills":
                return String.valueOf(pvpManager.getTotalKills(player));

            case "status":
            case "enabled":
                return pvpManager.isPVPEnabled(player) ? "Enabled" : "Disabled";

            case "status_boolean":
                return String.valueOf(pvpManager.isPVPEnabled(player));

            case "combat":
            case "incombat":
                return pvpManager.isInCombat(player) ? "Yes" : "No";

            case "combat_boolean":
                return String.valueOf(pvpManager.isInCombat(player));

            case "combat_time":
            case "combattime":
                if (pvpManager.isInCombat(player)) {
                    return String.valueOf(pvpManager.getCombatTimeLeft(player) / 1000);
                }
                return "0";

            case "combat_time_formatted":
                if (pvpManager.isInCombat(player)) {
                    long seconds = pvpManager.getCombatTimeLeft(player) / 1000;
                    return formatTime(seconds);
                }
                return "00:00";

            case "countdown":
            case "incountdown":
                return pvpManager.isInCountdown(player) ? "Yes" : "No";

            case "countdown_boolean":
                return String.valueOf(pvpManager.isInCountdown(player));

            case "countdown_type":
                PVPManager.CountdownType type = pvpManager.getCountdownType(player);
                if (type == null) return "None";
                return type == PVPManager.CountdownType.ENABLE ? "Enable" : "Disable";

            case "countdown_enable":
            case "countdown_enabling":
                return pvpManager.isInEnableCountdown(player) ? "Yes" : "No";

            case "countdown_enable_boolean":
                return String.valueOf(pvpManager.isInEnableCountdown(player));

            case "countdown_disable":
            case "countdown_disabling":
                return pvpManager.isInDisableCountdown(player) ? "Yes" : "No";

            case "countdown_disable_boolean":
                return String.valueOf(pvpManager.isInDisableCountdown(player));

            case "rank":
            case "ranking":
                return String.valueOf(getPlayerRank(player));

            case "status_color":
                return pvpManager.isPVPEnabled(player) ? "&a" : "&c";

            case "combat_color":
                return pvpManager.isInCombat(player) ? "&c" : "&a";

            case "countdown_color":
                return pvpManager.isInCountdown(player) ? "&e" : "&a";

            case "countdown_type_color":
                PVPManager.CountdownType countdownType = pvpManager.getCountdownType(player);
                if (countdownType == null) return "&7";
                return countdownType == PVPManager.CountdownType.ENABLE ? "&c" : "&a";

            case "combat_tag":
            case "combattag":
                return pvpManager.isCombatTagEnabled() && pvpManager.isInCombat(player) ? "Yes" : "No";

            case "combat_tag_boolean":
                return String.valueOf(pvpManager.isCombatTagEnabled() && pvpManager.isInCombat(player));

            case "combat_tag_time":
                if (pvpManager.isCombatTagEnabled() && pvpManager.isInCombat(player)) {
                    return String.valueOf(pvpManager.getCombatTimeLeft(player) / 1000);
                }
                return "0";

            case "combat_tag_time_formatted":
                if (pvpManager.isCombatTagEnabled() && pvpManager.isInCombat(player)) {
                    long seconds = pvpManager.getCombatTimeLeft(player) / 1000;
                    return formatTime(seconds);
                }
                return "00:00";
        }

        if (params.startsWith("top_")) {
            return handleTopPlaceholders(params);
        }

        if (params.startsWith("global_")) {
            return handleGlobalPlaceholders(params);
        }

        return null;
    }

    private String handleTopPlaceholders(String params) {
        String[] parts = params.split("_");
        if (parts.length < 3) return "";

        try {
            int position = Integer.parseInt(parts[1]) - 1;
            String type = parts[2].toLowerCase();

            List<Player> topPlayers = pvpManager.getTopKillers(50);

            if (position >= topPlayers.size()) {
                return type.equals("name") ? "N/A" : "0";
            }

            Player topPlayer = topPlayers.get(position);

            switch (type) {
                case "name":
                    return topPlayer.getName();
                case "kills":
                    return String.valueOf(pvpManager.getTotalKills(topPlayer));
                case "streak":
                case "killstreak":
                    return String.valueOf(pvpManager.getKillStreak(topPlayer));
                default:
                    return "";
            }
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private String handleGlobalPlaceholders(String params) {
        String[] parts = params.split("_");
        if (parts.length < 2) return "";

        String type = parts[1].toLowerCase();

        switch (type) {
            case "players":
            case "total":
                return String.valueOf(pvpManager.getPVPPlayers().size());

            case "combat":
            case "incombat":
                long combatCount = pvpManager.getPVPPlayers().stream()
                        .mapToLong(p -> pvpManager.isInCombat(p) ? 1 : 0)
                        .sum();
                return String.valueOf(combatCount);

            case "safe":
                long safeCount = pvpManager.getPVPPlayers().stream()
                        .mapToLong(p -> pvpManager.isInCombat(p) ? 0 : 1)
                        .sum();
                return String.valueOf(safeCount);

            case "countdown":
            case "incountdown":
                long countdownCount = plugin.getServer().getOnlinePlayers().stream()
                        .mapToLong(p -> pvpManager.isInCountdown(p) ? 1 : 0)
                        .sum();
                return String.valueOf(countdownCount);

            case "countdown_enable":
            case "countdown_enabling":
                long enableCountdownCount = plugin.getServer().getOnlinePlayers().stream()
                        .mapToLong(p -> pvpManager.isInEnableCountdown(p) ? 1 : 0)
                        .sum();
                return String.valueOf(enableCountdownCount);

            case "countdown_disable":
            case "countdown_disabling":
                long disableCountdownCount = plugin.getServer().getOnlinePlayers().stream()
                        .mapToLong(p -> pvpManager.isInDisableCountdown(p) ? 1 : 0)
                        .sum();
                return String.valueOf(disableCountdownCount);

            default:
                return "";
        }
    }

    private int getPlayerRank(Player player) {
        List<Player> topPlayers = pvpManager.getTopKillers(1000);

        for (int i = 0; i < topPlayers.size(); i++) {
            if (topPlayers.get(i).getUniqueId().equals(player.getUniqueId())) {
                return i + 1;
            }
        }

        return topPlayers.size() + 1;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}