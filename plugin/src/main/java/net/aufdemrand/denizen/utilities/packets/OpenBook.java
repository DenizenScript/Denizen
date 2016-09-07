package net.aufdemrand.denizen.utilities.packets;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_10_R1.EnumHand;
import net.minecraft.server.v1_10_R1.PacketDataSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutCustomPayload;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class OpenBook {

    private static final Field channel, packet_data;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutCustomPayload.class);
        channel = fields.get("a");
        packet_data = fields.get("b");
    }

    public static PacketPlayOutCustomPayload getOpenBookPacket(boolean offHand) {
        PacketPlayOutCustomPayload customPayloadPacket = new PacketPlayOutCustomPayload();
        try {
            channel.set(customPayloadPacket, "MC|BOpen");
            PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
            serializer.a(offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            packet_data.set(customPayloadPacket, serializer);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return customPayloadPacket;
    }

    public static void openBook(Player player, boolean offHand) {
        PacketPlayOutCustomPayload customPayloadPacket = getOpenBookPacket(offHand);
        PacketHelper.sendPacket(player, customPayloadPacket);
    }

}
