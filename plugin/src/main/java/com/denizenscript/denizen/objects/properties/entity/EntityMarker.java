package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityMarker implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityMarker getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityMarker((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "marker"
    };

    public static final String[] handledMechs = new String[] {
            "marker"
    };

    public EntityMarker(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.marker>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.marker
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand is a marker.
        // Marker armor stands have a tiny hitbox.
        // -->
        if (attribute.startsWith("marker")) {
            return new ElementTag(((ArmorStand) dentity.getBukkitEntity()).isMarker())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name marker
        // @input ElementTag(Boolean)
        // @description
        // Changes the marker state of an armor stand.
        // @tags
        // <EntityTag.marker>
        // -->
        if (mechanism.matches("marker") && mechanism.requireBoolean()) {
            ((ArmorStand) dentity.getBukkitEntity()).setMarker(mechanism.getValue().asBoolean());
        }
    }
}
