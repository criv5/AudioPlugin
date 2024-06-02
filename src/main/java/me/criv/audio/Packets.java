package me.criv.audio;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Packets {
    public static final int staticEntityID = -2147483648;

    public static PacketContainer spawnEntityPacket(Location location) {
        PacketContainer soundEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        soundEntity.getIntegers()
                .write(0, staticEntityID);
        UUID entityUUID = UUID.randomUUID();
        soundEntity.getUUIDs()
                .write(0, entityUUID);
        soundEntity.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
        soundEntity.getEntityTypeModifier()
                .write(0, EntityType.ARMOR_STAND);
        return soundEntity;
    }

    public static PacketContainer createEntityMetadata(boolean debug) {
        final PacketContainer entityMetadata = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        entityMetadata.getIntegers().write(0, staticEntityID);

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        if (!debug) {
            byte entityFlags = 0x20; // 0x20 = invisible
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), entityFlags, true);
            byte standFlags = 0x10; // 0x10 = marker
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), standFlags, true);
        } else {
            byte entityFlags = 0x00;
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), entityFlags, true);
            byte standFlags = 0x00;
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), standFlags, true);
        }

        List<WrappedDataValue> wrappedDataValueList = dataWatcher.getWatchableObjects().stream()
                .filter(Objects::nonNull)
                .map(entry -> new WrappedDataValue(entry.getWatcherObject().getIndex(), entry.getWatcherObject().getSerializer(), entry.getRawValue()))
                .collect(Collectors.toList());

        entityMetadata.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        return entityMetadata;
    }
    public static PacketContainer teleportEntityPacket(Player player, Location location) {
        Data playerData = Data.getPlayerData(player);
        double height = playerData.getHeight();
        PacketContainer entityTeleport = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        entityTeleport.getIntegers()
                .write(0, staticEntityID);
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

    public static PacketContainer playEntitySoundPacket(String sound, int number) {
        SoundEffect effect2 = SoundEffect.a(new MinecraftKey("minecraft", sound + number));
        Holder<SoundEffect> effectHolder = Holder.a(effect2);

        PacketContainer attachedSound = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        attachedSound.getSoundCategories()
                .write(0, EnumWrappers.SoundCategory.MASTER);
        attachedSound.getIntegers()
                .write(0, staticEntityID);
        attachedSound.getFloat()
                .write(0, 1F)
                .write(1, 1F);
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

    public static PacketContainer destroyEntityPacket() {
        PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getIntLists()
                .write(0, Collections.singletonList(staticEntityID));
        return destroyEntity;
    }
}
