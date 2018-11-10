package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

public class EntityIsShowingBottom implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ENDER_CRYSTAL;
    }

    public static EntityIsShowingBottom getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityIsShowingBottom((dEntity) entity);
        }
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityIsShowingBottom(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (((EnderCrystal) dentity.getBukkitEntity()).isShowingBottom()) {
            return null;
        }
        else {
            return "false";
        }
    }

    @Override
    public String getPropertyId() {
        return "is_showing_bottom";
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
        // @attribute <e@entity.is_showing_bottom>
        // @returns Element(Boolean)
        // @mechanism dEntity.is_showing_bottom
        // @group properties
        // @description
        // If the entity is an ender crystal, returns whether the ender crystal has its bottom showing.
        // -->
        if (attribute.startsWith("is_showing_bottom")) {
            return new Element(((EnderCrystal) dentity.getBukkitEntity()).isShowingBottom())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name is_showing_bottom
        // @input Element(Boolean)
        // @description
        // Changes the bottom state of an ender crystal.
        // @tags
        // <e@entity.is_showing_bottom>
        // -->

        if (mechanism.matches("is_showing_bottom") && mechanism.requireBoolean()) {
            ((EnderCrystal) dentity.getBukkitEntity()).setShowingBottom(mechanism.getValue().asBoolean());
        }
    }
}
