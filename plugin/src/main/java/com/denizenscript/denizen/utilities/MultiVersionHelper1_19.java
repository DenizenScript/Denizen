package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.*;

public class MultiVersionHelper1_19 {

    public static boolean colorIsApplicable(EntityType type) {
        return type == EntityType.FROG;
    }

    public static String getColor(Entity entity) {
        if (entity instanceof Frog) {
            return ((Frog) entity).getVariant().name();
        }
        return null;
    }

    public static ListTag getAllowedColors(EntityType type) {
        if (type == EntityType.FROG) {
            return EntityColor.listForEnum(Frog.Variant.values());
        }
        return null;
    }

    public static void setColor(Entity entity, Mechanism mech) {
        if (entity instanceof Frog && mech.requireEnum(Frog.Variant.class)) {
            ((Frog) entity).setVariant(Frog.Variant.valueOf(mech.getValue().asString().toUpperCase()));
        }
    }
}
