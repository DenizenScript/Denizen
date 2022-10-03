package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ItemFrame;

public class EntityFixed implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof ItemFrame;
    }

    public static EntityFixed getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFixed((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "fixed"
    };

    public static final String[] handledMechs = new String[] {
            "fixed"
    };

    private EntityFixed(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return ((ItemFrame) entity.getBukkitEntity()).isFixed() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "fixed";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.fixed>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.fixed
        // @group attributes
        // @description
        // Returns whether the item frame is fixed. (Meaning, it can't be altered by players or broken by block obstructions).
        // -->
        if (attribute.startsWith("fixed")) {
            return new ElementTag(((ItemFrame) entity.getBukkitEntity()).isFixed())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name fixed
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this item frame is fixed. (Meaning, it can't be altered by players or broken by block obstructions).
        // @tags
        // <EntityTag.fixed>
        // -->
        if (mechanism.matches("fixed") && mechanism.requireBoolean()) {
            ((ItemFrame) entity.getBukkitEntity()).setFixed(mechanism.getValue().asBoolean());
        }
    }
}
