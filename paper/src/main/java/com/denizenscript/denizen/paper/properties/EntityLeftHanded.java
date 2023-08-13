package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Mob;

public class EntityLeftHanded extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name left_handed
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // Whether a mob is left-handed. Mobs have a rare chance of spawning left-handed.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Mob;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Mob.class).isLeftHanded());
    }

    @Override
    public String getPropertyId() {
        return "left_handed";
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Mob.class).setLeftHanded(param.asBoolean());
        }
    }

    public static void register() {
        autoRegister("left_handed", EntityLeftHanded.class, ElementTag.class, false);
    }
}
