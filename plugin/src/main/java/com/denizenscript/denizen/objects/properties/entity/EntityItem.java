package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;

public class EntityItem implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                (((EntityTag) entity).getBukkitEntityType() == EntityType.DROPPED_ITEM
                        || ((EntityTag) entity).getBukkitEntityType() == EntityType.ENDERMAN
                        || ((EntityTag) entity).getBukkitEntityType() == EntityType.TRIDENT);
    }

    public static EntityItem getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityItem((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "item"
    };

    public static final String[] handledMechs = new String[] {
            "item"
    };

    private EntityItem(EntityTag entity) {
        item = entity;
    }

    EntityTag item;

    public ItemTag getItem() {
        if (item.getBukkitEntity() instanceof Item) {
            return new ItemTag(((Item) item.getBukkitEntity()).getItemStack());
        }
        else if (item.getBukkitEntityType() == EntityType.TRIDENT) {
            return new ItemTag(NMSHandler.getEntityHelper().getItemFromTrident(item.getBukkitEntity()));
        }
        else {
            return new ItemTag(((Enderman) item.getBukkitEntity()).getCarriedBlock().getMaterial());
        }
    }

    @Override
    public String getPropertyString() {
        ItemTag item = getItem();
        if (item.getItemStack().getType() != Material.AIR) {
            return item.identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "item";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.item>
        // @returns ItemTag
        // @mechanism EntityTag.item
        // @group properties
        // @description
        // If the entity is a dropped item, returns the item represented by the entity.
        // If the entity is an enderman, returns the item that the enderman is holding.
        // If the entity is a trident, returns the trident item represented by the entity.
        // -->
        if (attribute.startsWith("item")) {
            return getItem().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name item
        // @input ItemTag
        // @description
        // Changes what item a dropped item or trident represents, or that an Enderman holds.
        // @tags
        // <EntityTag.item>
        // -->
        if (mechanism.matches("item") && mechanism.requireObject(ItemTag.class)) {
            if (item.getBukkitEntity() instanceof Item) {
                ((Item) item.getBukkitEntity()).setItemStack(mechanism.valueAsType(ItemTag.class).getItemStack());
            }
            else if (item.getBukkitEntityType() == EntityType.TRIDENT) {
                NMSHandler.getEntityHelper().setItemForTrident(item.getBukkitEntity(), mechanism.valueAsType(ItemTag.class).getItemStack());
            }
            else {
                NMSHandler.getEntityHelper().setCarriedItem((Enderman) item.getBukkitEntity(), mechanism.valueAsType(ItemTag.class).getItemStack());
            }
        }
    }
}
