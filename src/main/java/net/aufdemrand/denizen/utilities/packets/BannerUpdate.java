package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBanner;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class BannerUpdate {

    private static final Field tile_location, tile_action, tile_data;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutTileEntityData.class);
        tile_location = fields.get("a");
        tile_action = fields.get("b");
        tile_data = fields.get("c");
    }

    public static PacketPlayOutTileEntityData getTileEntityDataPacket(Location location, DyeColor base, List<Pattern> patterns) {
        PacketPlayOutTileEntityData packet = new PacketPlayOutTileEntityData();
        try {
            tile_location.set(packet, new BlockPosition(location.getBlockX(),
                    location.getBlockY(), location.getBlockZ()));
            tile_action.set(packet, 6);

            NBTTagCompound compound = new NBTTagCompound();
            ((CraftBanner) location.getBlock().getState()).getTileEntity().b(compound);

            NBTTagList nbtPatterns = new NBTTagList();
            for (Pattern pattern : patterns) {
                NBTTagCompound patternCompound = new NBTTagCompound();
                patternCompound.setInt("Color", pattern.getColor().getDyeData());
                patternCompound.setString("Pattern", pattern.getPattern().getIdentifier());
                nbtPatterns.add(patternCompound);
            }

            TileEntityBanner.a(compound, base.getDyeData(), nbtPatterns);
            tile_data.set(packet, compound);
        }
        catch (IllegalAccessException e) {
            dB.echoError(e);
        }
        return packet;
    }

    public static void updateBanner(Player player, Location location, DyeColor base, List<Pattern> patterns) {
        PacketPlayOutTileEntityData packet = getTileEntityDataPacket(location, base, patterns);
        PacketHelper.sendPacket(player, packet);
    }
}
