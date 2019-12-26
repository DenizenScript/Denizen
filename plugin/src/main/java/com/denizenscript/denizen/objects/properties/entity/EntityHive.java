package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.Beehive;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;

public class EntityHive implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.BEE;
    }

    public static EntityHive getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHive((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "hive", "has_hive"
    };

    public static final String[] handledMechs = new String[] {
            "hive"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityHive(EntityTag entity) {
        bee = entity;
    }

    EntityTag bee;

    /////////
    // Property Methods
    ///////

    public boolean hasHive() {
        if (((Bee) bee.getBukkitEntity()).getHive() != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isHive(LocationTag location) {
        if (location.getBlockState() instanceof Beehive) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String getPropertyString() {
        if (hasHive()) {
            return new LocationTag(((Bee) bee.getBukkitEntity()).getHive()).toString();
        } else {
            return "NONE";
        }
    }

    @Override
    public String getPropertyId() {
        return "hive";
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
        // @attribute <EntityTag.hive>
        // @returns LocationTag
        // @mechanism EntityTag.hive
        // @group properties
        // @description
        // If the entity is a Bee, returns it's hive location.
        // -->
        if (attribute.startsWith("hive")) {
            return new LocationTag(((Bee) bee.getBukkitEntity()).getHive())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.has_hive>
        // @returns ElementTag(boolean)
        // @group properties
        // @description
        // Returns whether or not this Bee has a hive.
        // -->
        if (attribute.startsWith("hive")) {
            return new ElementTag(hasHive())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name hive
        // @input LocationTag
        // @description
        // Sets the Hive location of a Bee
        // @tags
        // <EntityTag.hive>
        // -->

        if (mechanism.matches("hive")
                && mechanism.requireObject(LocationTag.class)
                && isHive(mechanism.valueAsType(LocationTag.class))) {
            ((Bee) bee.getBukkitEntity()).setHive(mechanism.valueAsType(LocationTag.class));
        }
    }
}
