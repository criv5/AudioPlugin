package me.criv.audio;

import com.comphenix.protocol.ProtocolLibrary;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.criv.audio.events.EventConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static me.criv.audio.Data.State.*;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Main extends JavaPlugin implements Listener {
    static int trackIncrement = 0;
    static Main instance;
    EventConstructor eventConstructor = new EventConstructor();
    Events events = new Events();
    Commands commands = new Commands();

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getCommand("regionsound").setExecutor(commands);
        getCommand("rs").setExecutor(commands);
        getCommand("rsm").setExecutor(commands);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(eventConstructor, this);
        getServer().getPluginManager().registerEvents(events, this);
        Config.createDefaults();
        Bukkit.getConsoleSender().sendMessage("§aAUDIOPLUGIN ENABLED");
        for(Player player : Bukkit.getOnlinePlayers()) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(player.getLocation()));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                trackIncrement++;
                for(Player player : Bukkit.getOnlinePlayers()) {
                    String region = getRegion(player);
                    if(Data.getPlayerData(player).getState() == FADEOUT) region = lastRegion.get(player.getUniqueId());
                    if(!getConfig().getConfigurationSection("region-sound-mappings").isConfigurationSection(region)) return;

                    int max = Config.getMax(region);
                    if(max == 0) max = 1;
                    int divider = trackIncrement/max;
                    int currentTrack = trackIncrement-(divider*max)+1;

                    if(Data.getPlayerData(player).getDebug()) player.sendMessage("§asound: " +Config.getSound(region) + " max: " + Config.getMax(region) + " current: " + currentTrack + " total: " + trackIncrement);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.playEntitySoundPacket(Config.getSound(region), currentTrack));
                }
            }
        }.runTaskTimer(this, 0, Config.getSyncInterval()*20L);
    }

    public static String getRegion(Player player) {
        Location location = player.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet locationRegions = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return locationRegions.getRegions().isEmpty() ? "none" : locationRegions.getRegions().iterator().next().getId();
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.destroyEntityPacket());
            Bukkit.getConsoleSender().sendMessage("§aAUDIOPLUGIN DISABLED");
        }
    }
}