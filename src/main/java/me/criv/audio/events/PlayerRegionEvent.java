package me.criv.audio.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRegionEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String oldRegion;
    private final String newRegion;

    public PlayerRegionEvent(Player player, String fromRegion, String toRegion) {
        this.player = player;
        this.oldRegion = fromRegion;
        this.newRegion = toRegion;
    }

    public Player getPlayer() {
        return player;
    }

    public String getOldRegion() {
        return oldRegion;
    }

    public String getNewRegion() {
        return newRegion;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
