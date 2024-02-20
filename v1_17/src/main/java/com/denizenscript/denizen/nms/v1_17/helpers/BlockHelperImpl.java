package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.nms.v1_17.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_17.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.PushReaction;
import org.bukkit.Color;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

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
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftWorld) location.getWorld()).getHandle().updateNeighborsAt(pos, CraftMagicNumbers.getBlock(location.getBlock().getType()));
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
        BlockEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(new BlockPos(block.getX(), block.getY(), block.getZ()), true);
        if (te != null) {
            net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
            te.save(compound);
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
        BlockEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos, true);
        te.load(((CompoundTagImpl) ctag).toNMSTag());
    }

    @Override
    public boolean setBlockResistance(Material material, float resistance) {
        net.minecraft.world.level.block.Block block = getMaterialBlock(material);
        if (block == null) {
            return false;
        }
        ReflectionHelper.setFieldValue(net.minecraft.world.level.block.state.BlockBehaviour.class, ReflectionMappingsInfo.BlockBehaviour_explosionResistance, block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.world.level.block.Block block = getMaterialBlock(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.world.level.block.state.BlockBehaviour.class, ReflectionMappingsInfo.BlockBehaviour_explosionResistance, block);
    }

    public static final Field BLOCK_MATERIAL = ReflectionHelper.getFields(net.minecraft.world.level.block.state.BlockBehaviour.class).getFirstOfType(net.minecraft.world.level.material.Material.class);

    public static final MethodHandle MATERIAL_PUSH_REACTION_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.material.Material.class, PushReaction.class);

    public static final MethodHandle BLOCK_STRENGTH_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.class, float.class); // destroySpeed

    public net.minecraft.world.level.block.Block getMaterialBlock(Material bukkitMaterial) {
        if (!bukkitMaterial.isBlock()) {
            return null;
        }
        return ((CraftBlockData) bukkitMaterial.createBlockData()).getState().getBlock();
    }

    public net.minecraft.world.level.material.Material getInternalMaterial(Material bukkitMaterial) {
        try {
            return (net.minecraft.world.level.material.Material) BLOCK_MATERIAL.get(getMaterialBlock(bukkitMaterial));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    @Override
    public PistonPushReaction getPushReaction(Material mat) {
        return PistonPushReaction.VALUES[getInternalMaterial(mat).getPushReaction().ordinal()];
    }

    @Override
    public void setPushReaction(Material mat, PistonPushReaction reaction) {
        try {
            MATERIAL_PUSH_REACTION_SETTER.invoke(getInternalMaterial(mat), PushReaction.values()[reaction.ordinal()]);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public float getBlockStrength(Material mat) {
        return getMaterialBlock(mat).defaultBlockState().destroySpeed;
    }

    @Override
    public void setBlockStrength(Material mat, float strength) {
        try {
            BLOCK_STRENGTH_SETTER.invoke(getMaterialBlock(mat).defaultBlockState(), strength);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    // This is to debork Spigot's class remapper mishandling 'getFluidState' which remaps 'FluidState' to 'material.FluidType' (incorrectly) in the call and thus errors out.
    // TODO: 1.18: This might be fixed by Spigot and can be switched to raw method calls
    // Relevant issue: https://hub.spigotmc.org/jira/browse/SPIGOT-6696
    public static MethodHandle BLOCKSTATEBASE_GETFLUIDSTATE = ReflectionHelper.getMethodHandle(BlockBehaviour.BlockStateBase.class, "getFluid");
    public static MethodHandle FLUIDSTATE_ISRANDOMLYTICKING = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), "f");
    public static MethodHandle FLUIDSTATE_ISEMPTY = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), "isEmpty");
    public static MethodHandle FLUIDSTATE_CREATELEGACYBLOCK = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), "getBlockData");
    public static MethodHandle FLUIDSTATE_ANIMATETICK = ReflectionHelper.getMethodHandle(BLOCKSTATEBASE_GETFLUIDSTATE.type().returnType(), "a", Level.class, BlockPos.class, Random.class);

    @Override
    public void doRandomTick(Location location) {
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        LevelChunk nmsChunk = ((CraftChunk) location.getChunk()).getHandle();
        net.minecraft.world.level.block.state.BlockState nmsBlock = nmsChunk.getBlockState(pos);
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        if (nmsBlock.isRandomlyTicking()) {
            nmsBlock.randomTick(nmsWorld, pos, nmsWorld.random);
        }
        try {
            // FluidState fluid = nmsBlock.getFluidState();
            // if (fluid.isRandomlyTicking()) {
            //     fluid.animateTick(nmsWorld, pos, nmsWorld.random);
            // }
            Object fluid = BLOCKSTATEBASE_GETFLUIDSTATE.invoke(nmsBlock);
            if ((boolean) FLUIDSTATE_ISRANDOMLYTICKING.invoke(fluid)) {
                FLUIDSTATE_ANIMATETICK.invoke(fluid, nmsWorld, pos, nmsWorld.random);
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public Instrument getInstrumentFor(Material mat) {
        net.minecraft.world.level.block.Block blockType = getMaterialBlock(mat);
        NoteBlockInstrument nmsInstrument = NoteBlockInstrument.byState(blockType.defaultBlockState());
        return Instrument.values()[(nmsInstrument.ordinal())];
    }

    @Override
    public void ringBell(Bell bell) {
        org.bukkit.block.data.type.Bell bellData = (org.bukkit.block.data.type.Bell) bell.getBlockData();
        Direction face = CraftBlock.blockFaceToNotch(bellData.getFacing());
        Direction dir = Direction.NORTH;
        switch (bellData.getAttachment()) {
            case DOUBLE_WALL:
            case SINGLE_WALL:
                switch (face) {
                    case NORTH:
                    case SOUTH:
                        dir = Direction.EAST;
                        break;
                }
                break;
            case FLOOR:
                dir = face;
                break;
        }
        CraftBlock craftBlock = (CraftBlock) bell.getBlock();
        ((BellBlock) Blocks.BELL).attemptToRing(craftBlock.getCraftWorld().getHandle(), craftBlock.getPosition(), dir);
    }

    @Override
    public int getExpDrop(Block block, org.bukkit.inventory.ItemStack item) {
        net.minecraft.world.level.block.Block blockType = getMaterialBlock(block.getType());
        if (blockType == null) {
            return 0;
        }
        return blockType.getExpDrop(((CraftBlock) block).getNMS(), ((CraftBlock) block).getCraftWorld().getHandle(), ((CraftBlock) block).getPosition(),
                item == null ? null : CraftItemStack.asNMSCopy(item));
    }

    @Override
    public Color getMapColor(Block block) {
        CraftBlock craftBlock = (CraftBlock) block;
        return Color.fromRGB(craftBlock.getNMS().getMapColor(craftBlock.getHandle(), craftBlock.getPosition()).col);
    }

}
