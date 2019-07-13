package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityMarker implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityMarker getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityMarker((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "marker"
    };

    public static final String[] handledMechs = new String[] {
            "marker"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityMarker(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!((ArmorStand) dentity.getBukkitEntity()).isMarker()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "marker";
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
        // @attribute <e@entity.marker>
        // @returns Element(Boolean)
        // @mechanism dEntity.marker
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand is a marker.
        // -->
        if (attribute.startsWith("marker")) {
            return new Element(((ArmorStand) dentity.getBukkitEntity()).isMarker())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name marker
        // @input Element(Boolean)
        // @description
        // Changes the marker state of an armor stand.
        // @tags
        // <e@entity.marker>
        // -->

        if (mechanism.matches("marker") && mechanism.requireBoolean()) {
            ((ArmorStand) dentity.getBukkitEntity()).setMarker(mechanism.getValue().asBoolean());
        }
    }
}
