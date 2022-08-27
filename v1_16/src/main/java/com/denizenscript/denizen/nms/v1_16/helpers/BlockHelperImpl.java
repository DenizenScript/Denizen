package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.nms.v1_16.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSkull;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
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
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftWorld) location.getWorld()).getHandle().applyPhysics(pos, CraftMagicNumbers.getBlock(location.getBlock().getType()));
    }

    public <T extends TileEntity> T getTE(CraftBlockEntityState<T> cbs) {
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
        GameProfile profile = getTE(((CraftSkull) skull)).gameProfile;
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
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (te != null) {
            NBTTagCompound compound = new NBTTagCompound();
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
        BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos);
        te.load(((CraftBlockData) block.getBlockData()).getState(), ((CompoundTagImpl) ctag).toNMSTag());
    }

    @Override
    public boolean setBlockResistance(Material material, float resistance) {
        net.minecraft.server.v1_16_R3.Block block = getMaterialBlock(material);
        if (block == null) {
            return false;
        }
        // protected final float durability;
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_16_R3.BlockBase.class, "durability", block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.server.v1_16_R3.Block block = getMaterialBlock(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.server.v1_16_R3.BlockBase.class, "durability", block);
    }

    @Override
    public org.bukkit.block.BlockState generateBlockState(Block block, Material mat) {
        CraftBlockState state = new CraftBlockState(block);
        state.setData(CraftMagicNumbers.getBlock(mat).getBlockData());
        return state;
    }

    public static final Field BLOCK_MATERIAL = ReflectionHelper.getFields(net.minecraft.server.v1_16_R3.BlockBase.class).get("material");

    public static final MethodHandle MATERIAL_PUSH_REACTION_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.server.v1_16_R3.Material.class, "T");

    public static final MethodHandle BLOCK_STRENGTH_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.server.v1_16_R3.BlockBase.BlockData.class, "strength");

    public net.minecraft.server.v1_16_R3.Block getMaterialBlock(Material bukkitMaterial) {
        if (!bukkitMaterial.isBlock()) {
            return null;
        }
        return ((CraftBlockData) bukkitMaterial.createBlockData()).getState().getBlock();
    }

    public net.minecraft.server.v1_16_R3.Material getInternalMaterial(Material bukkitMaterial) {
        try {
            return (net.minecraft.server.v1_16_R3.Material) BLOCK_MATERIAL.get(getMaterialBlock(bukkitMaterial));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    @Override
    public String getPushReaction(Material mat) {
        return getInternalMaterial(mat).getPushReaction().name();
    }

    @Override
    public void setPushReaction(Material mat, String reaction) {
        try {
            MATERIAL_PUSH_REACTION_SETTER.invoke(getInternalMaterial(mat), EnumPistonReaction.valueOf(reaction));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public float getBlockStrength(Material mat) {
        return getMaterialBlock(mat).getBlockData().strength;
    }

    @Override
    public void setBlockStrength(Material mat, float strength) {
        try {
            BLOCK_STRENGTH_SETTER.invoke(getMaterialBlock(mat).getBlockData(), strength);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void doRandomTick(Location location) {
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Chunk nmsChunk = ((CraftChunk) location.getChunk()).getHandle();
        IBlockData nmsBlock = nmsChunk.getType(pos);
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        if (nmsBlock.isTicking()) {
            nmsBlock.b(nmsWorld, pos, nmsWorld.random);
        }
        Fluid fluid = nmsBlock.getFluid();
        if (fluid.f()) {
            fluid.b(nmsWorld, pos, nmsWorld.random);
        }
    }
}
