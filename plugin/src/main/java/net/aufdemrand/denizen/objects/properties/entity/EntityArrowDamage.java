package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityArrowDamage implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityArrowDamage getFrom(dObject entity) {
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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.damage>
        // @returns Element(Decimal)
        // @mechanism dEntity.damage
        // @group properties
        // @description
        // Returns the damage that the arrow/trident will inflict.
        // NOTE: The actual damage dealt by the arrow/trident may be different depending on the projectile's flight speed.
        // -->
        if (attribute.startsWith("damage")) {
            return new Element(NMSHandler.getInstance().getEntityHelper().getArrowDamage(dentity.getBukkitEntity()))
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
