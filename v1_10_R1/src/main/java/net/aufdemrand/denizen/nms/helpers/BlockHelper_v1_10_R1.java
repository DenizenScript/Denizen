package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.impl.blocks.BlockData_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.minecraft.server.v1_10_R1.Item;
import net.minecraft.server.v1_10_R1.ItemBlock;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.TileEntity;
import net.minecraft.server.v1_10_R1.TileEntitySkull;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.FlowerPot;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.material.MaterialData;

import java.util.UUID;

public class BlockHelper_v1_10_R1 implements BlockHelper {

    @Override
    public int idFor(Material mat) {
        return mat.getId();
    }

    @Override
    public MaterialData getFlowerpotContents(Block block) {
        MaterialData contents = ((FlowerPot) block.getState()).getContents();
        return contents == null ? new MaterialData(Material.AIR) : contents;
    }

    @Override
    public void setFlowerpotContents(Block block, MaterialData data) {
        FlowerPot flowerPot = (FlowerPot) block.getState();
        flowerPot.setContents(data);
        flowerPot.update();
    }

    @Override
    public PlayerProfile getPlayerProfile(Skull skull) {
        GameProfile profile = ((CraftSkull) skull).getTileEntity().getGameProfile();
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
        TileEntitySkull tileEntity = ((CraftSkull) skull).getTileEntity();
        tileEntity.setSkullType(SkullType.PLAYER.ordinal());
        tileEntity.setGameProfile(gameProfile);
        skull.getBlock().getState().update();
    }

    @Override
    public CompoundTag getNbtData(Block block) {
        TileEntity tileEntity = ((CraftBlockState) block.getState()).getTileEntity();
        if (tileEntity == null) {
            return null;
        }
        return CompoundTag_v1_10_R1.fromNMSTag(tileEntity.c());
    }

    @Override
    public void setNbtData(Block block, CompoundTag compoundTag) {
        TileEntity tileEntity = ((CraftBlockState) block.getState()).getTileEntity();
        if (tileEntity == null) {
            return;
        }
        tileEntity.a(((CompoundTag_v1_10_R1) compoundTag).toNMSTag());
        tileEntity.update();
    }

    @Override
    public BlockData getBlockData(Material material, byte data) {
        return new BlockData_v1_10_R1(material, data);
    }

    @Override
    public BlockData getBlockData(Block block) {
        return new BlockData_v1_10_R1(block);
    }

    @Override
    public BlockData getBlockData(String compressedString) {
        return BlockData_v1_10_R1.fromCompressedString(compressedString);
    }

    private static net.minecraft.server.v1_10_R1.Block getBlockFrom(Material material) {
        ItemStack is = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material));
        if (is == null) {
            return null;
        }
        Item item = is.getItem();
        if (!(item instanceof ItemBlock)) {
            return null;
        }
        return ((ItemBlock) item).d();
    }

    @Override
    public boolean hasBlock(Material material) {
        return getBlockFrom(material) != null;
    }

    @Override
    public boolean setBlockResistance(Material material, float resistance) {
        net.minecraft.server.v1_10_R1.Block block = getBlockFrom(material);
        if (block == null) {
            return false;
        }
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_10_R1.Block.class, "durability", block, resistance);
        return true;
    }

    @Override
    public float getBlockResistance(Material material) {
        net.minecraft.server.v1_10_R1.Block block = getBlockFrom(material);
        if (block == null) {
            return 0;
        }
        return ReflectionHelper.getFieldValue(net.minecraft.server.v1_10_R1.Block.class, "durability", block);
    }

    @Override
    public boolean isSafeBlock(Material material) {
        // Quick util function to decide whether
        // A block is 'safe' (Can be spawned inside of) - air, tallgrass, etc.
        // Credit to Mythan for compiling the initial list
        switch (material) {
            case LEVER:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case REDSTONE_WIRE:
            case SAPLING:
            case SIGN_POST:
            case WALL_SIGN:
            case SNOW:
            case TORCH:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
            case RAILS:
            case POWERED_RAIL:
            case NETHER_WARTS:
            case NETHER_STALK:
            case VINE:
            case SUGAR_CANE_BLOCK:
            case CROPS:
            case LONG_GRASS:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case DEAD_BUSH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case AIR:
            case YELLOW_FLOWER:
            case RED_ROSE:
                return true;
            default:
                return false;
        }
    }
}
