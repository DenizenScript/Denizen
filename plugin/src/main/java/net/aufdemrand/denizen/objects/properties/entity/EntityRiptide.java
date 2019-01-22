package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class EntityRiptide implements Property {
    public static boolean describes(dObject object) {
        return object instanceof dEntity && ((dEntity) object).isLivingEntity();
    }

    public static EntityRiptide getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityRiptide((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[]{
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
        // @returns Element(Boolean)
        // @mechanism dEntity.is_using_riptide
        // @group properties
        // @description
        // Returns whether this entity is using the Riptide enchantment.
        // -->
        if (attribute.startsWith("is_using_riptide")) {
            return new Element(entity.getLivingEntity().isRiptiding())
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
