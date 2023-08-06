package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityRiptide implements Property {
    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag && ((EntityTag) object).isLivingEntity();
    }

    public static EntityRiptide getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityRiptide((EntityTag) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_using_riptide"
    };

    public static final String[] handledMechs = new String[] {
            "is_using_riptide"
    };

    public EntityRiptide(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return entity.getLivingEntity().isRiptiding() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "is_using_riptide";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.is_using_riptide>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.is_using_riptide
        // @group properties
        // @description
        // Returns whether this entity is using the Riptide enchantment.
        // -->
        if (attribute.startsWith("is_using_riptide")) {
            return new ElementTag(entity.getLivingEntity().isRiptiding())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name is_using_riptide
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this entity is using the Riptide enchantment.
        // @tags
        // <EntityTag.is_using_riptide>
        // -->
        if (mechanism.matches("is_using_riptide") && mechanism.requireBoolean()) {
            boolean shouldRiptide = mechanism.getValue().asBoolean();
            if (shouldRiptide != entity.getLivingEntity().isRiptiding()) {
                NMSHandler.entityHelper.setRiptide(entity.getBukkitEntity(), shouldRiptide);
            }
        }
    }
}
