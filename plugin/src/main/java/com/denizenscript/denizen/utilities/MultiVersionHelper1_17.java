package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizen.objects.properties.material.MaterialBlockType;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;

public class MultiVersionHelper1_17 { // TODO: 1.17

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

    public static void materialBlockTypeRunMech(Mechanism mechanism, MaterialBlockType object) {
        if (object.isDripstone() && mechanism.requireEnum(false, PointedDripstone.Thickness.values())) {
            ((PointedDripstone) object.material.getModernData()).setThickness(PointedDripstone.Thickness.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
        else if (object.isCaveVines()) {
            ((CaveVinesPlant) object.material.getModernData()).setBerries(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "berries"));
        }
    }
}
