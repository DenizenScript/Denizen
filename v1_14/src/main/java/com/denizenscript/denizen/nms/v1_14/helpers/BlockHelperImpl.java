package com.denizenscript.denizen.nms.v1_14.helpers;

import com.denizenscript.denizen.nms.v1_14.impl.blocks.BlockDataImpl;
import com.denizenscript.denizen.nms.v1_14.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftLegacy;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.material.MaterialData;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockHelperImpl implements BlockHelper {

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
            Field f = CraftBlockEntityState.class.getDeclaredField("tileEntity");
            f.setAccessible(true);
            return (T) f.get(cbs);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
        catch (NoSuchFieldException e) {
            Debug.echoError(e);
        }
        return null;
    }

    @Override
    public int idFor(Material mat) {
        if (mat.isLegacy()) {
            return mat.getId();
        }
        return CraftLegacy.toLegacy(mat).getId();
    }

    @Override
    public MaterialData getFlowerpotContents(Block block) {
        throw new UnsupportedOperationException("As of Minecraft version 1.13 potted flowers each have their own material, such as POTTED_CACTUS.");
    }

    @Override
    public void setFlowerpotContents(Block block, MaterialData data) {
        throw new UnsupportedOperationException("As of Minecraft version 1.13 potted flowers each have their own material, such as POTTED_CACTUS.");
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
        NMSHandler.getChunkHelper().changeChunkServerThread(block.getWorld());
        org.bukkit.block.BlockState state = block.getState();
        NMSHandler.getChunkHelper().restoreServerThread(block.getWorld());
        TileEntity tileEntity = getTE((CraftBlockEntityState) state);
        if (tileEntity == null) {
            return null;
        }
        return CompoundTagImpl.fromNMSTag(tileEntity.b());
    }

    @Override
    public void setNbtData(Block block, CompoundTag compoundTag) {
        NMSHandler.getChunkHelper().changeChunkServerThread(block.getWorld());
        org.bukkit.block.BlockState state = block.getState();
        NMSHandler.getChunkHelper().restoreServerThread(block.getWorld());
        TileEntity tileEntity = getTE((CraftBlockEntityState) state);
        if (tileEntity == null) {
            return;
        }
        tileEntity.load(((CompoundTagImpl) compoundTag).toNMSTag());
        tileEntity.update();
    }

    @Override
    public BlockData getBlockData(Material material, byte data) {
        return new BlockDataImpl(material, data);
    }

    @Override
    public BlockData getBlockData(ModernBlockData data) {
        return new BlockDataImpl(data.data);
    }

    @Override
    public BlockData getBlockData(Block block) {
        return new BlockDataImpl(block);
    }

    @Override
    public BlockData getBlockData(String compressedString) {
        return BlockDataImpl.fromCompressedString(compressedString);
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
    public boolean isSafeBlock(Material material) {
        // this is presumably more accurate these days
        return !material.isSolid();
    }

    @Override
    public org.bukkit.block.BlockState generateBlockState(Material mat) {
        return new CraftBlockState(mat);
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
