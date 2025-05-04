package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.ChestedHorse;

public class EntityChestCarrier extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name carries_chest
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a horse-like entity is carrying a chest.
    // -->

    public static boolean describes(EntityTag entity) {
          return entity.getBukkitEntity() instanceof ChestedHorse;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(ChestedHorse.class).isCarryingChest());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(ChestedHorse.class).setCarryingChest(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "carries_chest";
    }

    public static void register() {
        autoRegister("carries_chest", EntityChestCarrier.class, ElementTag.class, false);
    }
}
