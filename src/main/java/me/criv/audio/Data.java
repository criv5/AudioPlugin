package me.criv.audio;

import org.bukkit.entity.Player;

public class Data {
    private Player player;
    private boolean transitioning;
    private double transheight;

    public Data(Player player) {
        this.player = player;
        this.transitioning = false;
        this.transheight = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public double getTransheight() {
        return transheight;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setTransitioning(Boolean transitioning) {
        this.transitioning = transitioning;
    }

    public void setTransheight(double transheight) {
        this.transheight = transheight;
    }

    //IF PACKET OUT OF RENDER DISTANCE, KILL AND RESPAWN OR TELEPORT MAYBE CONSIDER MAKING A PASSENGER MIGHT WORK
    //THIS OBJECT NEEDS TO BE ADDED TO A MAP. WHEN IN USE, GET THE PLAYERDATA HASHMAP OF THE PLAYER UUID AND USE THE RESULTING METHODS
    //ALL VALUES NEED TO BE PER INSTANCE OF PLAYER
}
