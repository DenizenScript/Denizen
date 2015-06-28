package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R3.PacketPlayOutCamera;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class PlayerSpectateEntity {

    private static final Field entity_id;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutCamera.class);
        entity_id = fields.get("a");
    }

    public static PacketPlayOutCamera getCameraPacket(Entity entity) {
        PacketPlayOutCamera cameraPacket = new PacketPlayOutCamera();
        try {
            entity_id.set(cameraPacket, entity.getEntityId());
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return cameraPacket;
    }

    public static void setSpectating(Player player, Entity entity) {
        PacketPlayOutCamera cameraPacket = getCameraPacket(entity);
        PacketHelper.sendPacket(player, cameraPacket);
    }
}
