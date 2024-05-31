package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import me.criv.audio.events.PlayerRegionEvent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import static me.criv.audio.Data.State.*;
import static me.criv.audio.Data.playerData;
import static me.criv.audio.Main.*;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Events implements Listener, CommandExecutor {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(location));
        lastRegion.put(player.getUniqueId(), getRegion(player));
        playerData.put(player.getUniqueId(), new Data(player));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player, location));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player, location));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player, location));
    }

    @EventHandler
    public void onRegionChange(PlayerRegionEvent event) {
        Player player = event.getPlayer();
        String newRegion = event.getNewRegion();
        String oldRegion = event.getOldRegion();
        player.sendMessage("now entering " + newRegion + " land");
        Data.getPlayerData(player.getUniqueId()).setTransitioning(true);
        BukkitRunnable runnable = new BukkitRunnable() {
            final int lastIncrement = trackIncrement;
            int time = 0;
            double height = 0;
            @Override
            public void run() {
                if(time > Config.getFadeTime()) {
                    //Data.getPlayerData(player.getUniqueId()).setHeight(0);
                    Data.getPlayerData(player.getUniqueId()).setState(INACTIVE);
                    Data.getPlayerData(player.getUniqueId()).setTransitioning(false);
                    cancel();
                }
                if(time < Config.getFadeTime()/2) {
                    Data.getPlayerData(player.getUniqueId()).setState(ASCENDING);
                    height = height+Config.getFadeHeight()/(Config.getFadeTime()/2);
                    Data.getPlayerData(player.getUniqueId()).setHeight(height);
                    time++;
                }
                if(time == Config.getFadeTime()/2) {
                    Data.getPlayerData(player.getUniqueId()).setState(SWITCH);
                    if(trackIncrement != lastIncrement) {
                        int lastIncrement = trackIncrement-1;
                        int max = Config.getMax(Config.getSound(lastRegion.get(player.getUniqueId())));
                        if(max == 0) max = 1;
                        int divider = (lastIncrement/max);
                        int lastTrack = lastIncrement-(divider*max);
                        Data.getPlayerData(player.getUniqueId()).setHeight(Config.getFadeHeight());
                        player.stopSound(Config.getSound(lastRegion.get(player.getUniqueId()))+lastTrack);
                        time++;
                    }
                }
                if(time > Config.getFadeTime()/2) {
                    Data.getPlayerData(player.getUniqueId()).setState(DESCENDING);
                    height = height-Config.getFadeHeight()/(Config.getFadeTime()/2);
                    Data.getPlayerData(player.getUniqueId()).setHeight(height);
                    time++;
                }
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player, player.getLocation()));
            }
        };
             runnable.runTaskTimer(getInstance(), 0, 1);
    }

    @Override
    public boolean onCommand(CommandSender player, Command message, String label, String[] args) {
        if(message.getName().equalsIgnoreCase("regionsound") || message.getName().equalsIgnoreCase("rsm") || message.getName().equalsIgnoreCase("rs")) {
            if(args.length == 4 && args[0].equalsIgnoreCase("add")) {
                String region = args[1];
                String sound = args[2];
                int max;

                try {
                    max = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage("max must be an integer");
                    return false;
                }
                Config.addRegionObject(region, sound, max);
                player.sendMessage("§3Added new region §c" + region + " §3with sound §c" + sound + " §3and max track number of §c" + max);
                return true;
            }

            if(args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                Config.removeRegionObject(args[1]);
                player.sendMessage("§3Removed region §c" + args[1]);
                return true;
            }

            if(args.length == 1 && args[0].equalsIgnoreCase("list")) {
                player.sendMessage("§3List of regions and their properties: §c" + Config.listRegionObjects());
                return true;
            }

            if(args.length == 2 && args[0].equalsIgnoreCase("height")) {
                int height;

                try {
                    height = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("height must be an integer");
                    return false;
                }
                Config.setFadeHeight(height);
                player.sendMessage("§3Set maximum fade height to §c" + Config.getFadeHeight());
                return true;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("time")) {
                int time;

                try {
                    time = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("time must be an integer");
                    return false;
                }
                Config.setFadeTime(time);
                player.sendMessage("§3Set fade time to §c" + Config.getFadeTime());
                return true;
            }
        }
        return false;
    }
}
