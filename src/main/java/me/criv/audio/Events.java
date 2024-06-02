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
import static me.criv.audio.Main.*;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Events implements Listener, CommandExecutor {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Data.createPlayerData(player);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(location));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(false));
    }

    @EventHandler void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.destroyEntityPacket());
        Data.deletePlayerData(player);
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
        if(Data.getPlayerData(player).getDebug()) player.sendMessage("§aold: " + oldRegion + " new: " + newRegion);
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
                    if(Data.getPlayerData(player).getDebug())player.sendMessage("§a" + playerData.getState());
                } else if (time > fadeTime && playerData.getState() == SWITCH) {
                    playerData.setState(FADEIN);
                    if(Data.getPlayerData(player).getDebug())player.sendMessage("§a" + playerData.getState());
                } else if (time == fadeTime && playerData.getState() == FADEOUT) {
                    playerData.setState(SWITCH);
                    if(Data.getPlayerData(player).getDebug())player.sendMessage("§a" + playerData.getState());
                    lastIncrement = trackIncrement;
                } else if (time < 1 && playerData.getState() != SWITCH) {
                    playerData.setState(FADEOUT);
                    if(Data.getPlayerData(player).getDebug())player.sendMessage("§a" + playerData.getState());
                }

                if(time > fadeTime && playerData.getState() == FADEOUT) cancel();
                if(time != fadeTime && playerData.getState() == SWITCH) cancel();

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
                            int lastTrack = adjustedLastIncrement - (divider * max);
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
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.teleportEntityPacket(player, player.getLocation()));
            }
        };
        runnable.runTaskTimer(getInstance(), 0, 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command message, String label, String[] args) {
        if(message.getName().equalsIgnoreCase("regionsound") || message.getName().equalsIgnoreCase("ra") || message.getName().equalsIgnoreCase("rs") || message.getName().equalsIgnoreCase("regionaudio")) {
            Player player = (Player) sender;
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

            if(args[0].equalsIgnoreCase("help")) {
                player.sendMessage("§3RegionAudio Commands: (/rs, /ra, /rsm) " +
                        "\n§cadd <region> <sound> <# of tracks>\n§3Adds a regionaudio object with region, sound name, and maximum track number" +
                        "\n§cremove <region>\n§3Removes a regionaudio object" +
                        "\n§clist\n§3Lists regionaudio objects" +
                        "\n§cdebug <boolean>\n§3Toggle debug mode" +
                        "\n§ctime <seconds>\n§3Sets the fade time when switching regions" +
                        "\n§cheight <blocks>\n§3Sets the fade height when switching regions");
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
                double height;

                try {
                    height = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("height must be a double");
                    return false;
                }
                height = Math.round(height * 10.0) / 10.0;
                Config.setFadeHeight(height);
                player.sendMessage("§3Set fade height to §c" + Config.getFadeHeight());
                return true;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("time")) {
                double time;

                try {
                    time = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("time must be a double");
                    return false;
                }
                time = Math.round(time * 10.0) / 10.0;
                Config.setFadeTime(time);
                player.sendMessage("§3Set fade time to §c" + Config.getFadeTime());
                return true;
            }
            if(args[0].equalsIgnoreCase("debug")) {
                boolean debug = Data.getPlayerData(player).getDebug();
                debug = !debug;
                Data.getPlayerData(player).setDebug(debug);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(debug));
                player.sendMessage("§3Debug mode: §c" + debug);
                return true;
            }
        }
        return false;
    }
}
