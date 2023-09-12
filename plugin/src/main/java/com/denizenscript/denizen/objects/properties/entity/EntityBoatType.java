package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.Boat;

public class EntityBoatType extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name boat_type
    // @input ElementTag
    // @description
    // Controls the wood type of the boat.
    // Valid wood types can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Boat.Type.html>
    // -->

    public static boolean describes(EntityTag boat) {
        return boat.getBukkitEntity() instanceof Boat;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getBoat().getBoatType());
    }

    @Override
    public String getPropertyId() {
        return "boat_type";
    }

    @Override
    public void setPropertyValue(ElementTag type, Mechanism mechanism) {
        if (!mechanism.requireEnum(Boat.Type.class)) {
            return;
        }
        getBoat().setBoatType(type.asEnum(Boat.Type.class));
    }

    public Boat getBoat() {
        return (Boat) getEntity();
    }

    public static void register() {
        autoRegister("boat_type", EntityBoatType.class, ElementTag.class, false);
    }
}
