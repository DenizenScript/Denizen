package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityBasePlate implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityBasePlate getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityBasePlate((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "base_plate"
    };

    public static final String[] handledMechs = new String[] {
            "base_plate"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityBasePlate(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (((ArmorStand) dentity.getBukkitEntity()).hasBasePlate()) {
            return null;
        }
        else {
            return "false";
        }
    }

    @Override
    public String getPropertyId() {
        return "base_plate";
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
        // @attribute <e@entity.base_plate>
        // @returns Element(Boolean)
        // @mechanism dEntity.base_plate
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has a base plate.
        // -->
        if (attribute.startsWith("base_plate")) {
            return new Element(((ArmorStand) dentity.getBukkitEntity()).hasBasePlate())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name base_plate
        // @input Element(Boolean)
        // @description
        // Changes the base plate state of an armor stand.
        // @tags
        // <e@entity.base_plate>
        // -->

        if (mechanism.matches("base_plate") && mechanism.requireBoolean()) {
            ((ArmorStand) dentity.getBukkitEntity()).setBasePlate(mechanism.getValue().asBoolean());
        }
    }
}
