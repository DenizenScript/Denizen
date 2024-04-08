package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Zombie;

public class EntityCanBreakDoors extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name can_break_doors
    // @input ElementTag(Boolean)
    // @description
    // Whether a zombie can break doors.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Zombie;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Zombie.class).canBreakDoors());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Zombie.class).setCanBreakDoors(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "can_break_doors";
    }

    public static void register() {
        autoRegister("can_break_doors", EntityCanBreakDoors.class, ElementTag.class, false);
    }
}
