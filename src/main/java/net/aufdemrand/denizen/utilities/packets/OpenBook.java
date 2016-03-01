package net.aufdemrand.denizen.utilities.packets;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_9_R1.PacketDataSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayOutCustomPayload;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class OpenBook {

    private static final Field channel, packet_data;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutCustomPayload.class);
        channel = fields.get("a");// TODO: 1.9
        packet_data = fields.get("b");// TODO: 1.9
    }

    public static PacketPlayOutCustomPayload getOpenBookPacket() {
        PacketPlayOutCustomPayload customPayloadPacket = new PacketPlayOutCustomPayload();
        try {
            channel.set(customPayloadPacket, "MC|BOpen");
            packet_data.set(customPayloadPacket, new PacketDataSerializer(Unpooled.buffer()));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return customPayloadPacket;
    }

    public static void openBook(Player player) {
        PacketPlayOutCustomPayload customPayloadPacket = getOpenBookPacket();
        PacketHelper.sendPacket(player, customPayloadPacket);
    }

}
