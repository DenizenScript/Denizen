package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.TileEntitySign;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class SignEditor {

    private static final Field sign_location;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutOpenSignEditor.class);
        sign_location = fields.get("a"); // TODO: Are these accurate (1.8.3)?
    }

    public static PacketPlayOutOpenSignEditor getSignEditorPacket(Location location) {
        PacketPlayOutOpenSignEditor signEditorPacket = new PacketPlayOutOpenSignEditor();
        try {
            sign_location.set(signEditorPacket, new BlockPosition(location.getX(), location.getY(), location.getZ()));
        } catch (Exception e) {
            dB.echoError(e);
        }
        return signEditorPacket;
    }

    public static void editSign(Player player, Location location) {
        PacketPlayOutOpenSignEditor signEditorPacket = getSignEditorPacket(location);
        TileEntity sign = ((CraftWorld) location.getWorld()).getTileEntityAt(location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        if (sign instanceof TileEntitySign) {
            // Prevent client crashing by sending current state of the sign
            PacketHelper.sendPacket(player, sign.getUpdatePacket());
            ((TileEntitySign) sign).isEditable = true;
            ((TileEntitySign) sign).a(((CraftPlayer) player).getHandle());
            PacketHelper.sendPacket(player, signEditorPacket);
        }
        else {
            dB.echoError("Can't edit non-sign materials!");
        }
    }

}
