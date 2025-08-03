package exsistory.dev.hubpvp.api;

import org.bukkit.entity.Player;
import java.util.List;
import java.util.Set;

public interface HubPVPAPI {

    static HubPVPAPI getInstance() {
        return exsistory.dev.hubpvp.api.HubPVPAPIImpl.getInstance();
    }

    boolean isPVPEnabled(Player player);
    void enablePVP(Player player);
    void disablePVP(Player player);

    boolean isInCombat(Player player);
    long getCombatTimeLeft(Player player);
    void enterCombat(Player player);
    void exitCombat(Player player);

    boolean isInCountdown(Player player);
    CountdownType getCountdownType(Player player);
    boolean isInEnableCountdown(Player player);
    boolean isInDisableCountdown(Player player);
    void cancelCountdown(Player player);

    int getKillStreak(Player player);
    int getTotalKills(Player player);
    void resetStats(Player player);
    List<Player> getTopKillers(int limit);
    Set<Player> getPVPPlayers();

    boolean isEnableCountdownEnabled();
    boolean isDisableCountdownEnabled();
    int getEnableCountdownTime();
    int getDisableCountdownTime();
    boolean isCombatTagEnabled();
    int getCombatTagDuration();
}