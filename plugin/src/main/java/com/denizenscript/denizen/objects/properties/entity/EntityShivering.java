package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Strider;

public class EntityShivering implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Strider;
    }

    public static EntityShivering getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityShivering((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "shivering"
    };

    public EntityShivering(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.shivering>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.shivering
        // @group properties
        // @description
        // Returns whether the strider is shivering.
        // -->
        PropertyParser.registerTag(EntityShivering.class, ElementTag.class, "shivering", (attribute, object) -> {
            return new ElementTag(object.getStrider().isShivering());
        });
    }

    public Strider getStrider() {
        return (Strider) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getStrider().isShivering());
    }

    @Override
    public String getPropertyId() {
        return "shivering";
    }


    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name shivering
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the strider is shivering.
        // @tags
        // <EntityTag.shivering>
        // -->
        if (mechanism.matches("shivering") && mechanism.requireBoolean()) {
            getStrider().setShivering(mechanism.getValue().asBoolean());
        }
    }
}
