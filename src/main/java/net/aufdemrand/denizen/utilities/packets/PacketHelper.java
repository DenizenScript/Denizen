package net.aufdemrand.denizen.utilities.packets;

import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.Packet;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PacketHelper {

    private final static Map<Class<?>, Map<String, Field>> classFields = new HashMap<Class<?>, Map<String, Field>>();

    public static void sendPacket(Player player, Packet packet) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(packet);
    }

    public static Map<String, Field> registerFields(Class<?> cl) {
        if (classFields.containsKey(cl)) {
            return classFields.get(cl);
        }
        Map<String, Field> fields = new HashMap<String, Field>();
        for (Field field : cl.getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
        classFields.put(cl, fields);
        return fields;
    }

    public static Map<String, Field> getFields(Class<?> cl) {
        if (classFields.containsKey(cl)) {
            return classFields.get(cl);
        }
        return null;
    }
}
