package me.criv.audio;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Data {
    static HashMap<UUID, Data> playerData = new HashMap<>();

    private Player player;
    private boolean transitioning;
    private double height;
    private State state;

    enum State {
        INACTIVE,
        ASCENDING,
        SWITCH,
        DESCENDING
    }
    public Data(Player player) {
        this.player = player;
        this.transitioning = false;
        this.height = 0;
        this.state = State.INACTIVE;
        playerData.put(player.getUniqueId(), this);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean getTransitioning() {
        return transitioning;
    }

    public State getState() {
        return state;
    }

    public double getHeight() {
        return height;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setTransitioning(Boolean transitioning) {
        this.transitioning = transitioning;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static Data getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    //IF PACKET OUT OF RENDER DISTANCE, KILL AND RESPAWN OR TELEPORT MAYBE CONSIDER MAKING A PASSENGER MIGHT WORK
    //THIS OBJECT NEEDS TO BE ADDED TO A MAP. WHEN IN USE, GET THE PLAYERDATA HASHMAP OF THE PLAYER UUID AND USE THE RESULTING METHODS
    //ALL VALUES NEED TO BE PER INSTANCE OF PLAYER
}
