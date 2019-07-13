package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.Parrot;

public class ParrotHelper {

    public static String parrotColor(EntityTag colored) {
        return ((Parrot) colored.getBukkitEntity()).getVariant().name();
    }

    public static void setParrotColor(EntityTag colored, Mechanism mechanism) {
        if (mechanism.getValue().matchesEnum(Parrot.Variant.values())) {
            ((Parrot) colored.getBukkitEntity())
                    .setVariant(Parrot.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
