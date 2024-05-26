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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static me.criv.audio.events.EventConstructor.lastRegion;

public class Main extends JavaPlugin implements Listener {
    static int currentTrack = 0;
    static HashMap<String, String> regionSoundMap = new HashMap<>();
    static Main instance;
    EventConstructor eventConstructor = new EventConstructor();
    Events events = new Events();
    FileConfiguration config = getConfig();

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(eventConstructor, this);
        getServer().getPluginManager().registerEvents(events, this);
        if(!config.isConfigurationSection("region-sound-mappings")) {
            config.createSection("region-sound-mappings");
            saveConfig();
        }
        getSoundMappings();
        saveConfig();
        Bukkit.getConsoleSender().sendMessage("§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED");
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<UUID> uuids = lastRegion.keySet();
                for (UUID uuid : uuids) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        lastRegion.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(this, 0, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                currentTrack++;
                if(currentTrack > 24) {
                    currentTrack = 0;
                }
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("we just changed tracks " + currentTrack);
                    if(regionSoundMap.containsKey(getRegion(player))) {
                        if(Packets.ascending) return;
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, Packets.playEntitySoundPacket(regionSoundMap.get(getRegion(player))));
                    }
                }
            }
        }.runTaskTimer(this, 0, 100);
    }

    public void getSoundMappings() {
        ConfigurationSection section = getConfig().getConfigurationSection("region-sound-mappings");
        Set<String> keys = section.getKeys(false);
        for(String key : keys) {
            String value = section.getString(key);
            regionSoundMap.put(key,value);
        }
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
    }
}