package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.properties.inventory.InventoryContents;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemInventory implements Property {

    public static boolean describes(ObjectTag item) {
        if (!(item instanceof ItemTag)) {
            return false;
        }
        ItemMeta meta = ((ItemTag) item).getItemMeta();
        if (meta instanceof BlockStateMeta
                && ((BlockStateMeta) meta).getBlockState() instanceof InventoryHolder) {
            return true;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && meta instanceof BundleMeta) {
            return true;
        }
        return false;
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
        InventoryHolder holder = ((InventoryHolder) ((BlockStateMeta) item.getItemMeta()).getBlockState());
        Inventory inv = getInventoryFor(holder);
        return InventoryTag.mirrorBukkitInventory(inv);
    }

    public static Inventory getInventoryFor(InventoryHolder holder) {
        if (holder instanceof Chest) {
            return ((Chest) holder).getBlockInventory();
        }
        else {
            return holder.getInventory();
        }
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
            BukkitImplDeprecations.itemInventoryTag.warn(attribute.context);
            return getItemInventory().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getInventoryContents() {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            InventoryTag inventory = getItemInventory();
            if (inventory == null) {
                return null;
            }
            return InventoryContents.getFrom(inventory).getContents(false);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            ListTag result = new ListTag();
            for (ItemStack item : ((BundleMeta) item.getItemMeta()).getItems()) {
                if (item != null && item.getType() != Material.AIR) {
                    result.addObject(new ItemTag(item));
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        ListTag items = getInventoryContents();
        return items != null ? items.identify() : null;
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
        if (mechanism.matches("inventory_contents") && mechanism.hasValue()) {
            List<ItemStack> items = new ArrayList<>();
            for (ItemTag item : mechanism.valueAsType(ListTag.class).filter(ItemTag.class, mechanism.context)) {
                items.add(item.getItemStack());
            }
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta bsm = ((BlockStateMeta) item.getItemMeta());
                InventoryHolder invHolder = (InventoryHolder) bsm.getBlockState();
                if (items.size() > getInventoryFor(invHolder).getSize()) {
                    mechanism.echoError("Invalid inventory_contents input size; expected " + getInventoryFor(invHolder).getSize() + " or less.");
                    return;
                }
                getInventoryFor(invHolder).setContents(items.toArray(new ItemStack[0]));
                bsm.setBlockState((BlockState) invHolder);
                item.setItemMeta(bsm);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
                BundleMeta bundle = (BundleMeta) item.getItemMeta();
                bundle.setItems(items);
                item.setItemMeta(bundle);
            }
        }

        if (mechanism.matches("inventory") && mechanism.hasValue()) {
            BukkitImplDeprecations.itemInventoryTag.warn(mechanism.context);
            Argument argument = new Argument("");
            argument.unsetValue();
            argument.object = mechanism.getValue();
            Map.Entry<Integer, InventoryTag> inventoryPair = Conversion.getInventory(argument, mechanism.context);
            if (inventoryPair == null || inventoryPair.getValue().getInventory() == null) {
                return;
            }
            ListTag items = InventoryContents.getFrom(inventoryPair.getValue()).getContents(false);
            ItemStack[] itemArray = new ItemStack[items.size()];
            for (int i = 0; i < itemArray.length; i++) {
                itemArray[i] = ((ItemTag) items.objectForms.get(i)).getItemStack().clone();
            }
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta bsm = ((BlockStateMeta) item.getItemMeta());
                InventoryHolder invHolder = (InventoryHolder) bsm.getBlockState();
                if (items.size() > getInventoryFor(invHolder).getSize()) {
                    mechanism.echoError("Invalid inventory mechanism input size; expected " + getInventoryFor(invHolder).getSize() + " or less.");
                    return;
                }
                getInventoryFor(invHolder).setContents(itemArray);
                bsm.setBlockState((BlockState) invHolder);
                item.setItemMeta(bsm);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
                BundleMeta bundle = (BundleMeta) item.getItemMeta();
                bundle.setItems(Arrays.asList(itemArray));
                item.setItemMeta(bundle);
            }
        }
    }
}
