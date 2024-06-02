package me.criv.audio.events;

import com.comphenix.protocol.ProtocolLibrary;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.criv.audio.Data;
import me.criv.audio.Packets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.criv.audio.Main.getRegion;

public class EventConstructor implements Listener {
    private static final Map<UUID, String> regionMemory = new HashMap<>();
    public static Map<UUID, String> lastRegion = new HashMap<>();
    @EventHandler
    public void regionChange(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet locationRegions = query.getApplicableRegions(BukkitAdapter.adapt(location));

        String lastRegionId = regionMemory.getOrDefault(player.getUniqueId(), "none");
        String currentRegionId = locationRegions.getRegions().isEmpty() ? "none" : locationRegions.getRegions().iterator().next().getId();

        if (!currentRegionId.equals(lastRegionId)) {
            PlayerRegionEvent playerRegionEvent = new PlayerRegionEvent(player, lastRegionId, currentRegionId);
            lastRegion.put(player.getUniqueId(), lastRegionId);
            Bukkit.getServer().getPluginManager().callEvent(playerRegionEvent);
            regionMemory.put(player.getUniqueId(), currentRegionId);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        regionMemory.remove(player.getUniqueId());
        lastRegion.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        regionMemory.put(player.getUniqueId(), getRegion(player));
        lastRegion.put(player.getUniqueId(), getRegion(player));
    }
}
