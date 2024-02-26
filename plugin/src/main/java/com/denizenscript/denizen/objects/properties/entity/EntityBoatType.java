package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;

public class EntityBoatType extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name boat_type
    // @input ElementTag
    // @description
    // Controls the wood type of the boat.
    // Valid wood types can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/TreeSpecies.html>
    // Deprecated in versions 1.19 and above. Use <@link property EntityTag.color>.
    // -->

    public static boolean describes(EntityTag boat) {
        return boat.getBukkitEntity() instanceof Boat;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Boat.class).getWoodType());
    }

    @Override
    public String getPropertyId() {
        return "boat_type";
    }

    @Override
    public void setPropertyValue(ElementTag type, Mechanism mechanism) {
        if (mechanism.requireEnum(TreeSpecies.class)) {
            as(Boat.class).setWoodType(type.asEnum(TreeSpecies.class));
        }
    }

    public static void register() {
        PropertyParser.registerTag(EntityBoatType.class, ElementTag.class, "boat_type", (attribute, object) -> {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                BukkitImplDeprecations.boatType.warn(attribute.context);
            }
            return object.getPropertyValue();
        });

        PropertyParser.registerMechanism(EntityBoatType.class, ElementTag.class, "boat_type", (object, mechanism, type) -> {
           if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
               BukkitImplDeprecations.boatType.warn(mechanism.context);
           }
           object.setPropertyValue(type, mechanism);
        });
    }
}
