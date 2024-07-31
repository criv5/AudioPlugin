package me.criv.audio;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Data {
    private static final HashMap<UUID, Data> playerData = new HashMap<>();

    private Player player;
    private boolean transitioning;
    private double height;
    private State state;
    private boolean debug;
    private float volume;
    enum State {
        INACTIVE,
        FADEIN,
        SWITCH,
        FADEOUT
    }

    public Data(Player player) {
        this.player = player;
        this.transitioning = false;
        this.height = 0;
        this.state = State.INACTIVE;
        this.debug = false;
        this.volume = 1;
        playerData.put(player.getUniqueId(), this);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean getTransitioning() {
        return transitioning;
    }

    public double getHeight() {
        return height;
    }

    public State getState() {
        return state;
    }

    public boolean getDebug() {
        return debug;
    }

    public float getVolume() {
        return volume;
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

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public static Data getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public static void deletePlayerData(Player player) {
        playerData.remove(player.getUniqueId());
    }

    public static void createPlayerData(Player player) {
        new Data(player);
    }
}
