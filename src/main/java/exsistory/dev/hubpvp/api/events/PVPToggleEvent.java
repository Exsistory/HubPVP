package exsistory.dev.hubpvp.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PVPToggleEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final boolean newPVPStatus;
    private final boolean oldPVPStatus;
    private final ToggleReason reason;

    public enum ToggleReason {
        COMMAND, SWORD_CLICK, SWORD_HOLD, API_CALL,
        FORCE, DEATH, WORLD_CHANGE, PLUGIN_DISABLE
    }

    public PVPToggleEvent(Player player, boolean oldStatus, boolean newStatus, ToggleReason reason) {
        super(player);
        this.oldPVPStatus = oldStatus;
        this.newPVPStatus = newStatus;
        this.reason = reason;
    }

    public boolean getOldPVPStatus() {
        return oldPVPStatus;
    }

    public boolean getNewPVPStatus() {
        return newPVPStatus;
    }

    public boolean isEnabling() {
        return !oldPVPStatus && newPVPStatus;
    }

    public boolean isDisabling() {
        return oldPVPStatus && !newPVPStatus;
    }

    public ToggleReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}