package net.aufdemrand.denizen.utilities.packets;

import net.minecraft.server.v1_8_R2.PacketPlayOutExperience;
import net.minecraft.server.v1_8_R2.PacketPlayOutUpdateHealth;

import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class PlayerBars {

    private static final Field xp_current, xp_total, xp_level;
    private static final Field health_health, health_food, health_saturation;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutExperience.class);
        xp_current = fields.get("a"); // TODO: Are these accurate (1.8.3)?
        xp_total = fields.get("b");
        xp_level = fields.get("c");

        fields = PacketHelper.registerFields(PacketPlayOutUpdateHealth.class);
        health_health = fields.get("a");
        health_food = fields.get("b");
        health_saturation = fields.get("c");
    }

    public static PacketPlayOutExperience getExperiencePacket(float exp, int level) {
        PacketPlayOutExperience experiencePacket = new PacketPlayOutExperience();
        try {
            xp_current.set(experiencePacket, exp);
            // xp_total.set(experiencePacket, totalExp);
            xp_level.set(experiencePacket, level);
        } catch (Exception e) {
            dB.echoError(e);
        }
        return experiencePacket;
    }

    public static PacketPlayOutUpdateHealth getHealthPacket(float health, int food, float food_saturation) {
        PacketPlayOutUpdateHealth healthPacket = new PacketPlayOutUpdateHealth();
        try {
            health_health.set(healthPacket, health);
            health_food.set(healthPacket, food);
            health_saturation.set(healthPacket, food_saturation);

        } catch (Exception e) {
            dB.echoError(e);
        }
        return healthPacket;
    }

    public static void showHealth(Player player, float health, int food, float food_saturation) {
        PacketPlayOutUpdateHealth healthPacket = getHealthPacket(health, food, food_saturation);
        PacketHelper.sendPacket(player, healthPacket);
    }

    public static void showExperience(Player player, float experience, int level) {
        PacketPlayOutExperience experiencePacket = getExperiencePacket(experience, level);
        PacketHelper.sendPacket(player, experiencePacket);
    }

    public static void resetExperience(Player player) {
        PacketPlayOutExperience experiencePacket = getExperiencePacket(player.getExp(), player.getLevel());
        PacketHelper.sendPacket(player, experiencePacket);
    }

    public static void resetHealth(Player player) {
        PacketPlayOutUpdateHealth healthPacket = getHealthPacket((float) player.getHealth(),
                player.getFoodLevel(), player.getSaturation());
        PacketHelper.sendPacket(player, healthPacket);
    }
}
