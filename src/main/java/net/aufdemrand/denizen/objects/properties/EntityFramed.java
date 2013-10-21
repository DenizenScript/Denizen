package net.aufdemrand.denizen.objects.properties;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.Rotation;
import org.bukkit.entity.*;

public class EntityFramed implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        return ((dEntity) entity).getEntityType() == EntityType.ITEM_FRAME;
    }

    public static EntityFramed getFrom(dObject entity) {
        if (!describes(entity)) return null;
        else return new EntityFramed((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityFramed(dEntity item) {
        item_frame = item;
    }

    dEntity item_frame;

    public boolean hasItem() {
        return getItemFrameEntity().getItem() != null;
    }

    public ItemFrame getItemFrameEntity() {
        return (ItemFrame) item_frame.getBukkitEntity();
    }

    public void setItem(dItem item) {
        getItemFrameEntity().setItem(item.getItemStack());
    }

    public dItem getItem() {
        return new dItem(getItemFrameEntity().getItem());
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getPropertyId() + '=' + getItem().getMaterial().identify()
                + (getItemFrameEntity().getRotation() == Rotation.NONE ? ';'
                : '|' + getItemFrameEntity().getRotation().name().toLowerCase() + ';');
    }

    @Override
    public String getPropertyId() {
        return "framed";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.framed_item>
        // @returns dItem
        // @description
        // returns the material currently framed if the entity is an item frame.
        // -->
        if (attribute.startsWith("framed_item"))
            return getItem()
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.has_framed_item>
        // @returns Element(Boolean)
        // @description
        // returns true if the entity is an item frame that has an item.
        // -->
        if (attribute.startsWith("has_framed_item"))
            return new Element(hasItem())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.framed_item_rotation>
        // @returns Element
        // @description
        // returns the rotation of the material currently framed if the entity is an item frame.
        // -->
        if (attribute.startsWith("framed_item_rotation"))
            return new Element(getItemFrameEntity().getRotation().name().toLowerCase())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}
