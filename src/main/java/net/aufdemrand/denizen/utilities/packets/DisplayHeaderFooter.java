package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class DisplayHeaderFooter {

    private static final Field tab_header, tab_footer;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutPlayerListHeaderFooter.class);
        tab_header = fields.get("a");
        tab_footer = fields.get("b");
    }

    public static PacketPlayOutPlayerListHeaderFooter getHeaderFooterPacket(String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter headerFooterPacket = new PacketPlayOutPlayerListHeaderFooter();
        try {
            tab_header.set(headerFooterPacket, new ChatComponentText(header));
            tab_footer.set(headerFooterPacket, new ChatComponentText(footer));
        } catch (Exception e) {
            dB.echoError(e);
        }
        return headerFooterPacket;
    }

    public static void showHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter headerFooterPacket = getHeaderFooterPacket(header, footer);
        PacketHelper.sendPacket(player, headerFooterPacket);
    }

    public static void clearHeaderFooter(Player player) {
        showHeaderFooter(player, "", "");
    }

}
