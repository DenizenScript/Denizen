package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityArms implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityArms getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityArms((dEntity) entity);
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityArms(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!((ArmorStand) dentity.getBukkitEntity()).hasArms()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "arms";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.arms>
        // @returns Element(Boolean)
        // @mechanism dEntity.arms
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has arms.
        // -->
        if (attribute.startsWith("arms"))
            return new Element(((ArmorStand) dentity.getBukkitEntity()).hasGravity())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name arms
        // @input Element(Boolean)
        // @description
        // Changes the arms state of an armor stand.
        // @tags
        // <e@entity.arms>
        // -->

        if (mechanism.matches("arms") && mechanism.requireBoolean()) {
            ((ArmorStand) dentity.getBukkitEntity()).setArms(mechanism.getValue().asBoolean());
        }
    }
}
