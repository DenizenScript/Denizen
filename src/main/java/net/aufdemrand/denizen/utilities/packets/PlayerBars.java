package net.aufdemrand.denizen.utilities.packets;

// NMS/CB imports start
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.Packet;
import net.minecraft.server.v1_7_R3.PacketPlayOutExperience;
import net.minecraft.server.v1_7_R3.PacketPlayOutUpdateHealth;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
// NMS/CB imports end

import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PlayerBars {

    public static void sendPacket(Player player, Packet packet) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(packet);
    }

    public static Field getField(Class<?> cl, String field_name) {
        try {
            Field field = cl.getDeclaredField(field_name);
            return field;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PacketPlayOutExperience getExperiencePacket(float exp, int level) {
        PacketPlayOutExperience experiencePacket = new PacketPlayOutExperience();
        try {
            Field a = getField(experiencePacket.getClass(), "a"); // Current experience (between 0 and 1)
            a.setAccessible(true);
            a.set(experiencePacket, exp);
            // Field b = getField(experiencePacket.getClass(), "b"); // Total experience
            // b.setAccessible(true);
            // b.set(experiencePacket, totalExp);
            Field c = getField(experiencePacket.getClass(), "c"); // Experience level
            c.setAccessible(true);
            c.set(experiencePacket, level);
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        return experiencePacket;
    }

    public static PacketPlayOutUpdateHealth getHealthPacket(float health, int food, float food_saturation) {
        PacketPlayOutUpdateHealth healthPacket = new PacketPlayOutUpdateHealth();
        try {
            Field a = getField(healthPacket.getClass(), "a"); // Health (0-20)
            a.setAccessible(true);
            a.set(healthPacket, health);
            Field b = getField(healthPacket.getClass(), "b"); // Food (0-20)
            b.setAccessible(true);
            b.set(healthPacket, food);
            Field c = getField(healthPacket.getClass(), "c"); // Food saturation (0.0-5.0)
            c.setAccessible(true);
            c.set(healthPacket, food_saturation);

        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        return healthPacket;
    }

    public static void showHealth(Player player, float health, int food, float food_saturation) {
        PacketPlayOutUpdateHealth healthPacket = getHealthPacket(health, food, food_saturation);
        sendPacket(player, healthPacket);
    }

    public static void showExperience(Player player, float experience, int level) {
        PacketPlayOutExperience experiencePacket = getExperiencePacket(experience, level);
        sendPacket(player, experiencePacket);
    }

    public static void resetExperience(Player player) {
        PacketPlayOutExperience experiencePacket = getExperiencePacket(player.getExp(), player.getLevel());
        sendPacket(player, experiencePacket);
    }

    public static void resetHealth(Player player) {
        PacketPlayOutUpdateHealth healthPacket = getHealthPacket((float) player.getHealth(),
                player.getFoodLevel(), player.getSaturation());
        sendPacket(player, healthPacket);
    }

}
