package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityChestCarrier implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && NMSHandler.getEntityHelper().isChestedHorse(((EntityTag) entity).getBukkitEntity());
    }

    public static EntityChestCarrier getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityChestCarrier((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "carries_chest"
    };

    public static final String[] handledMechs = new String[] {
            "carries_chest"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityChestCarrier(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return NMSHandler.getEntityHelper().isCarryingChest(entity.getBukkitEntity()) ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "carries_chest";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.carries_chest>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.carries_chest
        // @group properties
        // @description
        // If the entity is a horse, returns whether it is carrying a chest.
        // -->
        if (attribute.startsWith("carries_chest")) {
            return new ElementTag(NMSHandler.getEntityHelper().isCarryingChest(entity.getBukkitEntity()))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name carries_chest
        // @input Element(Boolean)
        // @description
        // Changes whether a Horse carries a chest.
        // @tags
        // <EntityTag.carries_chest>
        // -->

        if (mechanism.matches("carries_chest") && mechanism.requireBoolean()) {
            NMSHandler.getEntityHelper().setCarryingChest(entity.getBukkitEntity(), mechanism.getValue().asBoolean());
        }
    }
}
