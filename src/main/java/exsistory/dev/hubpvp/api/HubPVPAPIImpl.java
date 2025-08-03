package exsistory.dev.hubpvp.api;

import exsistory.dev.hubpvp.HubPVP;
import exsistory.dev.hubpvp.PVPManager;
import exsistory.dev.hubpvp.ConfigManager;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Set;

public class HubPVPAPIImpl implements HubPVPAPI {
    private static HubPVPAPIImpl instance;
    private final HubPVP plugin;
    private final PVPManager pvpManager;
    private final ConfigManager configManager;

    public HubPVPAPIImpl(HubPVP plugin) {
        this.plugin = plugin;
        this.pvpManager = plugin.getPVPManager();
        this.configManager = plugin.getConfigManager();
        instance = this;
    }

    public static HubPVPAPIImpl getInstance() {
        if (instance == null) {
            throw new IllegalStateException("HubPVP is not loaded! Make sure HubPVP plugin is enabled.");
        }
        return instance;
    }

    @Override
    public boolean isPVPEnabled(Player player) {
        return pvpManager.isPVPEnabled(player);
    }

    @Override
    public void enablePVP(Player player) {
        pvpManager.enablePVP(player);
    }

    @Override
    public void disablePVP(Player player) {
        pvpManager.disablePVP(player);
    }

    @Override
    public boolean isInCombat(Player player) {
        return pvpManager.isInCombat(player);
    }

    @Override
    public long getCombatTimeLeft(Player player) {
        return pvpManager.getCombatTimeLeft(player);
    }

    @Override
    public void enterCombat(Player player) {
        pvpManager.enterCombat(player);
    }

    @Override
    public void exitCombat(Player player) {
        pvpManager.exitCombat(player);
    }

    @Override
    public boolean isInCountdown(Player player) {
        return pvpManager.isInCountdown(player);
    }

    @Override
    public CountdownType getCountdownType(Player player) {
        PVPManager.CountdownType internalType = pvpManager.getCountdownType(player);
        if (internalType == null) return null;
        return internalType == PVPManager.CountdownType.ENABLE ?
                CountdownType.ENABLE : CountdownType.DISABLE;
    }

    @Override
    public boolean isInEnableCountdown(Player player) {
        return pvpManager.isInEnableCountdown(player);
    }

    @Override
    public boolean isInDisableCountdown(Player player) {
        return pvpManager.isInDisableCountdown(player);
    }

    @Override
    public void cancelCountdown(Player player) {
        pvpManager.cancelCountdown(player);
    }

    @Override
    public int getKillStreak(Player player) {
        return pvpManager.getKillStreak(player);
    }

    @Override
    public int getTotalKills(Player player) {
        return pvpManager.getTotalKills(player);
    }

    @Override
    public void resetStats(Player player) {
        pvpManager.resetStats(player);
    }

    @Override
    public List<Player> getTopKillers(int limit) {
        return pvpManager.getTopKillers(limit);
    }

    @Override
    public Set<Player> getPVPPlayers() {
        return pvpManager.getPVPPlayers();
    }

    @Override
    public boolean isEnableCountdownEnabled() {
        return configManager.isEnableCountdownEnabled();
    }

    @Override
    public boolean isDisableCountdownEnabled() {
        return configManager.isDisableCountdownEnabled();
    }

    @Override
    public int getEnableCountdownTime() {
        return configManager.getEnableCountdownTime();
    }

    @Override
    public int getDisableCountdownTime() {
        return configManager.getDisableCountdownTime();
    }

    @Override
    public boolean isCombatTagEnabled() {
        return configManager.isCombatTagEnabled();
    }

    @Override
    public int getCombatTagDuration() {
        return configManager.getCombatTagDuration();
    }

    public static void cleanup() {
        instance = null;
    }
}