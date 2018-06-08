package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemInventory implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((dItem) item).getItemStack().getItemMeta()).getBlockState() instanceof InventoryHolder;
    }

    public static ItemInventory getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemInventory((dItem) _item);
        }
    }

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
            dInventory inventory = mechanism.getValue().asType(dInventory.class);
            if (inventory == null || inventory.getInventory() == null) {
                return;
            }

            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            InventoryHolder invHolder = (InventoryHolder) bsm.getBlockState();

            if (inventory.getSize() > invHolder.getInventory().getSize()) {
                dB.echoError("Invalid dInventory size; expected " + invHolder.getInventory().getSize() + " or less.");
                return;
            }

            invHolder.getInventory().setContents(inventory.getContents());
            bsm.setBlockState((BlockState) invHolder);
            itemStack.setItemMeta(bsm);
        }
    }
}
