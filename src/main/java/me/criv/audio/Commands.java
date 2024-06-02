package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
                    player.sendMessage("max must be an integer");
                    return false;
                }
                Config.addRegionObject(region, sound, max);
                player.sendMessage("§bAdded new region §c" + region + " §bwith sound §c" + sound + " §band max track number of §c" + max);
                return true;
            }

            if(args[0].equalsIgnoreCase("help")) {
                player.sendMessage("""
                        §bRegionAudio Commands: (/rs, /ra, /rsm)\s
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
                        §bSets the fade height when switching regions""");
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
                    player.sendMessage("height must be a double");
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
                    player.sendMessage("time must be a double");
                    return false;
                }
                time = Math.round(time * 10.0) / 10.0;
                Config.setFadeTime(time);
                player.sendMessage("§bSet fade time to §c" + Config.getFadeTime());
                return true;
            }
            if(args[0].equalsIgnoreCase("debug")) {
                boolean debug = Data.getPlayerData(player).getDebug();
                debug = !debug;
                Data.getPlayerData(player).setDebug(debug);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.createEntityMetadata(debug));
                player.sendMessage("§bDebug mode: §c" + debug);
                return true;
            }
        }
        return false;
    }
}
