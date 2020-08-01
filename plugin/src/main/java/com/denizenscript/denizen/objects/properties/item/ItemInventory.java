package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.properties.inventory.InventoryContents;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Map;

public class ItemInventory implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof InventoryHolder;
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
            "inventory", "inventory_contents"
    };

    public static final String[] handledMechs = new String[] {
            "inventory", "inventory_contents"
    };

    private InventoryTag getItemInventory() {
        return InventoryTag.mirrorBukkitInventory(((InventoryHolder) ((BlockStateMeta) item.getItemMeta()).getBlockState()).getInventory());
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
        // @attribute <ItemTag.inventory_contents>
        // @returns ListTag(ItemTag)
        // @mechanism ItemTag.inventory_contents
        // @group properties
        // @description
        // Returns a list of the contents of the inventory of a container item.
        // -->
        if (attribute.startsWith("inventory_contents")) {
            return getInventoryContents().getObjectAttribute(attribute.fulfill(1));
        }
        if (attribute.startsWith("inventory")) {
            Deprecations.itemInventoryTag.warn(attribute.context);
            return getItemInventory().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getInventoryContents() {
        InventoryTag inventory = getItemInventory();
        if (inventory == null) {
            return null;
        }
        return InventoryContents.getFrom(inventory).getContents(false);
    }

    @Override
    public String getPropertyString() {
        ListTag inv = getInventoryContents();
        return inv != null ? inv.identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "inventory_contents";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name inventory_contents
        // @input ListTag(ItemTag)
        // @description
        // Sets the item's inventory contents.
        // @tags
        // <ItemTag.inventory_contents>
        // -->
        if ((mechanism.matches("inventory_contents") || mechanism.matches("inventory")) && mechanism.hasValue()) {
            if (mechanism.matches("inventory")) {
                Deprecations.itemInventoryTag.warn(mechanism.context);
            }
            Map.Entry<Integer, InventoryTag> inventoryPair = Conversion.getInventory(mechanism.getValue().asString(), mechanism.context);
            if (inventoryPair == null || inventoryPair.getValue().getInventory() == null) {
                return;
            }

            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            InventoryHolder invHolder = (InventoryHolder) bsm.getBlockState();

            ListTag items = InventoryContents.getFrom(inventoryPair.getValue()).getContents(false);
            if (items.size() > invHolder.getInventory().getSize()) {
                Debug.echoError("Invalid inventory_contents input size; expected " + invHolder.getInventory().getSize() + " or less.");
                return;
            }
            ItemStack[] itemArray = new ItemStack[items.size()];
            for (int i = 0; i < itemArray.length; i++) {
                itemArray[i] = ((ItemTag) items.objectForms.get(i)).getItemStack().clone();
            }

            invHolder.getInventory().setContents(itemArray);
            bsm.setBlockState((BlockState) invHolder);
            itemStack.setItemMeta(bsm);
        }
    }
}
