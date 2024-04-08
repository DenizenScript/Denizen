package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.inventory.InventoryContents;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemInventoryContents extends ItemProperty<ListTag> {

    // <--[property]
    // @object ItemTag
    // @name inventory_contents
    // @input ListTag(ItemTag)
    // @description
    // A container item's inventory contents.
    // -->

    public static boolean describes(ItemTag item) {
        return (item.getItemMeta() instanceof BlockStateMeta blockStateMeta && blockStateMeta.getBlockState() instanceof InventoryHolder)
                || item.getItemMeta() instanceof BundleMeta;
    }

    public InventoryTag getItemInventory() {
        InventoryHolder holder = (InventoryHolder) as(BlockStateMeta.class).getBlockState();
        Inventory inv = getInventoryFor(holder);
        return InventoryTag.mirrorBukkitInventory(inv);
    }

    public static Inventory getInventoryFor(InventoryHolder holder) {
        return holder instanceof Chest chest ? chest.getBlockInventory() : holder.getInventory();
    }

    @Override
    public ListTag getPropertyValue() {
        if (getItemMeta() instanceof BlockStateMeta blockStateMeta) {
            if (!blockStateMeta.hasBlockState()) {
                return null;
            }
            return new InventoryContents(getItemInventory()).getContents(false);
        }
        return new ListTag(as(BundleMeta.class).getItems(), item -> item != null && item.getType() != Material.AIR, ItemTag::new);
    }

    @Override
    public void setPropertyValue(ListTag value, Mechanism mechanism) {
        List<ItemStack> items = new ArrayList<>(value.size());
        for (ItemTag item : value.filter(ItemTag.class, mechanism.context)) {
            items.add(item.getItemStack());
        }
        if (getItemMeta() instanceof BlockStateMeta blockStateMeta) {
            BlockState state = blockStateMeta.getBlockState();
            Inventory inventory = getInventoryFor((InventoryHolder) state);
            if (items.size() > inventory.getSize()) {
                mechanism.echoError("Input list is too large: must be " + inventory.getSize() + " or less.");
                return;
            }
            inventory.setContents(items.toArray(new ItemStack[0]));
            blockStateMeta.setBlockState(state);
            setItemMeta(blockStateMeta);
        }
        else {
            editMeta(BundleMeta.class, bundleMeta -> bundleMeta.setItems(items));
        }
    }

    @Override
    public String getPropertyId() {
        return "inventory_contents";
    }

    public static void register() {
        PropertyParser.registerTag(ItemInventoryContents.class, ListTag.class, "inventory_contents", (attribute, prop) -> {
            if (prop.getItemMeta() instanceof BlockStateMeta blockStateMeta && !blockStateMeta.hasBlockState()) {
                return new ListTag();
            }
            return prop.getPropertyValue();
        });
        PropertyParser.registerMechanism(ItemInventoryContents.class, ListTag.class, "inventory_contents", (prop, mechanism, input) -> {
            prop.setPropertyValue(input, mechanism);
        });

        PropertyParser.registerTag(ItemInventoryContents.class, InventoryTag.class, "inventory", (attribute, prop) -> {
            BukkitImplDeprecations.itemInventoryTag.warn(attribute.context);
            return prop.getItemInventory();
        });
        PropertyParser.registerMechanism(ItemInventoryContents.class, InventoryTag.class, "inventory", (prop, mechanism, input) -> {
            BukkitImplDeprecations.itemInventoryTag.warn(mechanism.context);
            Argument argument = new Argument("");
            argument.unsetValue();
            argument.object = mechanism.getValue();
            Map.Entry<Integer, InventoryTag> inventoryPair = Conversion.getInventory(argument, mechanism.context);
            if (inventoryPair == null || inventoryPair.getValue().getInventory() == null) {
                return;
            }
            ListTag items = new InventoryContents(inventoryPair.getValue()).getContents(false);
            ItemStack[] itemArray = new ItemStack[items.size()];
            for (int i = 0; i < itemArray.length; i++) {
                itemArray[i] = ((ItemTag) items.objectForms.get(i)).getItemStack().clone();
            }
            if (prop.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
                InventoryHolder invHolder = (InventoryHolder) blockStateMeta.getBlockState();
                if (items.size() > getInventoryFor(invHolder).getSize()) {
                    mechanism.echoError("Invalid inventory mechanism input size; expected " + getInventoryFor(invHolder).getSize() + " or less.");
                    return;
                }
                getInventoryFor(invHolder).setContents(itemArray);
                blockStateMeta.setBlockState((BlockState) invHolder);
                prop.setItemMeta(blockStateMeta);
            }
            else {
                prop.editMeta(BundleMeta.class, bundleMeta -> bundleMeta.setItems(Arrays.asList(itemArray)));
            }
        });
    }
}
