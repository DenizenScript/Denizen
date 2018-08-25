package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.impl.blocks.BlockData_v1_13_R1;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_13_R1;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.minecraft.server.v1_13_R1.Blocks;
import net.minecraft.server.v1_13_R1.Item;
import net.minecraft.server.v1_13_R1.ItemBlock;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.TileEntity;
import net.minecraft.server.v1_13_R1.TileEntitySkull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.FlowerPot;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.UUID;

public class BlockHelper_v1_13_R1 implements BlockHelper {

    public <T extends TileEntity> T getTE(CraftBlockEntityState<T> cbs) {
        try {
            Field f = CraftBlockEntityState.class.getDeclaredField("tileEntity");
            f.setAccessible(true);
            return (T) f.get(cbs);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MaterialData getFlowerpotContents(Block block) {
        return ((FlowerPot) block.getState()).getContents();
    }

    @Override
    public void setFlowerpotContents(Block block, MaterialData data) {
        FlowerPot flowerPot = (FlowerPot) block.getState();
        flowerPot.setContents(data);
        flowerPot.update();
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
        net.minecraft.server.v1_13_R1.Block block = tileEntity.getBlock().getBlock();
        if (block != Blocks.PLAYER_HEAD && block != Blocks.PLAYER_WALL_HEAD) {
            // TODO: 1.13 - force block change?
            //tileEntity.setSkullType(SkullType.PLAYER.ordinal());
            return;
        }
        tileEntity.setGameProfile(gameProfile);
        skull.getBlock().getState().update();
    }

    @Override
    public CompoundTag getNbtData(Block block) {
        TileEntity tileEntity = getTE((CraftBlockEntityState) (CraftBlockState) block.getState());
        if (tileEntity == null) {
            return null;
        }
        return CompoundTag_v1_13_R1.fromNMSTag(tileEntity.aa_());
    }

    @Override
    public void setNbtData(Block block, CompoundTag compoundTag) {
        TileEntity tileEntity = getTE((CraftBlockEntityState) (CraftBlockState) block.getState());
        if (tileEntity == null) {
            return;
        }
        tileEntity.load(((CompoundTag_v1_13_R1) compoundTag).toNMSTag());
        tileEntity.update();
    }

    @Override
    public BlockData getBlockData(Material material, byte data) {
        return new BlockData_v1_13_R1(material, data);
    }

    @Override
    public BlockData getBlockData(Block block) {
        return new BlockData_v1_13_R1(block);
    }

    @Override
    public BlockData getBlockData(String compressedString) {
        return BlockData_v1_13_R1.fromCompressedString(compressedString);
    }

    private static net.minecraft.server.v1_13_R1.Block getBlockFrom(Material material) {
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
        net.minecraft.server.v1_13_R1.Block block = getBlockFrom(material);
        if (block == null) {
            return false;
        }
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_13_R1.Block.class, "durability", block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.server.v1_13_R1.Block block = getBlockFrom(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.server.v1_13_R1.Block.class, "durability", block);
    }

    @Override
    public boolean isSafeBlock(Material material) {
        // this is presumably more accurate these days
        return !material.isSolid();
    }
}
