package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.nms.v1_17.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_17.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.event.world.PortalCreateEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public BlockData parseBlockData(Material material, String otherData) {
        return CraftBlockData.newData(material, otherData);
    }

    @Override
    public List<Location> getBlocksList(PortalCreateEvent event) {
        List<Location> blocks = new ArrayList<>();
        for (org.bukkit.block.BlockState block : event.getBlocks()) {
            blocks.add(block.getLocation());
        }
        return blocks;
    }

    public <T extends BlockEntity> T getTE(CraftBlockEntityState<T> cbs) {
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

    private static net.minecraft.world.level.block.Block getBlockFrom(Material material) {
        if (material == Material.FLOWER_POT) {
            return Blocks.FLOWER_POT;
        }
        ItemStack is = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material));
        if (is == null) {
            return null;
        }
        Item item = is.getItem();
        if (!(item instanceof BlockItem)) {
            return null;
        }
        return ((BlockItem) item).getBlock();
    }

    @Override
    public boolean hasBlock(Material material) {
        return getBlockFrom(material) != null;
    }

    @Override
    public boolean setBlockResistance(Material material, float resistance) {
        net.minecraft.world.level.block.Block block = getBlockFrom(material);
        if (block == null) {
            return false;
        }
        ReflectionHelper.setFieldValue(net.minecraft.world.level.block.state.BlockBehaviour.class, ReflectionMappingsInfo.BlockBehaviour_explosionResistance, block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.world.level.block.Block block = getBlockFrom(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.world.level.block.state.BlockBehaviour.class, ReflectionMappingsInfo.BlockBehaviour_explosionResistance, block);
    }

    @Override
    public org.bukkit.block.BlockState generateBlockState(Block block, Material mat) {
        CraftBlockState state = new CraftBlockState(block);
        state.setData(CraftMagicNumbers.getBlock(mat).defaultBlockState());
        return state;
    }

    public static final Field BLOCK_MATERIAL = ReflectionHelper.getFields(net.minecraft.world.level.block.state.BlockBehaviour.class).getFirstOfType(net.minecraft.world.level.material.Material.class);

    public static final MethodHandle MATERIAL_PUSH_REACTION_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.material.Material.class, PushReaction.class);

    public static final MethodHandle BLOCK_STRENGTH_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.class, float.class); // destroySpeed

    public net.minecraft.world.level.block.Block getMaterialBlock(Material bukkitMaterial) {
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
    public String getPushReaction(Material mat) {
        return getInternalMaterial(mat).getPushReaction().name();
    }

    @Override
    public void setPushReaction(Material mat, String reaction) {
        try {
            MATERIAL_PUSH_REACTION_SETTER.invoke(getInternalMaterial(mat), PushReaction.valueOf(reaction));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public float getBlockStength(Material mat) {
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

    @Override
    public void doRandomTick(Location location) {
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        LevelChunk nmsChunk = ((CraftChunk) location.getChunk()).getHandle();
        net.minecraft.world.level.block.state.BlockState nmsBlock = nmsChunk.getBlockState(pos);
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        if (nmsBlock.isRandomlyTicking()) {
            nmsBlock.tick(nmsWorld, pos, nmsWorld.random);
        }
        FluidState fluid = nmsBlock.getFluidState();
        if (fluid.isRandomlyTicking()) {
            fluid.animateTick(nmsWorld, pos, nmsWorld.random);
        }
    }
}
