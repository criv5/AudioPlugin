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

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static me.criv.audio.Data.State.*;
import static me.criv.audio.Data.playerData;
import static me.criv.audio.events.EventConstructor.lastRegion;

public class Main extends JavaPlugin implements Listener {
    static int trackIncrement = 0;
    static Main instance;
    EventConstructor eventConstructor = new EventConstructor();
    Events events = new Events();

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getCommand("regionsound").setExecutor(events);
        getCommand("rs").setExecutor(events);
        getCommand("rsm").setExecutor(events);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(eventConstructor, this);
        getServer().getPluginManager().registerEvents(events, this);
        Config.createDefaults();
        saveConfig();
        Bukkit.getConsoleSender().sendMessage("§a AUDIOPLUGIN ENABLED");
        for(Player player : Bukkit.getOnlinePlayers()) {
            //ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.destroyEntityPacket());
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.spawnEntityPacket(player.getLocation()));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<UUID> uuids = lastRegion.keySet();
                for (UUID uuid : uuids) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        lastRegion.remove(uuid);
                        playerData.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(this, 0, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                trackIncrement++;
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("we just changed tracks " + trackIncrement);
                    if (getConfig().getConfigurationSection("region-sound-mappings").isConfigurationSection(getRegion(player)) && Data.getPlayerData(player).getState() != FADEOUT) {
                        int max = Config.getMax(getRegion(player));
                        if(max == 0) max = 1;
                        int divider = (trackIncrement/max);
                        int currentTrack = (trackIncrement-(divider*max))+1;
                        player.sendMessage("sound: " +Config.getSound(getRegion(player)) + " and max is " + Config.getMax(getRegion(player)) + " and current track number should be " + currentTrack);
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.playEntitySoundPacket(Config.getSound(getRegion(player)), currentTrack));
                    }
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
        }
    }
}