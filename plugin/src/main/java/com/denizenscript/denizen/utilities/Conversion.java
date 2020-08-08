package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversion {

    /**
     * Turn a list of ColorTags into a list of Colors.
     *
     * @param colors The list of ColorTags
     * @return The list of Colors
     */
    public static List<Color> convertColors(List<ColorTag> colors) {

        List<Color> newList = new ArrayList<>();

        for (ColorTag color : colors) {
            newList.add(color.getColor());
        }

        return newList;
    }

    /**
     * Turn a list of ItemTags into a list of ItemStacks.
     *
     * @param items The list of ItemTags
     * @return The list of ItemStacks
     */
    public static List<ItemStack> convertItems(List<ItemTag> items) {

        List<ItemStack> newList = new ArrayList<>();

        for (ItemTag item : items) {
            newList.add(item.getItemStack());
        }

        return newList;
    }

    /**
     * Turn a list of dEntities into a list of Entities.
     *
     * @param entities The list of dEntities
     * @return The list of Entities
     */
    public static List<Entity> convertEntities(List<EntityTag> entities) {

        List<Entity> newList = new ArrayList<>();

        for (EntityTag entity : entities) {
            newList.add(entity.getBukkitEntity());
        }

        return newList;
    }

    /**
     * Gets the InventoryTag from an Object, which can be a
     * EntityTag, LocationTag, InventoryTag, or a ListTag of ItemTags
     *
     * @param arg An argument to parse
     * @return The InventoryTag retrieved by parsing the argument
     */
    public static AbstractMap.SimpleEntry<Integer, InventoryTag> getInventory(Argument arg, ScriptEntry scriptEntry) {
        return getInventory(arg.getValue(), scriptEntry.context);
    }

    public static AbstractMap.SimpleEntry<Integer, InventoryTag> getInventory(String string, TagContext context) {
        if (InventoryTag.matches(string)) {
            InventoryTag inv = InventoryTag.valueOf(string, context);
            if (inv != null) {
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }
        else if (string.startsWith("map@")) {
            MapTag map = MapTag.valueOf(string, context);
            int maxSlot = 0;
            for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
                if (!ArgumentHelper.matchesInteger(entry.getKey().str)) {
                    return null;
                }
                int slot = new ElementTag(entry.getKey().str).asInt();
                if (slot > maxSlot) {
                    maxSlot = slot;
                }
            }
            InventoryTag inventory = new InventoryTag(Math.min(InventoryTag.maxSlots, (maxSlot / 9) * 9 + 9));
            for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
                int slot = new ElementTag(entry.getKey().str).asInt();
                ItemTag item = ItemTag.getItemFor(entry.getValue(), context);
                if (item == null) {
                    if (context == null || context.debug) {
                        Debug.echoError("Not a valid item: '" + entry.getValue() + "'");
                    }
                    continue;
                }
                inventory.getInventory().setItem(slot - 1, item.getItemStack());
            }
            return new AbstractMap.SimpleEntry<>(maxSlot, inventory);
        }
        else if (ListTag.valueOf(string, context).containsObjectsFrom(ItemTag.class)) {
            List<ItemTag> list = ListTag.valueOf(string, context).filter(ItemTag.class, context);
            ItemStack[] items = convertItems(list).toArray(new ItemStack[list.size()]);
            InventoryTag inventory = new InventoryTag(Math.min(InventoryTag.maxSlots, (items.length / 9) * 9 + 9));
            inventory.setContents(items);
            return new AbstractMap.SimpleEntry<>(items.length, inventory);
        }
        else if (LocationTag.matches(string)) {
            InventoryTag inv = LocationTag.valueOf(string, context).getInventory();
            if (inv != null) {
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }
        else if (EntityTag.matches(string)) {
            InventoryTag inv = EntityTag.valueOf(string, context).getInventory();
            if (inv != null) {
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }

        return null;
    }
}
