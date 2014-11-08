package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;

public class EntityRotation implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a Villager, the only EntityType that can be a Professional
        return ((dEntity) entity).getEntityType() == EntityType.PAINTING
                || ((dEntity) entity).getEntityType() == EntityType.ITEM_FRAME;
    }

    public static EntityRotation getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityRotation((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityRotation(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    private BlockFace getRotation() {
        return ((Hanging) entity.getBukkitEntity()).getAttachedFace().getOppositeFace();
    }

    public void setRotation(BlockFace direction) {
        ((Hanging) entity.getBukkitEntity()).setFacingDirection(direction, true);
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getRotation().name().toLowerCase();
    }

    @Override
    public String getPropertyId() {
        return "rotation";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.rotation>
        // @returns Element
        // @mechanism dEntity.rotiation
        // @group properties
        // @description
        // If the entity can have a rotation, returns the entity's rotation.
        // Currently, only Hanging-type entities can have professions.
        // -->
        if (attribute.startsWith("rotation"))
            return new Element(getRotation().name().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name rotation
        // @input Element
        // @description
        // Changes the entity's rotation.
        // Currently, only Hanging-type entities can have professions.
        // @tags
        // <e@entity.rotation>
        // -->

        if (mechanism.matches("rotation") && mechanism.requireEnum(false, BlockFace.values())) {
            setRotation(BlockFace.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
