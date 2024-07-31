package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import static me.criv.audio.Main.instance;
import static me.criv.audio.Packets.*;

@SuppressWarnings("NullableProblems")
public class Commands implements Listener, CommandExecutor {
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
                    player.sendMessage("§bError: max must be an integer");
                    return false;
                }
                Config.addRegionObject(region, sound, max);
                player.sendMessage("§bAdded new region §c" + region + " §bwith sound §c" + sound + " §band max track number of §c" + max);
                return true;
            }

            if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
                player.sendMessage("""
                        §9§LRegionAudio Commands: (/rs, /ra, /rsm)\s
                        §cadd <region> <sound> <# of tracks>
                        §bAdds a regionaudio object with region, sound name, and maximum track number
                        §cremove <region>
                        §bRemoves a regionaudio object
                        §clist
                        §bLists regionaudio objects
                        §cdebug <boolean>
                        §bToggles debug mode
                        §ctime <seconds>
                        §bSets the fade time when switching regions
                        §cheight <blocks>
                        §bSets the fade height when switching regions
                        §cpitch <0.5-2.0>
                        §bSets the pitch of audio tracks
                        §cvolume <0-1.0>
                        §bSets the volume of audio tracks
                        §ckill <player>
                        §bKills the audio entity""");
                return true;
            }

            if(args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                Config.removeRegionObject(args[1]);
                player.sendMessage("§bRemoved region §c" + args[1]);
                return true;
            }

            if(args.length == 1 && args[0].equalsIgnoreCase("list")) {
                player.sendMessage("§bList of regions and their properties: §c" + Config.listRegionObjects());
                return true;
            }

            if(args.length == 2 && args[0].equalsIgnoreCase("height")) {
                double height;

                try {
                    height = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§bError: height must be a double");
                    return false;
                }
                height = Math.round(height * 10.0) / 10.0;
                Config.setFadeHeight(height);
                player.sendMessage("§bSet fade height to §c" + Config.getFadeHeight());
                return true;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("time")) {
                double time;

                try {
                    time = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§bError: time must be a double");
                    return false;
                }
                time = Math.round(time * 10.0) / 10.0;
                Config.setFadeTime(time);
                player.sendMessage("§bSet fade time to §c" + Config.getFadeTime());
                return true;
            }
            if(args[0].equalsIgnoreCase("debug")) {
                boolean debug = Data.getPlayerData(player).getDebug();

                if(args.length < 2) {
                    debug = !debug;
                } else if(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                    debug = Boolean.parseBoolean(args[1].toLowerCase());
                }
                else debug = !debug;

                Data.getPlayerData(player).setDebug(debug);
                if(debug) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(staticEntityID, true));
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(secondaryEntityID, true));
                }
                else {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(staticEntityID, false));
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(secondaryEntityID, false));
                }
                player.sendMessage("§bDebug mode: §c" + debug);
                return true;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("pitch")) {
                double pitch;
                try {
                    pitch = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§bError: pitch must be a double");
                    return false;
                }
                double interval = 5/pitch;
                if(pitch < 0.5) pitch = 0.5;
                if(pitch > 2) pitch = 2;
                Config.setSyncInterval(interval);
                Config.setPitch(pitch);
                instance.startTrackScheduler();
                player.stopAllSounds();
                player.sendMessage("§bSet pitch to " + pitch + " and sync interval to " + interval);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("kill")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§bError: player not found");
                    return false;
                }

                ProtocolLibrary.getProtocolManager().sendServerPacket(target, Packets.destroyEntityPacket(staticEntityID));
                ProtocolLibrary.getProtocolManager().sendServerPacket(target, Packets.destroyEntityPacket(secondaryEntityID));
                ProtocolLibrary.getProtocolManager().sendServerPacket(target, Packets.destroyEntityPacket(thirdEntityID));
                ProtocolLibrary.getProtocolManager().sendServerPacket(target, Packets.destroyEntityPacket(fourthEntityID));
                player.sendMessage("§bKilled all audio entities for " + args[1]);
                return true;
            }

            if(args.length == 2 && args[0].equalsIgnoreCase("volume")) {
                float volume;
                try {
                    volume = Float.parseFloat(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§bError: volume must be a float");
                    return false;
                }
                if(volume > 1) volume = 1;
                if(volume < 0) volume = 0;
                player.sendMessage("§bSet volume to " + volume);
                Data.getPlayerData(player).setVolume(volume);
                return true;
            }
        }
        return false;
    }
}
