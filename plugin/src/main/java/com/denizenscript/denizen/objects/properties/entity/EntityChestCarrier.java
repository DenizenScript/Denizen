package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ChestedHorse;

public class EntityChestCarrier implements Property {

    // <--[property]
    // @object EntityTag
    // @name carries_chest
    // @input ElementTag(Boolean)
    // @description
    // Returns whether a horse-like entity is carrying a chest.
    // -->

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ChestedHorse;
    }

    public static EntityChestCarrier getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityChestCarrier((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "carries_chest"
    };

    public EntityChestCarrier(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public void adjust(Mechanism mechanism) {
        getChestedHorse().setCarryingChest(mechanism.getValue().asBoolean());
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getChestedHorse().isCarryingChest());
    }

    @Override
    public String getPropertyId() {
        return "carries_chest";
    }

    public ChestedHorse getChestedHorse() {
        return (ChestedHorse) entity.getBukkitEntity();
    }

    public static void register() {
        autoRegister("carries_chest", EntityChestCarrier.class, ElementTag.class, false);
    }
}
