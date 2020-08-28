package com.denizenscript.denizen.nms.v1_13.helpers;

import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.v1_13.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftSkull;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.event.world.PortalCreateEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockHelperImpl implements BlockHelper {

    @Override
    public void applyPhysics(Location location) {
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftWorld) location.getWorld()).getHandle().applyPhysics(pos, CraftMagicNumbers.getBlock(location.getBlock().getType()));
    }

    @Override
    public List<Location> getBlocksList(PortalCreateEvent event) {
        List<Location> blocks = new ArrayList<>();
        for (Block block : event.getBlocks()) {
            blocks.add(block.getLocation());
        }
        return blocks;
    }

    @Override
    public ModernBlockData parseBlockData(Material material, String otherData) {
        CraftBlockData data = CraftBlockData.newData(material, otherData);
        return new ModernBlockData(data);
    }

    public <T extends TileEntity> T getTE(CraftBlockEntityState<T> cbs) {
        try {
            Field f = CraftBlockEntityState.class.getDeclaredField("tileEntity");
            f.setAccessible(true);
            return (T) f.get(cbs);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            Debug.echoError(e);
        }
        return null;
    }

    @Override
    public PlayerProfile getPlayerProfile(Skull skull) {
        GameProfile profile = getTE(((CraftSkull) skull)).getGameProfile();
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
        skull.getBlock().getState().update();
    }

    @Override
    public CompoundTag getNbtData(Block block) {
        TileEntity tileEntity = getTE((CraftBlockEntityState) block.getState());
        if (tileEntity == null) {
            return null;
        }
        return CompoundTagImpl.fromNMSTag(tileEntity.aa_());
    }

    @Override
    public void setNbtData(Block block, CompoundTag compoundTag) {
        TileEntity tileEntity = getTE((CraftBlockEntityState) block.getState());
        if (tileEntity == null) {
            return;
        }
        tileEntity.load(((CompoundTagImpl) compoundTag).toNMSTag());
        tileEntity.update();
    }

    private static net.minecraft.server.v1_13_R2.Block getBlockFrom(Material material) {
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
        net.minecraft.server.v1_13_R2.Block block = getBlockFrom(material);
        if (block == null) {
            return false;
        }
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_13_R2.Block.class, "durability", block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.server.v1_13_R2.Block block = getBlockFrom(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.server.v1_13_R2.Block.class, "durability", block);
    }

    @Override
    public org.bukkit.block.BlockState generateBlockState(Material mat) {
        return new CraftBlockState(mat);
    }

}
