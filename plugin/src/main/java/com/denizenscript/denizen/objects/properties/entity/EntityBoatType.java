package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;

public class EntityBoatType extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name boat_type
    // @input ElementTag
    // @description
    // Controls the wood type of the boat.
    // Valid wood types can be found here: <@linke url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/TreeSpecies.html>
    // For versions 1.19 and above, valid wood types can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Boat.Type.html>
    // -->

    public static boolean describes(EntityTag boat) {
        return boat.getBukkitEntity() instanceof Boat;
    }

    @Override
    public ElementTag getPropertyValue() {
        return getBoatType();
    }

    @Override
    public String getPropertyId() {
        return "boat_type";
    }

    @Override
    public void setPropertyValue(ElementTag type, Mechanism mechanism) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18) && !mechanism.requireEnum(TreeSpecies.class)) {
            return;
        }
        else if (!mechanism.requireEnum(Boat.Type.class)) {
            return;
        }
        setBoatType(type);
    }

    public ElementTag getBoatType() {
        return NMSHandler.entityHelper.getBoatType(as(Boat.class));
    }

    public void setBoatType(ElementTag type) {
        NMSHandler.entityHelper.setBoatType(as(Boat.class), type);
    }

    public static void register() {
        autoRegister("boat_type", EntityBoatType.class, ElementTag.class, false);
    }
}
