package com.denizenscript.denizen.nms.v1_14.helpers;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.nms.v1_14.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.event.world.PortalCreateEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockHelperImpl implements BlockHelper {

    public static final Field craftBlockEntityState_tileEntity;

    @Override
    public void applyPhysics(Location location) {
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftWorld) location.getWorld()).getHandle().applyPhysics(pos, CraftMagicNumbers.getBlock(location.getBlock().getType()));
    }

    static {
        Field f = null;
        try {
            f = CraftBlockEntityState.class.getDeclaredField("tileEntity");
            f.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            Debug.echoError(e);
        }
        craftBlockEntityState_tileEntity = f;
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
        TileEntitySkull tileEntity = getTE((CraftSkull) skull);
        tileEntity.setGameProfile(gameProfile);
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
        te.load(((CompoundTagImpl) ctag).toNMSTag());
    }

    private static net.minecraft.server.v1_14_R1.Block getBlockFrom(Material material) {
        if (material == Material.FLOWER_POT) {
            return Blocks.FLOWER_POT;
        }
        ItemStack is = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material));
        if (is == null) {
            return null;
        }
        Item item = is.getItem();
        if (!(item instanceof ItemBlock)) {
            return null;
        }
        return ((ItemBlock) item).getBlock();
    }

    @Override
    public boolean hasBlock(Material material) {
        return getBlockFrom(material) != null;
    }

    @Override
    public boolean setBlockResistance(Material material, float resistance) {
        net.minecraft.server.v1_14_R1.Block block = getBlockFrom(material);
        if (block == null) {
            return false;
        }
        // protected final float durability;
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_14_R1.Block.class, "durability", block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.server.v1_14_R1.Block block = getBlockFrom(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.server.v1_14_R1.Block.class, "durability", block);
    }

    @Override
    public org.bukkit.block.BlockState generateBlockState(Block block, Material mat) {
        CraftBlockState state = new CraftBlockState(block);
        state.setData(CraftMagicNumbers.getBlock(mat).getBlockData());
        return state;
    }

    // protected final Material material;
    public static final Field BLOCK_MATERIAL = ReflectionHelper.getFields(net.minecraft.server.v1_14_R1.Block.class).get("material");

    // private final EnumPistonReaction R;
    public static final MethodHandle MATERIAL_PUSH_REACTION_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.server.v1_14_R1.Material.class, "R");

    // public final float strength;
    public static final MethodHandle BLOCK_STRENGTH_SETTER = ReflectionHelper.getFinalSetter(net.minecraft.server.v1_14_R1.Block.class, "strength");

    public net.minecraft.server.v1_14_R1.Block getMaterialBlock(Material bukkitMaterial) {
        return ((CraftBlockData) bukkitMaterial.createBlockData()).getState().getBlock();
    }

    public net.minecraft.server.v1_14_R1.Material getInternalMaterial(Material bukkitMaterial) {
        try {
            return (net.minecraft.server.v1_14_R1.Material) BLOCK_MATERIAL.get(getMaterialBlock(bukkitMaterial));
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
    public float getBlockStength(Material mat) {
        return getMaterialBlock(mat).strength;
    }

    @Override
    public void setBlockStrength(Material mat, float strength) {
        try {
            BLOCK_STRENGTH_SETTER.invoke(getMaterialBlock(mat), strength);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
