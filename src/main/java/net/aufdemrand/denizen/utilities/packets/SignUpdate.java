package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_10_R1.ChatComponentText;
import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_10_R1.TileEntity;
import net.minecraft.server.v1_10_R1.TileEntitySign;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class SignUpdate {

    private static final Field block_location, block_action, block_nbt;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutTileEntityData.class);
        block_location = fields.get("a");
        block_action = fields.get("b");
        block_nbt = fields.get("c");
    }

    public static PacketPlayOutTileEntityData getSignUpdatePacket(TileEntitySign sign, String[] lines) {
        PacketPlayOutTileEntityData signUpdatePacket = new PacketPlayOutTileEntityData();
        try {
            block_location.set(signUpdatePacket, sign.getPosition());
            block_action.set(signUpdatePacket, 9);
            IChatBaseComponent[] realLines = sign.lines;
            sign.lines[0] = lines[0] != null ? new ChatComponentText(lines[0]) : null;
            sign.lines[1] = lines[1] != null ? new ChatComponentText(lines[1]) : null;
            sign.lines[2] = lines[2] != null ? new ChatComponentText(lines[2]) : null;
            sign.lines[3] = lines[3] != null ? new ChatComponentText(lines[3]) : null;
            block_nbt.set(signUpdatePacket, sign.save(new NBTTagCompound()));
            System.arraycopy(realLines, 0, sign.lines, 0, 4);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return signUpdatePacket;
    }

    public static void updateSign(Player player, Location location, String[] lines) {
        TileEntity tileEntity = ((CraftWorld)location.getWorld()).getTileEntityAt(location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        if (tileEntity == null || !(tileEntity instanceof TileEntitySign)) {
            return;
        }
        PacketPlayOutTileEntityData signUpdatePacket = getSignUpdatePacket((TileEntitySign)tileEntity, lines);
        PacketHelper.sendPacket(player, signUpdatePacket);
    }

}
