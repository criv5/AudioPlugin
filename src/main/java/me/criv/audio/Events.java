package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import me.criv.audio.events.PlayerRegionEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
import static me.criv.audio.Main.*;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(location));
        lastRegion.put(player.getUniqueId(), getRegion(player));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(location));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(location));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(location));
    }

    @EventHandler
    public void onRegionChange(PlayerRegionEvent event) {
        Player player = event.getPlayer();
        String newRegion = event.getNewRegion();
        String oldRegion = event.getOldRegion();
        player.sendMessage("now entering " + newRegion + " land");
        BukkitRunnable runnable = new BukkitRunnable() {
            Packets packet = new Packets();
            final int oldTime = currentTrack;
            int time = 0;
            int height = 0;
            double increment = 0.5;
            @Override
            public void run() {
                packet.transitionHeight = height;
                packet.transitioning = true;
                packet.ascending = true;
                if(time < (packet.transitionTime/2)) {
                    height = time;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.teleportEntityPacket(player.getLocation()));
                    //player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    time++;
                }
                if(time == (packet.transitionTime/2)) {
                    packet.ascending = false;
                    player.stopSound(regionSoundMap.get(oldRegion)+currentTrack);
                    if(oldTime != currentTrack) {
                        time++;
                    }
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.teleportEntityPacket(player.getLocation()));
                    //player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                }
                if(time > (packet.transitionTime/2) && time <= (packet.transitionTime)) {
                    packet.ascending = false;
                    height = (Packets.transitionTime) - time;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.teleportEntityPacket(player.getLocation()));
                    //player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    time++;
                }
                if(time > (packet.transitionTime)) {
                    height = 0;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.teleportEntityPacket(player.getLocation()));
                    //player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    packet.transitioning = false;
                    packet.ascending = false;
                    cancel();
                }
            }
        };
             runnable.runTaskTimer(getInstance(), 0, 1);
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(message.equals("test")) {
            player.sendMessage("yep");
            player.sendMessage(lastRegion.toString());
        }
        if(message.contains("newmap")) {
            String[] spliced = message.split(" ");
            regionSoundMap.put(spliced[1], spliced[2]);
            for(Map.Entry<String, String> entry : regionSoundMap.entrySet()) {
                instance.getConfig().getConfigurationSection("region-sound-mappings").set(entry.getKey(), entry.getValue());
                instance.saveConfig();
            }
            player.sendMessage("added");
        }
        if(message.contains("seemap")) {
            player.sendMessage(regionSoundMap.toString());
        }
    }
}
