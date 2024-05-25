package me.criv.audio.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.criv.audio.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventConstructor implements Listener {
    @EventHandler
    public void regionChange(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet locationRegions = query.getApplicableRegions(BukkitAdapter.adapt(location));

        String lastRegionId = Main.lastRegion.getOrDefault(player.getUniqueId(), "none");
        String currentRegionId = locationRegions.getRegions().isEmpty() ? "none" : locationRegions.getRegions().iterator().next().getId();

        if (!currentRegionId.equals(lastRegionId)) {
            PlayerRegionEvent playerRegionEvent = new PlayerRegionEvent(player, lastRegionId, currentRegionId);
            Bukkit.getServer().getPluginManager().callEvent(playerRegionEvent);
            Main.lastRegion.put(player.getUniqueId(), currentRegionId);
        }
    }
}
