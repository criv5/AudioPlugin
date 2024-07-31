package me.criv.audio;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Packets {
    public static final int staticEntityID = -2147483648;
    public static final int secondaryEntityID = -2147483647;
    public static final int thirdEntityID = -2147483646;
    public static final int fourthEntityID = -2147483645;

    public static PacketContainer spawnEntityPacket(int entityID, Location location, EntityType entityType) {
        PacketContainer soundEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        soundEntity.getIntegers()
                .write(0, entityID);
        UUID entityUUID = UUID.randomUUID();
        soundEntity.getUUIDs()
                .write(0, entityUUID);
        soundEntity.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
        soundEntity.getEntityTypeModifier()
                .write(0, entityType);
        return soundEntity;
    }

    public static PacketContainer createEntityMetadata(int entityID, boolean visible) {
        final PacketContainer entityMetadata = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        entityMetadata.getIntegers().write(0, entityID);

        if(visible) {
            entityMetadata.getDataValueCollectionModifier().write(0, List.of(
                    new WrappedDataValue(23, WrappedDataWatcher.Registry.getBlockDataSerializer(false), WrappedBlockData.createData(Material.DIAMOND_BLOCK).getHandle())
                    //new WrappedDataValue(8, WrappedDataWatcher.Registry.get(Float.class), 2F),
                    //new WrappedDataValue(9, WrappedDataWatcher.Registry.get(Float.class), 3F)
            ));
        }
        if(!visible) {
            entityMetadata.getDataValueCollectionModifier().write(0, List.of(
                    new WrappedDataValue(23, WrappedDataWatcher.Registry.getBlockDataSerializer(false), WrappedBlockData.createData(Material.AIR).getHandle())
                    //new WrappedDataValue(8, WrappedDataWatcher.Registry.get(Float.class), 2F),
                    //new WrappedDataValue(9, WrappedDataWatcher.Registry.get(Float.class), 3F)
            ));
        }

        return entityMetadata;
    }

    public static PacketContainer teleportEntityPacket(int entityID, Player player, Location location) {
        Data playerData = Data.getPlayerData(player);
        double height = playerData.getHeight();
        PacketContainer entityTeleport = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        entityTeleport.getIntegers()
                .write(0, entityID);
        entityTeleport.getDoubles()
                .write(0, location.getX())
                .write(2, location.getZ());
        if(!playerData.getTransitioning()) {
            entityTeleport.getDoubles().write(1, location.getY());
        } else {
            entityTeleport.getDoubles().write(1, location.getY() + height);
        }
        return entityTeleport;
    }

    public static PacketContainer setPassenger(int vehicleID, int passengerID, boolean passenger) {
        PacketContainer setPassenger = new PacketContainer(PacketType.Play.Server.MOUNT);
        setPassenger.getIntegers()
                .write(0, vehicleID);
        if(passenger) setPassenger.getIntegerArrays().write(0, new int[]{passengerID});
        else setPassenger.getIntegerArrays().write(0, new int[]{});
        return setPassenger;
    }

    public static PacketContainer playEntitySoundPacket(int entityID, String sound, int number, float volume) {
        SoundEffect effect2 = SoundEffect.a(new MinecraftKey("minecraft", sound + number));
        Holder<SoundEffect> effectHolder = Holder.a(effect2);

        PacketContainer attachedSound = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        attachedSound.getSoundCategories()
                .write(0, EnumWrappers.SoundCategory.MASTER);
        attachedSound.getIntegers()
                .write(0, entityID);
        attachedSound.getFloat()
                .write(0, volume)
                .write(1, (float) Config.getPitch());
        attachedSound.getLongs()
                .write(0,0L);
        attachedSound.getHolders(MinecraftReflection.getSoundEffectClass(), new EquivalentConverter<Holder<SoundEffect>>() {
            @Override
            public Object getGeneric(Holder<SoundEffect> soundEffectHolder) {
                return effect2;
            }
            @Override
            public Holder<SoundEffect> getSpecific(Object o) {
                return null;
            }
            @Override
            public Class<Holder<SoundEffect>> getSpecificType() {
                return null;
            }
        }).write(0, effectHolder);
        return attachedSound;
    }

    public static PacketContainer destroyEntityPacket(int entityID) {
        PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getIntLists()
                .write(0, Collections.singletonList(entityID));
        return destroyEntity;
    }
}
