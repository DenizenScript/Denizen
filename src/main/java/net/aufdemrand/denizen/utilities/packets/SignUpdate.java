package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateSign;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class SignUpdate {

    private static final Field sign_world, sign_location, sign_lines;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutUpdateSign.class);
        sign_world = fields.get("a");
        sign_location = fields.get("b");
        sign_lines = fields.get("c");
    }

    public static PacketPlayOutUpdateSign getSignUpdatePacket(Location location, String[] lines) {
        PacketPlayOutUpdateSign signUpdatePacket = new PacketPlayOutUpdateSign();
        try {
            sign_world.set(signUpdatePacket, ((CraftWorld) location.getWorld()).getHandle());
            sign_location.set(signUpdatePacket, new BlockPosition(location.getBlockX(),
                    location.getBlockY(), location.getBlockZ()));
            sign_lines.set(signUpdatePacket, new IChatBaseComponent[] {
                    lines[0] != null ? new ChatComponentText(lines[0]) : null,
                    lines[1] != null ? new ChatComponentText(lines[1]) : null,
                    lines[2] != null ? new ChatComponentText(lines[2]) : null,
                    lines[3] != null ? new ChatComponentText(lines[3]) : null
            });
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return signUpdatePacket;
    }

    public static void updateSign(Player player, Location location, String[] lines) {
        PacketPlayOutUpdateSign signUpdatePacket = getSignUpdatePacket(location, lines);
        PacketHelper.sendPacket(player, signUpdatePacket);
    }

}
