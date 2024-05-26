package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import me.criv.audio.events.PlayerRegionEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

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
        Sound sound = Sound.MUSIC_DISC_RELIC; //relic i think
        if(newRegion.equals("none"))
            sound = Sound.MUSIC_DISC_CHIRP; //chirp
        if(newRegion.equals("fart"))
            sound = Sound.MUSIC_DISC_STAL; //stal
        if(newRegion.equals("poop10"))
            sound = Sound.MUSIC_DISC_CAT;
        Sound finalSound = sound;
        BukkitRunnable runnable = new BukkitRunnable() {
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
                }
                if(time == (Packets.transitionTime/2)) {
                    player.stopAllSounds();
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.playEntitySoundPacket(finalSound));
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                }
                if(time > (Packets.transitionTime/2) && time <= (Packets.transitionTime)) {
                    height = (Packets.transitionTime) - time;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                }
                if(time > (Packets.transitionTime)) {
                    height = 0;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player.getLocation()));
                    player.sendMessage("transitioning bool = " + Packets.transitioning + " and time is " + time + " and distance should be " + Packets.transitionHeight);
                    Packets.transitioning = false;
                    cancel();
                }
                time++;
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
    }
}
