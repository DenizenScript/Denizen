package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.impl.blocks.BlockData_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.minecraft.server.v1_10_R1.TileEntitySkull;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftSkull;

import java.util.UUID;

public class BlockHelper_v1_10_R1 implements BlockHelper {

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
                    new Property("value", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
        TileEntitySkull tileEntity = ((CraftSkull) skull).getTileEntity();
        tileEntity.setSkullType(SkullType.PLAYER.ordinal());
        tileEntity.setGameProfile(gameProfile);
        skull.getBlock().getState().update();
    }

    @Override
    public BlockData getBlockData(short id, byte data) {
        return new BlockData_v1_10_R1(id, data);
    }

    @Override
    public BlockData getBlockData(Block block) {
        return new BlockData_v1_10_R1(block);
    }

    @Override
    public BlockData getBlockData(String compressedString) {
        return BlockData_v1_10_R1.fromCompressedString(compressedString);
    }
}
