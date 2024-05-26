package me.criv.audio;

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

import java.util.*;

import static me.criv.audio.events.EventConstructor.lastRegion;

public class Main extends JavaPlugin implements Listener {
    EventConstructor eventConstructor = new EventConstructor();
    Events events = new Events();
    static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(eventConstructor, this);
        getServer().getPluginManager().registerEvents(events, this);
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

        //runs every 15 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(getRegion(player).equals("none")) return;
                }
            }
        }.runTaskTimer(this, 300, 0);
    }

    public String getRegion(Player player) {
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