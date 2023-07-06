package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.*;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.tag.CraftBlockTag;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

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
        GameProfile profile = getTE(((CraftSkull) skull)).owner;
        if (profile == null) {
            return null;
        }
        String name = profile.getName();
        UUID id = profile.getId();
        com.mojang.authlib.properties.Property property = Iterables.getFirst(profile.getProperties().get("textures"), null);
        return new PlayerProfile(name, id, property != null ? property.getValue() : null);
    }

    @Override
    public void setPlayerProfile(Skull skull, PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().put("textures",
                    new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
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
            net.minecraft.nbt.CompoundTag compound = te.saveWithFullMetadata();
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
        te.load(((CompoundTagImpl) ctag).toNMSTag());
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

    @Override
    public org.bukkit.block.BlockState generateBlockState(Block block, Material mat) {
        try {
            CraftBlockState state = (CraftBlockState) CRAFTBLOCKSTATE_CONSTRUCTOR.invoke(block);
            state.setData(CraftMagicNumbers.getBlock(mat).defaultBlockState());
            return state;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public static final MethodHandle CRAFTBLOCKSTATE_CONSTRUCTOR = ReflectionHelper.getConstructor(CraftBlockState.class, Block.class);

    public static final MethodHandle MATERIAL_PUSH_REACTION_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(BlockBehaviour.BlockStateBase.class, PushReaction.class);

    public static final MethodHandle BLOCK_STRENGTH_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.class, float.class); // destroySpeed

    public net.minecraft.world.level.block.state.BlockState getMaterialBlockState(Material bukkitMaterial) { // TODO: Is this needed? can probably just Block#defaultBlockState
        if (!bukkitMaterial.isBlock()) {
            return null;
        }
        return ((CraftBlockData) bukkitMaterial.createBlockData()).getState();
    }

    @Override
    public void setPushReaction(Material mat, PistonPushReaction reaction) {
        try {
            MATERIAL_PUSH_REACTION_SETTER.invoke(CraftMagicNumbers.getBlock(mat).defaultBlockState(), PushReaction.values()[reaction.ordinal()]);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public float getBlockStrength(Material mat) {
        return CraftMagicNumbers.getBlock(mat).defaultBlockState().destroySpeed;
    }

    @Override
    public void setBlockStrength(Material mat, float strength) {
        try {
            BLOCK_STRENGTH_SETTER.invoke(CraftMagicNumbers.getBlock(mat).defaultBlockState(), strength);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    // This is to debork Spigot's class remapper mishandling 'getFluidState' which remaps 'FluidState' to 'material.FluidType' (incorrectly) in the call and thus errors out.
    // Relevant issue: https://hub.spigotmc.org/jira/browse/SPIGOT-6696
    // NOTE: Not fixed as of 1.19 initial update
    public static final MethodHandle BLOCKSTATEBASE_GETFLUIDSTATE = ReflectionHelper.getMethodHandle(BlockBehaviour.BlockStateBase.class, ReflectionMappingsInfo.BlockBehaviourBlockStateBase_getFluidState_method);
    public static final MethodHandle FLUIDSTATE_ISRANDOMLYTICKING = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), ReflectionMappingsInfo.FluidState_isRandomlyTicking_method);
    public static final MethodHandle FLUIDSTATE_ISEMPTY = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), ReflectionMappingsInfo.FluidState_isEmpty_method);
    public static final MethodHandle FLUIDSTATE_CREATELEGACYBLOCK = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), ReflectionMappingsInfo.FluidState_createLegacyBlock_method);
    public static final MethodHandle FLUIDSTATE_ANIMATETICK = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), ReflectionMappingsInfo.FluidState_animateTick_method, Level.class, BlockPos.class, RandomSource.class);

    @Override
    public void doRandomTick(Location location) {
        BlockPos pos = CraftLocation.toBlockPosition(location);
        ChunkAccess nmsChunk = ((CraftChunk) location.getChunk()).getHandle(ChunkStatus.FULL);
        net.minecraft.world.level.block.state.BlockState nmsBlock = nmsChunk.getBlockState(pos);
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        if (nmsBlock.isRandomlyTicking()) {
            nmsBlock.randomTick(nmsWorld, pos, nmsWorld.random);
        }
        try {
            Debug.log("Ticking fluid state");
             FluidState fluid = nmsBlock.getFluidState();
             if (fluid.isRandomlyTicking()) {
                 fluid.animateTick(nmsWorld, pos, nmsWorld.random);
             }
//            Object fluid = BLOCKSTATEBASE_GETFLUIDSTATE.invoke(nmsBlock);
//            if ((boolean) FLUIDSTATE_ISRANDOMLYTICKING.invoke(fluid)) {
//                FLUIDSTATE_ANIMATETICK.invoke(fluid, nmsWorld, pos, nmsWorld.random);
//            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public Instrument getInstrumentFor(Material mat) {
        Instrument first = Instrument.values()[getMaterialBlockState(mat).instrument().ordinal()];
        Instrument second = Instrument.values()[CraftMagicNumbers.getBlock(mat).defaultBlockState().instrument().ordinal()];
        Debug.log("Getting instrument with both methods: " + (first == second));
        return first;
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
            Entity nmsEntity = ((CraftWorld) spawner.getWorld()).createEntity(spawner.getLocation(), entity.getBukkitEntityType().getEntityClass());
            EntityTag entityTag = new EntityTag(nmsEntity.getBukkitEntity());
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
            nmsSpawner.nextSpawnData = new SpawnData(toSpawn.entityToSpawn(), Optional.ofNullable(rules));
            nmsSpawner.spawnPotentials = SimpleWeightedRandomList.empty();
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public Color getMapColor(Block block) {
        CraftBlock craftBlock = (CraftBlock) block;
        return Color.fromRGB(craftBlock.getNMS().getMapColor(craftBlock.getHandle(), craftBlock.getPosition()).col);
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
        ClientboundUpdateTagsPacket tagsPacket = new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(((CraftServer) Bukkit.getServer()).getServer().registries()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketHelperImpl.send(player, tagsPacket);
        }
    }
}
