package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityRiptide implements Property {
    public static boolean describes(ObjectTag object) {
        return object instanceof dEntity && ((dEntity) object).isLivingEntity();
    }

    public static EntityRiptide getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityRiptide((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_using_riptide"
    };

    public static final String[] handledMechs = new String[] {
            "is_using_riptide"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityRiptide(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return entity.getLivingEntity().isRiptiding() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "is_using_riptide";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.is_using_riptide>
        // @returns ElementTag(Boolean)
        // @mechanism dEntity.is_using_riptide
        // @group properties
        // @description
        // Returns whether this entity is using the Riptide enchantment.
        // -->
        if (attribute.startsWith("is_using_riptide")) {
            return new ElementTag(entity.getLivingEntity().isRiptiding())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name is_using_riptide
        // @input Element(Boolean)
        // @description
        // Sets whether this entity is using the Riptide enchantment.
        // @tags
        // <e@entity.is_using_riptide>
        // -->
        if (mechanism.matches("is_using_riptide") && mechanism.requireBoolean()) {
            NMSHandler.getInstance().getEntityHelper().setRiptide(entity.getBukkitEntity(), mechanism.getValue().asBoolean());
        }
    }
}
