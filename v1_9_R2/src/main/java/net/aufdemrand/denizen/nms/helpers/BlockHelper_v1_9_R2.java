package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.impl.blocks.BlockData_v1_9_R2;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_9_R2;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.TileEntitySkull;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftSkull;

import java.util.UUID;

public class BlockHelper_v1_9_R2 implements BlockHelper {

    @Override
    public PlayerProfile getPlayerProfile(Skull skull) {
        GameProfile profile = ((CraftSkull) skull).getTileEntity().getGameProfile();
        if (profile == null) {
            return null;
        }
        String name = profile.getName();
        UUID id = profile.getId();
        Property property = Iterables.getFirst(profile.getProperties().get("textures"), null);
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
        return CompoundTag_v1_9_R2.fromNMSTag(tileEntity.E_());
    }

    @Override
    public void setNbtData(Block block, CompoundTag compoundTag) {
        TileEntity tileEntity = ((CraftBlockState) block.getState()).getTileEntity();
        if (tileEntity == null) {
            return;
        }
        tileEntity.a(((CompoundTag_v1_9_R2) compoundTag).toNMSTag());
        tileEntity.update();
    }

    @Override
    public BlockData getBlockData(short id, byte data) {
        return new BlockData_v1_9_R2(id, data);
    }

    @Override
    public BlockData getBlockData(Block block) {
        return new BlockData_v1_9_R2(block);
    }

    @Override
    public BlockData getBlockData(String compressedString) {
        return BlockData_v1_9_R2.fromCompressedString(compressedString);
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
