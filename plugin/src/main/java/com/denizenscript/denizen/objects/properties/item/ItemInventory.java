package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizen.objects.dItem;
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
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((dItem) item).getItemStack().getItemMeta()).getBlockState() instanceof InventoryHolder;
    }

    public static ItemInventory getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemInventory((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "inventory"
    };

    public static final String[] handledMechs = new String[] {
            "inventory"
    };


    private dInventory getItemInventory() {
        return dInventory.mirrorBukkitInventory(((InventoryHolder) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).getInventory());
    }

    private ItemInventory(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.inventory>
        // @returns dInventory
        // @mechanism dItem.inventory
        // @group properties
        // @description
        // Returns a dInventory of a container item.
        // -->
        if (attribute.startsWith("inventory")) {
            return getItemInventory().getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        dInventory inv = getItemInventory();
        return inv != null ? inv.identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "inventory";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name inventory
        // @input dInventory
        // @description
        // Sets the item's inventory contents.
        // @tags
        // <i@item.inventory>
        // -->
        if (mechanism.matches("inventory") && mechanism.requireObject(dInventory.class)) {
            dInventory inventory = mechanism.valueAsType(dInventory.class);
            if (inventory == null || inventory.getInventory() == null) {
                return;
            }

            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            InventoryHolder invHolder = (InventoryHolder) bsm.getBlockState();

            if (inventory.getSize() > invHolder.getInventory().getSize()) {
                Debug.echoError("Invalid dInventory size; expected " + invHolder.getInventory().getSize() + " or less.");
                return;
            }

            invHolder.getInventory().setContents(inventory.getContents());
            bsm.setBlockState((BlockState) invHolder);
            itemStack.setItemMeta(bsm);
        }
    }
}
