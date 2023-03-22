package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;

public class EntityFramed implements Property {

    // TODO: Possibly merge class with EntityItem?
    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof ItemFrame;
    }

    public static EntityFramed getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFramed((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "framed_item_rotation", "framed_item", "has_framed_item"
    };

    public static final String[] handledMechs = new String[] {
            "framed"
    };

    public EntityFramed(EntityTag item) {
        item_frame = item;
    }

    EntityTag item_frame;

    public boolean hasItem() {
        return getItemFrameEntity().getItem() != null
                && getItemFrameEntity().getItem().getType() != Material.AIR;
    }

    public ItemFrame getItemFrameEntity() {
        return (ItemFrame) item_frame.getBukkitEntity();
    }

    public void setItem(ItemTag item) {
        getItemFrameEntity().setItem(item.getItemStack());
    }

    public ItemTag getItem() {
        return new ItemTag(getItemFrameEntity().getItem());
    }

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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.framed_item_rotation>
        // @returns ElementTag
        // @mechanism EntityTag.framed
        // @group properties
        // @description
        // If the entity is an item frame, returns the rotation of the item currently framed.
        // -->
        if (attribute.startsWith("framed_item_rotation")) {
            return new ElementTag(CoreUtilities.toLowerCase(getItemFrameEntity().getRotation().name()))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.framed_item>
        // @returns ItemTag
        // @mechanism EntityTag.framed
        // @group properties
        // @description
        // If the entity is an item frame, returns the item currently framed.
        // -->
        if (attribute.startsWith("framed_item")) {
            return getItem()
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.has_framed_item>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.framed
        // @group properties
        // @description
        // If the entity is an item frame, returns whether the frame has an item in it.
        // -->
        if (attribute.startsWith("has_framed_item")) {
            return new ElementTag(hasItem())
                    .getObjectAttribute(attribute.fulfill(1));
        }
        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name framed
        // @input ItemTag(|ElementTag)
        // @description
        // Sets the entity's framed item and optionally the rotation as well.
        // Valid rotations: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Rotation.html>
        // For example: framed:diamond_sword|clockwise
        // @tags
        // <EntityTag.has_framed_item>
        // <EntityTag.framed_item>
        // <EntityTag.framed_item_rotation>
        // -->
        if (mechanism.matches("framed")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.isEmpty()) {
                mechanism.echoError("Missing value for 'framed' mechanism!");
                return;
            }
            if (new ElementTag(list.get(0)).matchesType(ItemTag.class)) {
                setItem(new ElementTag(list.get(0)).asType(ItemTag.class, mechanism.context));
            }
            else {
                mechanism.echoError("Invalid item '" + list.get(0) + "'");
            }
            if (list.size() > 1 && new ElementTag(list.get(1)).matchesEnum(Rotation.class)) {
                getItemFrameEntity().setRotation(Rotation.valueOf(list.get(1).toUpperCase()));
            }
        }

    }
}
