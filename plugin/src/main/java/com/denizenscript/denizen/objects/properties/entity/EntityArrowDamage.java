package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityArrowDamage implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityArrowDamage getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityArrowDamage((dEntity) entity);
    }

    public static final String[] handledTags = {
            "damage"
    };

    public static final String[] handledMechs = {
            "damage"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityArrowDamage(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(NMSHandler.getInstance().getEntityHelper().getArrowDamage(dentity.getBukkitEntity()));
    }

    @Override
    public String getPropertyId() {
        return "damage";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.damage>
        // @returns ElementTag(Decimal)
        // @mechanism dEntity.damage
        // @group properties
        // @description
        // Returns the damage that the arrow/trident will inflict.
        // NOTE: The actual damage dealt by the arrow/trident may be different depending on the projectile's flight speed.
        // -->
        if (attribute.startsWith("damage")) {
            return new ElementTag(NMSHandler.getInstance().getEntityHelper().getArrowDamage(dentity.getBukkitEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name damage
        // @input Element(Decimal)
        // @description
        // Changes how much damage an arrow/trident will inflict.
        // @tags
        // <e@entity.damage>
        // -->

        if (mechanism.matches("damage") && mechanism.requireDouble()) {
            NMSHandler.getInstance().getEntityHelper().setArrowDamage(dentity.getBukkitEntity(), mechanism.getValue().asDouble());
        }
    }
}
