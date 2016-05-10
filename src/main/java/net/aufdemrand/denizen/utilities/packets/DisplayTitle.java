package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_9_R2.ChatComponentText;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class DisplayTitle {

    private static final Field title_action, chat_component, fade_in_ticks, stay_ticks, fade_out_ticks;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutTitle.class);
        title_action = fields.get("a");
        chat_component = fields.get("b");
        fade_in_ticks = fields.get("c");
        stay_ticks = fields.get("d");
        fade_out_ticks = fields.get("e");
    }

    public static PacketPlayOutTitle getTitlePacket(String title) {
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle();
        try {
            title_action.set(titlePacket, PacketPlayOutTitle.EnumTitleAction.TITLE);
            chat_component.set(titlePacket, new ChatComponentText(title));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return titlePacket;
    }

    public static PacketPlayOutTitle getSubtitlePacket(String subtitle) {
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle();
        try {
            title_action.set(titlePacket, PacketPlayOutTitle.EnumTitleAction.SUBTITLE);
            chat_component.set(titlePacket, new ChatComponentText(subtitle));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return titlePacket;
    }

    public static PacketPlayOutTitle getTimesPacket(int fade_in, int stay, int fade_out) {
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle();
        try {
            title_action.set(titlePacket, PacketPlayOutTitle.EnumTitleAction.TIMES);
            fade_in_ticks.set(titlePacket, fade_in);
            stay_ticks.set(titlePacket, stay);
            fade_out_ticks.set(titlePacket, fade_out);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return titlePacket;
    }

    public static void showTitle(Player player, String title, String subtitle, int fade_in, int stay, int fade_out) {
        PacketPlayOutTitle titlePacketTimes = getTimesPacket(fade_in, stay, fade_out);
        PacketHelper.sendPacket(player, titlePacketTimes);
        if (title != null) {
            PacketPlayOutTitle titlePacketTitle = getTitlePacket(title);
            PacketHelper.sendPacket(player, titlePacketTitle);
        }
        if (subtitle != null) {
            PacketPlayOutTitle titlePacketSubtitle = getSubtitlePacket(subtitle);
            PacketHelper.sendPacket(player, titlePacketSubtitle);
        }
    }

}
