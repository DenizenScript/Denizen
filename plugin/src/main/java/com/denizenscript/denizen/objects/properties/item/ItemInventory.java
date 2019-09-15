package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemInventory implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemStack().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemStack().getItemMeta()).getBlockState() instanceof InventoryHolder;
    }

    public static ItemInventory getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemInventory((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "inventory"
    };

    public static final String[] handledMechs = new String[] {
            "inventory"
    };


    private InventoryTag getItemInventory() {
        return InventoryTag.mirrorBukkitInventory(((InventoryHolder) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).getInventory());
    }

    private ItemInventory(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.inventory>
        // @returns InventoryTag
        // @mechanism ItemTag.inventory
        // @group properties
        // @description
        // Returns a InventoryTag of a container item.
        // -->
        if (attribute.startsWith("inventory")) {
            return getItemInventory().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        InventoryTag inv = getItemInventory();
        return inv != null ? inv.identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "inventory";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name inventory
        // @input InventoryTag
        // @description
        // Sets the item's inventory contents.
        // @tags
        // <ItemTag.inventory>
        // -->
        if (mechanism.matches("inventory") && mechanism.requireObject(InventoryTag.class)) {
            InventoryTag inventory = mechanism.valueAsType(InventoryTag.class);
            if (inventory == null || inventory.getInventory() == null) {
                return;
            }

            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            InventoryHolder invHolder = (InventoryHolder) bsm.getBlockState();

            if (inventory.getSize() > invHolder.getInventory().getSize()) {
                Debug.echoError("Invalid InventoryTag size; expected " + invHolder.getInventory().getSize() + " or less.");
                return;
            }

            invHolder.getInventory().setContents(inventory.getContents());
            bsm.setBlockState((BlockState) invHolder);
            itemStack.setItemMeta(bsm);
        }
    }
}
