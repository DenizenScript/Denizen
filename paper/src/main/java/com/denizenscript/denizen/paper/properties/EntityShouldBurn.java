package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Zombie;

public class EntityShouldBurn extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name should_burn
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // If the entity is a Zombie, controls whether it should burn in daylight.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Zombie;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Zombie.class).shouldBurnInDay());
    }

    @Override
    public String getPropertyId() {
        return "should_burn";
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Zombie.class).setShouldBurnInDay(param.asBoolean());
        }
    }

    public static void register() {
        autoRegister("should_burn", EntityShouldBurn.class, ElementTag.class, false);
    }
}
