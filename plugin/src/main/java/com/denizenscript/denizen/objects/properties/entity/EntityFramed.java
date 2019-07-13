package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

public class EntityFramed implements Property {

    // TODO: Possibly merge class with EntityItem?
    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ITEM_FRAME;
    }

    public static EntityFramed getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFramed((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "framed_item_rotation", "framed_item", "has_framed_item"
    };

    public static final String[] handledMechs = new String[] {
            "framed"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityFramed(dEntity item) {
        item_frame = item;
    }

    dEntity item_frame;

    public boolean hasItem() {
        return getItemFrameEntity().getItem() != null
                && getItemFrameEntity().getItem().getType() != Material.AIR;
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
        if (hasItem()) {
            return getItem().identify()
                    + (getItemFrameEntity().getRotation() == Rotation.NONE ? ""
                    : '|' + CoreUtilities.toLowerCase(getItemFrameEntity().getRotation().name()));
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "framed";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.framed_item_rotation>
        // @returns ElementTag
        // @mechanism dEntity.framed
        // @group properties
        // @description
        // If the entity is an item frame, returns the rotation of the material currently framed.
        // -->
        if (attribute.startsWith("framed_item_rotation")) {
            return new ElementTag(CoreUtilities.toLowerCase(getItemFrameEntity().getRotation().name()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.framed_item>
        // @returns dItem
        // @mechanism dEntity.framed
        // @group properties
        // @description
        // If the entity is an item frame, returns the material currently framed.
        // -->
        if (attribute.startsWith("framed_item")) {
            return getItem()
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.has_framed_item>
        // @returns ElementTag(Boolean)
        // @mechanism dEntity.framed
        // @group properties
        // @description
        // If the entity is an item frame, returns whether the frame has an item in it.
        // -->
        if (attribute.startsWith("has_framed_item")) {
            return new ElementTag(hasItem())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name framed
        // @input dItem(|Element)
        // @description
        // Sets the entity's framed item and optionally the rotation as well.
        // Valid rotations: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Rotation.html>
        // For example: framed:i@diamond_sword|clockwise
        // @tags
        // <e@entity.is_frame>
        // <e@entity.has_framed_item>
        // <e@entity.framed_item>
        // <e@entity.framed_item_rotation>
        // -->

        if (mechanism.matches("framed")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.size() == 0) {
                Debug.echoError("Missing value for 'framed' mechanism!");
                return;
            }
            if (new ElementTag(list.get(0)).matchesType(dItem.class)) {
                setItem(new ElementTag(list.get(0)).asType(dItem.class, mechanism.context));
            }
            else {
                Debug.echoError("Invalid item '" + list.get(0) + "'");
            }
            if (list.size() > 1 && new ElementTag(list.get(1)).matchesEnum(Rotation.values())) {
                getItemFrameEntity().setRotation(Rotation.valueOf(list.get(1).toUpperCase()));
            }
        }

    }
}
