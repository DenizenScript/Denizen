package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_20.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_20_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R4.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftSkull;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.tag.CraftBlockTag;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftLocation;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftMagicNumbers;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BlockHelperImpl implements BlockHelper {

    public static final Field craftBlockEntityState_tileEntity = ReflectionHelper.getFields(CraftBlockEntityState.class).get("tileEntity");
    public static final Field craftBlockEntityState_snapshot = ReflectionHelper.getFields(CraftBlockEntityState.class).get("snapshot");
    public static final Field craftSkull_profile = ReflectionHelper.getFields(CraftSkull.class).get("profile");

    @Override
    public void makeBlockStateRaw(BlockState state) {
        try {
            craftBlockEntityState_snapshot.set(state, craftBlockEntityState_tileEntity.get(state));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void applyPhysics(Location location) {
        ((CraftWorld) location.getWorld()).getHandle().updateNeighborsAt(CraftLocation.toBlockPosition(location), CraftMagicNumbers.getBlock(location.getBlock().getType()));
    }

    public static <T extends BlockEntity> T getTE(CraftBlockEntityState<T> cbs) {
        try {
            return (T) craftBlockEntityState_tileEntity.get(cbs);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
        return null;
    }

    @Override
    public PlayerProfile getPlayerProfile(Skull skull) {
        // TODO: 1.20.6: Seems to be a holder for data that can make the request to complete it later - do we want to do that here?
        ResolvableProfile profile = getTE(((CraftSkull) skull)).owner;
        if (profile == null) {
            return null;
        }
        com.mojang.authlib.properties.Property property = Iterables.getFirst(profile.properties().get("textures"), null);
        return new PlayerProfile(profile.name().orElse(null), profile.id().orElse(null), property != null ? property.value() : null);
    }

    @Override
    public void setPlayerProfile(Skull skull, PlayerProfile playerProfile) {
        GameProfile gameProfile = ProfileEditorImpl.getGameProfile(playerProfile);
        try {
            craftSkull_profile.set(skull, gameProfile);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        skull.update();
    }

    @Override
    public CompoundTag getNbtData(Block block) {
        BlockEntity te = ((CraftWorld) block.getWorld()).getHandle().getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()), true);
        if (te != null) {
            net.minecraft.nbt.CompoundTag compound = te.saveWithFullMetadata(CraftRegistry.getMinecraftRegistry());
            return CompoundTagImpl.fromNMSTag(compound);
        }
        return null;
    }

    @Override
    public void setNbtData(Block block, CompoundTag ctag) {
        CompoundTagBuilder builder = ctag.createBuilder();
        builder.putInt("x", block.getX());
        builder.putInt("y", block.getY());
        builder.putInt("z", block.getZ());
        ctag = builder.build();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
        BlockEntity te = ((CraftWorld) block.getWorld()).getHandle().getBlockEntity(blockPos, true);
        te.loadWithComponents(((CompoundTagImpl) ctag).toNMSTag(), CraftRegistry.getMinecraftRegistry());
    }

    @Override
    public boolean setBlockResistance(Material material, float resistance) {
        net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(material);
        if (block == null) {
            return false;
        }
        ReflectionHelper.setFieldValue(net.minecraft.world.level.block.state.BlockBehaviour.class, ReflectionMappingsInfo.BlockBehaviour_explosionResistance, block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.world.level.block.state.BlockBehaviour.class, ReflectionMappingsInfo.BlockBehaviour_explosionResistance, block);
    }

    public static final MethodHandle MATERIAL_PUSH_REACTION_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(BlockBehaviour.BlockStateBase.class, PushReaction.class);

    public static final MethodHandle BLOCK_STRENGTH_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.class, float.class); // destroySpeed

    public net.minecraft.world.level.block.state.BlockState getMaterialBlockState(Material bukkitMaterial) {
        net.minecraft.world.level.block.Block nmsBlock = CraftMagicNumbers.getBlock(bukkitMaterial);
        return nmsBlock != null ? nmsBlock.defaultBlockState() : null;
    }

    @Override
    public void setPushReaction(Material mat, PistonPushReaction reaction) {
        try {
            MATERIAL_PUSH_REACTION_SETTER.invoke(getMaterialBlockState(mat), PushReaction.values()[reaction.ordinal()]);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public float getBlockStrength(Material mat) {
        return getMaterialBlockState(mat).destroySpeed;
    }

    @Override
    public void setBlockStrength(Material mat, float strength) {
        try {
            BLOCK_STRENGTH_SETTER.invoke(getMaterialBlockState(mat), strength);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void doRandomTick(Location location) {
        BlockPos pos = CraftLocation.toBlockPosition(location);
        ChunkAccess nmsChunk = ((CraftChunk) location.getChunk()).getHandle(ChunkStatus.FULL);
        net.minecraft.world.level.block.state.BlockState nmsBlock = nmsChunk.getBlockState(pos);
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        if (nmsBlock.isRandomlyTicking()) {
            nmsBlock.randomTick(nmsWorld, pos, nmsWorld.random);
        }
        FluidState fluid = nmsBlock.getFluidState();
        if (fluid.isRandomlyTicking()) {
            fluid.animateTick(nmsWorld, pos, nmsWorld.random);
        }
    }

    @Override
    public Instrument getInstrumentFor(Material mat) {
        return Instrument.values()[getMaterialBlockState(mat).instrument().ordinal()];
    }

    @Override
    public int getExpDrop(Block block, org.bukkit.inventory.ItemStack item) {
        net.minecraft.world.level.block.Block blockType = CraftMagicNumbers.getBlock(block.getType());
        if (blockType == null) {
            return 0;
        }
        return blockType.getExpDrop(((CraftBlock) block).getNMS(), ((CraftBlock) block).getCraftWorld().getHandle(), ((CraftBlock) block).getPosition(),
                item == null ? null : CraftItemStack.asNMSCopy(item), true);
    }

    @Override
    public void setSpawnerSpawnedType(CreatureSpawner spawner, EntityTag entity) {
        spawner.setSpawnedType(entity.getBukkitEntityType());
        if (entity.getWaitingMechanisms() == null || entity.getWaitingMechanisms().size() == 0) {
            return;
        }
        try {
            // Wrangle a fake entity
            org.bukkit.entity.Entity bukkitEntity = ((CraftWorld) spawner.getWorld()).createEntity(spawner.getLocation(), entity.getBukkitEntityType().getEntityClass());
            Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
            EntityTag entityTag = new EntityTag(bukkitEntity);
            entityTag.isFake = true;
            entityTag.isFakeValid = true;
            for (Mechanism mechanism : entity.getWaitingMechanisms()) {
                entityTag.safeAdjustDuplicate(mechanism);
            }
            nmsEntity.unsetRemoved();
            // Store it into the spawner
            CraftCreatureSpawner bukkitSpawner = (CraftCreatureSpawner) spawner;
            SpawnerBlockEntity nmsSnapshot = (SpawnerBlockEntity) craftBlockEntityState_snapshot.get(bukkitSpawner);
            BaseSpawner nmsSpawner = nmsSnapshot.getSpawner();
            SpawnData toSpawn = nmsSpawner.nextSpawnData;
            net.minecraft.nbt.CompoundTag tag = toSpawn.getEntityToSpawn();
            nmsEntity.saveWithoutId(tag);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void setSpawnerCustomRules(CreatureSpawner spawner, int skyMin, int skyMax, int blockMin, int blockMax) {
        try {
            CraftCreatureSpawner bukkitSpawner = (CraftCreatureSpawner) spawner;
            SpawnerBlockEntity nmsSnapshot = (SpawnerBlockEntity) craftBlockEntityState_snapshot.get(bukkitSpawner);
            BaseSpawner nmsSpawner = nmsSnapshot.getSpawner();
            SpawnData toSpawn = nmsSpawner.nextSpawnData;
            SpawnData.CustomSpawnRules rules = skyMin == -1 ? null : new SpawnData.CustomSpawnRules(new InclusiveRange<>(skyMin, skyMax), new InclusiveRange<>(blockMin, blockMax));
            nmsSpawner.nextSpawnData = new SpawnData(toSpawn.entityToSpawn(), Optional.ofNullable(rules), toSpawn.equipment());
            nmsSpawner.spawnPotentials = SimpleWeightedRandomList.empty();
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static final MethodHandle HOLDERSET_NAMED_BIND = ReflectionHelper.getMethodHandle(HolderSet.Named.class, ReflectionMappingsInfo.HolderSetNamed_bind_method, List.class);
    public static final MethodHandle HOLDER_REFERENCE_BINDTAGS = ReflectionHelper.getMethodHandle(Holder.Reference.class, ReflectionMappingsInfo.HolderReference_bindTags_method, Collection.class);

    @Override
    public void setVanillaTags(Material material, Set<String> tags) {
        Holder<net.minecraft.world.level.block.Block> nmsHolder = CraftMagicNumbers.getBlock(material).builtInRegistryHolder();
        nmsHolder.tags().forEach(nmsTag -> {
            HolderSet.Named<net.minecraft.world.level.block.Block> nmsHolderSet = BuiltInRegistries.BLOCK.getTag(nmsTag).orElse(null);
            if (nmsHolderSet == null) {
                return;
            }
            List<Holder<net.minecraft.world.level.block.Block>> nmsHolders = nmsHolderSet.stream().collect(Collectors.toCollection(ArrayList::new));
            nmsHolders.remove(nmsHolder);
            try {
                HOLDERSET_NAMED_BIND.invoke(nmsHolderSet, nmsHolders);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
            VanillaTagHelper.updateMaterialTag(new CraftBlockTag(BuiltInRegistries.BLOCK, nmsTag));
        });
        List<TagKey<net.minecraft.world.level.block.Block>> newNmsTags = new ArrayList<>();
        for (String tag : tags) {
            TagKey<net.minecraft.world.level.block.Block> newNmsTag = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(tag));
            HolderSet.Named<net.minecraft.world.level.block.Block> nmsHolderSet = BuiltInRegistries.BLOCK.getOrCreateTag(newNmsTag);
            List<Holder<net.minecraft.world.level.block.Block>> nmsHolders = nmsHolderSet.stream().collect(Collectors.toCollection(ArrayList::new));
            nmsHolders.add(nmsHolder);
            try {
                HOLDERSET_NAMED_BIND.invoke(nmsHolderSet, nmsHolders);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
            newNmsTags.add(newNmsTag);
            VanillaTagHelper.addOrUpdateMaterialTag(new CraftBlockTag(BuiltInRegistries.BLOCK, newNmsTag));
        }
        try {
            HOLDER_REFERENCE_BINDTAGS.invoke(nmsHolder, newNmsTags);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        PacketHelperImpl.broadcast(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(((CraftServer) Bukkit.getServer()).getServer().registries())));
    }
}
