package me.criv.audio;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import java.util.UUID;
public class Packets {
    public static final int staticEntityID = -2147483648;
    public static boolean transitioning = false;
    public static int transitionHeight = 0;
    public static int transitionTime = 40;
    public static PacketContainer spawnEntityPacket(Location location) {
        PacketContainer soundEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        soundEntity.getIntegers()
                .write(0, staticEntityID);
        UUID entityUUID = UUID.randomUUID();
        soundEntity.getUUIDs()
                .write(0, entityUUID);
        soundEntity.getDoubles()
                .write(0,location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
        soundEntity.getEntityTypeModifier()
                .write(0,EntityType.ARMOR_STAND);
        return soundEntity;
            }
    public static PacketContainer teleportEntityPacket(Location location) {
        PacketContainer entityTeleport = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        entityTeleport.getIntegers()
                .write(0, staticEntityID);
        entityTeleport.getDoubles()
                .write(0, location.getX())
                .write(2, location.getZ());
        if(!transitioning) {
            entityTeleport.getDoubles().write(1, location.getY());
        } else {
            entityTeleport.getDoubles().write(1, location.getY() + transitionHeight);
        }
        return entityTeleport;
    }

    public static PacketContainer playEntitySoundPacket(Sound sound) {
        PacketContainer attachedSound = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        attachedSound.getSoundEffects()
                .write(0, sound);
        attachedSound.getSoundCategories()
                .write(0, EnumWrappers.SoundCategory.MASTER);
        attachedSound.getIntegers()
                .write(0, staticEntityID);
        attachedSound.getFloat()
                .write(0, 1F)
                .write(1, 1F);
        attachedSound.getLongs()
                .write(0,0L);
        return attachedSound;
    }
}