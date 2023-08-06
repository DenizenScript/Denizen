package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ChestedHorse;

public class EntityChestCarrier implements Property {

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

        // <--[tag]
        // @attribute <EntityTag.carries_chest>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.carries_chest
        // @group properties
        // @description
        // Returns whether a horse-like entity is carrying a chest.
        // -->
        PropertyParser.registerTag(EntityChestCarrier.class, ElementTag.class, "carries_chest", (attribute, object) -> {
            return new ElementTag(object.getChestedHorse().isCarryingChest());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name carries_chest
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a horse-like entity is carrying a chest.
        // @tags
        // <EntityTag.carries_chest>
        // -->
        if (mechanism.matches("carries_chest") && mechanism.requireBoolean()) {
            getChestedHorse().setCarryingChest(mechanism.getValue().asBoolean());
        }
    }
}
