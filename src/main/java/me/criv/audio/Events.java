package me.criv.audio;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.criv.audio.events.PlayerRegionEvent;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.criv.audio.Main.*;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            PacketContainer soundEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        Map<PacketContainer, Integer> packetMap = new HashMap<>();
            soundEntity.getIntegers()
                    .write(0, (int)(Math.random() * 10000000) + 1);
            UUID entityUUID = UUID.randomUUID();
            soundEntity.getUUIDs()
                    .write(0, entityUUID);
            soundEntity.getDoubles()
                    .write(0,player.getLocation().getX())
                    .write(1, player.getLocation().getY())
                    .write(2, player.getLocation().getZ());
            soundEntity.getEntityTypeModifier()
                    .write(0, EntityType.ARMOR_STAND);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, soundEntity);
        player.sendMessage("armorstand should have summoned at u");
            //PacketContainer regionEntitySound = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        packetMap.put(soundEntity, 0);
        standMap.put(player, packetMap);
    }

    @EventHandler
    public void regionChangeEvent(PlayerRegionEvent event) {
        Player player = event.getPlayer();
        String oldRegion = event.getOldRegion();
        String newRegion = event.getNewRegion();
        player.sendMessage("now entering " + newRegion + " land");
        Sound sound = Sound.MUSIC_DISC_RELIC; //relic i think
        if(newRegion.equals("none"))
            sound = Sound.MUSIC_DISC_CHIRP; //chirp
        if(newRegion.equals("fart"))
            sound = Sound.MUSIC_DISC_STAL; //stal
        if(newRegion.equals("poop10"))
            sound = Sound.MUSIC_DISC_CAT;
        Sound finalSound = sound;
        BukkitRunnable b = new BukkitRunnable() {
            int distance = 1;
            Map<PacketContainer, Integer> packetMap = standMap.get(player);
            @Override
            public void run() {
                distance++;
                if(distance < 25) {
                    packetMap.put(standMap.get(player).keySet().iterator().next(), distance);
                    player.sendMessage(String.valueOf(distance));
                }
                if(distance == 25) {
                    player.stopAllSounds();
                    PacketContainer attachedSound = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
                    attachedSound.getSoundEffects()
                            .write(0, finalSound);
                    attachedSound.getSoundCategories()
                            .write(0, EnumWrappers.SoundCategory.MASTER);
                    attachedSound.getIntegers()
                            .write(0, standMap.get(player).keySet().iterator().next().getIntegers().read(0));
                    attachedSound.getFloat()
                            .write(0, 1F)
                            .write(1, 1F);
                    attachedSound.getLongs()
                            .write(0,0L);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, attachedSound);
                    player.sendMessage(String.valueOf(distance));
                }
                if(distance > 25) {
                    packetMap.put(standMap.get(player).keySet().iterator().next(), 25-(distance/2));
                    player.sendMessage(String.valueOf(distance));
                }
                if(distance >= 50) {
                    packetMap.put(standMap.get(player).keySet().iterator().next(), 0);
                    player.sendMessage(String.valueOf(distance));
                    cancel();
                }
            }
        };
             b.runTaskTimer(getInstance(), 0, 1);
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(message.equals("test")) {
            player.sendMessage("yep");
            player.sendMessage(lastRegion.toString());
            player.sendMessage(standMap.toString());
        }
    }
}
