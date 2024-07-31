package me.criv.audio;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.criv.audio.events.EventConstructor;
import me.criv.audio.events.PlayerRegionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import static me.criv.audio.Data.State.*;
import static me.criv.audio.Main.*;
import static me.criv.audio.Packets.*;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Events implements Listener {

    public final Vector offset = new Vector(0, 5, 0);

    private final Plugin plugin;
    public Events(Plugin plugin) {
        this.plugin = plugin;
    }

    public void packetTeleportListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.TELEPORT_ACCEPT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.destroyEntityPacket(staticEntityID));

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(staticEntityID, player.getLocation().add(offset), EntityType.BLOCK_DISPLAY));
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(staticEntityID, Data.getPlayerData(player).getDebug()));
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Data.createPlayerData(player);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(staticEntityID, location, EntityType.BLOCK_DISPLAY));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(staticEntityID, false));
    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.destroyEntityPacket(staticEntityID));
        Data.deletePlayerData(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(staticEntityID, player, location.add(offset)));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(staticEntityID, player, location.add(offset)));
    }

    @EventHandler
    public void onRegionChange(PlayerRegionEvent event) {
        Player player = event.getPlayer();
        String newRegion = event.getNewRegion();
        String oldRegion = event.getOldRegion();
        if (Data.getPlayerData(player).getDebug()) player.sendMessage("§aold: " + oldRegion + " new: " + newRegion);
        Data.getPlayerData(player).setTransitioning(true);
        BukkitRunnable runnable = new BukkitRunnable() {
            final double fadeTime = Config.getFadeTime();
            final double fadeHeight = Config.getFadeHeight();
            int lastIncrement;
            int time = 0;
            double height = 0;

            @Override
            public void run() {
                Data playerData = Data.getPlayerData(player);

                if (time > fadeTime * 2 && playerData.getState() == FADEIN) {
                    playerData.setState(INACTIVE);
                    if (playerData.getDebug()) player.sendMessage("§a" + playerData.getState());
                } else if (time > fadeTime && playerData.getState() == SWITCH) {
                    playerData.setState(FADEIN);
                    if (playerData.getDebug()) player.sendMessage("§a" + playerData.getState());
                } else if (time == fadeTime && playerData.getState() == FADEOUT) {
                    playerData.setState(SWITCH);
                    if (playerData.getDebug()) player.sendMessage("§a" + playerData.getState());
                    lastIncrement = trackIncrement;
                } else if (time < 1 && playerData.getState() != SWITCH) {
                    playerData.setState(FADEOUT);
                    if (playerData.getDebug()) player.sendMessage("§a" + playerData.getState());
                }

                if (time > fadeTime && playerData.getState() == FADEOUT) cancel();
                if (time != fadeTime && playerData.getState() == SWITCH) cancel();

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(staticEntityID, player, player.getLocation().add(offset)));
                switch (playerData.getState()) {
                    case INACTIVE -> {
                        playerData.setTransitioning(false);
                        cancel();
                    }
                    case FADEOUT -> {
                        height += fadeHeight / fadeTime;
                        if (height >= playerData.getHeight()) {
                            playerData.setHeight(height);
                        }
                        time++;
                    }
                    case SWITCH -> {
                        if (trackIncrement != lastIncrement) {
                            int adjustedLastIncrement = trackIncrement - 1;
                            int max = Config.getMax(Config.getSound(lastRegion.get(player.getUniqueId())));
                            if (max == 0) max = 1;
                            int divider = adjustedLastIncrement / max;
                            int lastTrack = adjustedLastIncrement - (divider * max) + 1;
                            playerData.setHeight(fadeHeight);
                            player.stopSound(Config.getSound(lastRegion.get(player.getUniqueId())) + lastTrack);
                            time++;
                        }
                    }
                    case FADEIN -> {
                        height -= fadeHeight / fadeTime;
                        if (height < 0) height = 0;
                        playerData.setHeight(height);
                        time++;
                    }
                }
            }
        };
        runnable.runTaskTimer(getInstance(), 0, 1);
    }
}
