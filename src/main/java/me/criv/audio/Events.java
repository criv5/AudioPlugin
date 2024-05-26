package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import me.criv.audio.events.PlayerRegionEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.PacketPlayOutEntitySound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;

import static me.criv.audio.Main.*;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(location));
        player.sendMessage("armorstand should have summoned at u");
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
        player.sendMessage("now entering " + newRegion + " land");
        BukkitRunnable runnable = new BukkitRunnable() {
            final int oldTime = currentTrack;
            int time = 0;
            int height = 0;
            @Override
            public void run() {
                Packets.transitionHeight = height;
                Packets.transitioning = true;
                if(time < (Packets.transitionTime/2)) {
                    height = time;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    time++;
                }
                if(time == (Packets.transitionTime/2)) {
                    player.stopSound(lastRegion.get(player.getUniqueId())+currentTrack);
                    if(oldTime != currentTrack) {
                        time++;
                    }
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                }
                if(time > (Packets.transitionTime/2) && time <= (Packets.transitionTime)) {
                    height = (Packets.transitionTime) - time;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    time++;
                }
                if(time > (Packets.transitionTime)) {
                    height = 0;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    Packets.transitioning = false;
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
            regionsounds.put(spliced[1], spliced[2]);
            for(Map.Entry<String, String> entry : regionsounds.entrySet()) {
                instance.getConfig().getConfigurationSection("region-sound-mappings").set(entry.getKey(), entry.getValue());
                instance.saveConfig();
            }
            player.sendMessage("added");
        }
        if(message.contains("seemap")) {
            player.sendMessage(regionsounds.toString());
        }
    }
}
