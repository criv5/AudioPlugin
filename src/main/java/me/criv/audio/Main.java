package me.criv.audio;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.criv.audio.events.EventConstructor;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Main extends JavaPlugin implements Listener {
    public static final Map<Player, Map<PacketContainer, Integer>> standMap = new HashMap<>();
    public static final Map<Player, Tuple<PacketContainer, Integer>> twoValues = new HashMap<>();
    public static final Map<UUID, String> lastRegion = new HashMap<>();
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
        Bukkit.getConsoleSender().sendMessage("§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n§a AUDIOPLUGIN ENABLED \n");
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<UUID> uuids = lastRegion.keySet();
                for (UUID uuid : uuids) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        lastRegion.remove(uuid);
/*
                        Set<PacketContainer> packetMap = standMap.get(player).keySet();
                        int index = 0;
                        int[] packetArray = new int[packetMap.size()];
                        for (PacketContainer packet : packetMap) {
                            packetArray[index++] = packet.getIntegers().read(0);
                        }
                        PacketContainer removeEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
                        removeEntity.getIntegers()
                                        .write(0,1);
                        removeEntity.getIntegerArrays()
                                        .write(0, packetArray);
                        standMap.remove(player);
 */
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PacketContainer entityTeleport = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
                    entityTeleport.getIntegers()
                            .write(0, standMap.get(player).keySet().iterator().next().getIntegers().read(0));
                    entityTeleport.getDoubles()
                            .write(0,player.getLocation().getX())
                            .write(1, player.getLocation().getY() + standMap.get(player).get(standMap.get(player).keySet().iterator().next()))
                            .write(2, player.getLocation().getZ());
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, entityTeleport);
                }
            }
        }.runTaskTimer(this, 0, 0);

        //runs every 15 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(getRegion(player) == "none") return;
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

    public String getTrackNumber(String track) {
        //region = poop
        //total of 10- 10sec track
        //poop1, poop2, poop3, poop-10
        //string + int
        return "soonTM";
    }

    @Override
    public void onDisable() {
    }
}

//take into account players ping