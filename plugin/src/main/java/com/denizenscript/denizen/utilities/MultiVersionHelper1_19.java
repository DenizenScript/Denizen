package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.World;
import org.bukkit.entity.*;

public class MultiVersionHelper1_19 {

    public static boolean colorIsApplicable(EntityType type) {
        return type == EntityType.FROG || Boat.class.isAssignableFrom(type.getEntityClass());
    }

    // TODO Frog variants technically have registries on all supported versions
    public static String getColor(Entity entity) {
        if (entity instanceof Frog frog) {
            return String.valueOf(frog.getVariant());
        }
        else if (entity instanceof Boat boat) {
            return boat.getBoatType().name();
        }
        return null;
    }

    public static ListTag getAllowedColors(EntityType type) {
        if (type == EntityType.FROG) {
            return Utilities.listTypes(Frog.Variant.class);
        }
        else if (Boat.class.isAssignableFrom(type.getEntityClass())) {
            return Utilities.listTypes(Boat.Type.class);
        }
        return null;
    }

    public static void setColor(Entity entity, Mechanism mech) {
        if (entity instanceof Frog frog && Utilities.requireEnumlike(mech, Frog.Variant.class)) {
            frog.setVariant(Utilities.elementToEnumlike(mech.getValue(), Frog.Variant.class));
        }
        else if (entity instanceof Boat boat && mech.requireEnum(Boat.Type.class)) {
            // TODO: 1.21.3: Deprecate setting boat types
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
