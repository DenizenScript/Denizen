package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;

public class ColorHelper1_17 {

    public static boolean colorIsApplicable(EntityType type) {
        return type == EntityType.GOAT || type == EntityType.AXOLOTL;
    }

    public static String getColor(Entity entity) {
        if (entity instanceof Goat) {
            return ((Goat) entity).isScreaming() ? "screaming" : "normal";
        }
        else if (entity instanceof Axolotl) {
            return ((Axolotl) entity).getVariant().name();
        }
        return null;
    }

    public static ListTag getAllowedColors(EntityType type) {
        if (type == EntityType.GOAT) {
            ListTag result = new ListTag();
            result.add("screaming");
            result.add("normal");
            return result;
        }
        else if (type == EntityType.AXOLOTL) {
            return EntityColor.listForEnum(Axolotl.Variant.values());
        }
        return null;
    }

    public static void setColor(Entity entity, Mechanism mech) {
        if (entity instanceof Goat) {
            ((Goat) entity).setScreaming(CoreUtilities.toLowerCase(mech.getValue().asString()).equals("screaming"));
        }
        else if (entity instanceof Axolotl && mech.requireEnum(false, Axolotl.Variant.values())) {
            ((Axolotl) entity).setVariant(Axolotl.Variant.valueOf(mech.getValue().asString().toUpperCase()));
        }
    }
}
