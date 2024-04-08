package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.World;
import org.bukkit.entity.*;

public class MultiVersionHelper1_19 {

    public static boolean colorIsApplicable(EntityType type) {
        return type == EntityType.FROG || type == EntityType.BOAT || type == EntityType.CHEST_BOAT;
    }

    public static String getColor(Entity entity) {
        if (entity instanceof Frog frog) {
            return frog.getVariant().name();
        }
        else if (entity instanceof Boat boat) {
            return boat.getBoatType().name();
        }
        return null;
    }

    public static ListTag getAllowedColors(EntityType type) {
        if (type == EntityType.FROG) {
            return EntityColor.listForEnum(Frog.Variant.values());
        }
        else if (type == EntityType.BOAT || type == EntityType.CHEST_BOAT) {
            return EntityColor.listForEnum(Boat.Type.values());
        }
        return null;
    }

    public static void setColor(Entity entity, Mechanism mech) {
        if (entity instanceof Frog frog && mech.requireEnum(Frog.Variant.class)) {
            frog.setVariant(mech.getValue().asEnum(Frog.Variant.class));
        }
        else if (entity instanceof Boat boat && mech.requireEnum(Boat.Type.class)) {
            boat.setBoatType(mech.getValue().asEnum(Boat.Type.class));
        }
    }

    public static MapTag interactionToMap(Interaction.PreviousInteraction interaction, World world) {
        if (interaction == null) {
            return null;
        }
        MapTag result = new MapTag();
        result.putObject("player", new PlayerTag(interaction.getPlayer()));
        result.putObject("duration", new DurationTag((world.getGameTime() - interaction.getTimestamp()) / 20d));
        result.putObject("raw_game_time", new ElementTag(interaction.getTimestamp()));
        return result;
    }

    public static ElementTag getWardenAngerLevel(Warden warden) {
        return new ElementTag(warden.getAngerLevel());
    }
}
