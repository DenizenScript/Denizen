package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Llama;

public class EntityStrength implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Llama;
    }

    public static EntityStrength getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityStrength((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "strength"
    };

    public EntityStrength(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public ElementTag getStrength() {
        return new ElementTag(((Llama) entity.getBukkitEntity()).getStrength());
    }

    @Override
    public String getPropertyString() {
        return getStrength().asString();
    }

    @Override
    public String getPropertyId() {
        return "strength";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.strength>
        // @returns ElementTag
        // @mechanism EntityTag.strength
        // @group properties
        // @description
        // Returns the strength of a Llama. A llama's inventory contains (strength times three) slots.
        // Can be from 1 to 5 (inclusive).
        // -->
        PropertyParser.registerTag(EntityStrength.class, ElementTag.class, "strength", (attribute, object) -> {
            return object.getStrength();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name strength
        // @input ElementTag(Boolean)
        // @description
        // Sets the strength of a Llama. A llama's inventory contains (strength times three) slots.
        // Can be from 1 to 5 (inclusive).
        // @tags
        // <EntityTag.strength>
        // -->
        if (mechanism.matches("strength") && mechanism.requireInteger()) {
            ((Llama) entity.getBukkitEntity()).setStrength(mechanism.getValue().asInt());
        }
    }
}
