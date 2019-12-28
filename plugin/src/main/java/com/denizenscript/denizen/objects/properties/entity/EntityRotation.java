package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;

public class EntityRotation implements Property {


    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) entity).getBukkitEntityType() == EntityType.PAINTING
                || ((EntityTag) entity).getBukkitEntityType() == EntityType.ITEM_FRAME;
    }

    public static EntityRotation getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityRotation((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "rotation"
    };

    public static final String[] handledMechs = new String[] {
            "rotation"
    };

    private EntityRotation(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    private BlockFace getRotation() {
        return ((Hanging) entity.getBukkitEntity()).getAttachedFace().getOppositeFace();
    }

    public void setRotation(BlockFace direction) {
        ((Hanging) entity.getBukkitEntity()).setFacingDirection(direction, true);
    }

    @Override
    public String getPropertyString() {
        return CoreUtilities.toLowerCase(getRotation().name());
    }

    @Override
    public String getPropertyId() {
        return "rotation";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.rotation_vector>
        // @returns LocationTag
        // @mechanism EntityTag.rotation
        // @group properties
        // @description
        // If the entity can have a rotation, returns the entity's rotation as a direction vector.
        // Currently, only Hanging-type entities can have rotations.
        // -->
        if (attribute.startsWith("rotation_vector")) {
            return new LocationTag(getRotation().getDirection())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.rotation>
        // @returns ElementTag
        // @mechanism EntityTag.rotation
        // @group properties
        // @description
        // If the entity can have a rotation, returns the entity's rotation.
        // Currently, only Hanging-type entities can have rotations.
        // -->
        if (attribute.startsWith("rotation")) {
            return new ElementTag(CoreUtilities.toLowerCase(getRotation().name()))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name rotation
        // @input Element
        // @description
        // Changes the entity's rotation.
        // Currently, only Hanging-type entities can have rotations.
        // @tags
        // <EntityTag.rotation>
        // <EntityTag.rotation_vector>
        // -->

        if (mechanism.matches("rotation") && mechanism.requireEnum(false, BlockFace.values())) {
            setRotation(BlockFace.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
