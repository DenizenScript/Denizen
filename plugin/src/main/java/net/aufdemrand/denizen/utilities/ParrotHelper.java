package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Mechanism;
import org.bukkit.entity.Parrot;

public class ParrotHelper {

    public static String parrotColor(dEntity colored) {
        return ((Parrot) colored.getBukkitEntity()).getVariant().name();
    }

    public static void setParrotColor(dEntity colored, Mechanism mechanism) {
        if (mechanism.getValue().matchesEnum(Parrot.Variant.values())) {
            ((Parrot) colored.getBukkitEntity())
                    .setVariant(Parrot.Variant.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
