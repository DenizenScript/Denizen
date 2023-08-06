package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.properties.inventory.InventoryContents;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.InventoryHolder;

public class EntityInventory implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof InventoryHolder;
    }

    public static EntityInventory getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityInventory((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "inventory"
    };

    public static final String[] handledMechs = new String[] {
            "inventory_contents"
    };

    public EntityInventory(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new InventoryContents(entity.getInventory()).getContents(false).identify();
    }

    @Override
    public String getPropertyId() {
        return "inventory_contents";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.inventory>
        // @returns InventoryTag
        // @group inventory
        // @description
        // Returns the entity's inventory, if it has one.
        // -->
        if (attribute.startsWith("inventory")) {
            InventoryTag inventory = entity.getInventory();
            if (inventory != null) {
                return inventory.getObjectAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name inventory_contents
        // @input ListTag(ItemTag)
        // @description
        // Clears the entity's inventory and sets it's item list to match the input.
        // @tags
        // <EntityTag.inventory>
        // <InventoryTag.list_contents>
        // -->
        if (mechanism.matches("inventory_contents")) {
            ListTag list = ListTag.valueOf(mechanism.getValue().asString(), mechanism.context);
            InventoryTag inv = entity.getInventory();
            inv.clear();
            int i = 0;
            for (String str : list) {
                inv.setSlots(i, ItemTag.valueOf(str, mechanism.context).getItemStack());
                i++;
            }
        }
    }
}
