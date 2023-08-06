package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversion {

    public static List<Color> convertColors(List<ColorTag> colors) {
        List<Color> newList = new ArrayList<>();
        for (ColorTag color : colors) {
            newList.add(BukkitColorExtensions.getColor(color));
        }
        return newList;
    }

    public static List<ItemStack> convertItems(List<ItemTag> items) {
        List<ItemStack> newList = new ArrayList<>();
        for (ItemTag item : items) {
            newList.add(item.getItemStack());
        }
        return newList;
    }

    public static List<Entity> convertEntities(List<EntityTag> entities) {
        List<Entity> newList = new ArrayList<>();
        for (EntityTag entity : entities) {
            newList.add(entity.getBukkitEntity());
        }
        return newList;
    }

    public static AbstractMap.SimpleEntry<Integer, InventoryTag> getInventory(Argument arg, ScriptEntry scriptEntry) {
        return getInventory(arg, scriptEntry == null ? null : scriptEntry.context);
    }

    public static AbstractMap.SimpleEntry<Integer, InventoryTag> getInventory(Argument arg, TagContext context) {
        boolean isElement = arg.object instanceof ElementTag;
        if (arg.object instanceof InventoryTag || (isElement && InventoryTag.matches(arg.getValue()))) {
            InventoryTag inv = arg.object instanceof InventoryTag ? (InventoryTag) arg.object : InventoryTag.valueOf(arg.getValue(), context);
            if (inv != null) {
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }
        else if (arg.object instanceof MapTag || (isElement && arg.getValue().startsWith("map@"))) {
            MapTag map = arg.object instanceof MapTag ? (MapTag) arg.object : MapTag.valueOf(arg.getValue(), context);
            int maxSlot = 0;
            for (Map.Entry<StringHolder, ObjectTag> entry : map.entrySet()) {
                if (!ArgumentHelper.matchesInteger(entry.getKey().str)) {
                    return null;
                }
                int slot = new ElementTag(entry.getKey().str).asInt();
                if (slot > maxSlot) {
                    maxSlot = slot;
                }
            }
            InventoryTag inventory = new InventoryTag(Math.min(InventoryTag.maxSlots, (maxSlot / 9) * 9 + 9));
            for (Map.Entry<StringHolder, ObjectTag> entry : map.entrySet()) {
                int slot = new ElementTag(entry.getKey().str).asInt();
                ItemTag item = ItemTag.getItemFor(entry.getValue(), context);
                if (item == null) {
                    if (context == null || context.debug || CoreConfiguration.debugOverride) {
                        Debug.echoError("Not a valid item: '" + entry.getValue() + "'");
                    }
                    continue;
                }
                inventory.getInventory().setItem(slot - 1, item.getItemStack());
            }
            return new AbstractMap.SimpleEntry<>(maxSlot, inventory);
        }
        else if (arg.object instanceof LocationTag || (isElement && LocationTag.matches(arg.getValue()))) {
            InventoryTag inv = (arg.object instanceof LocationTag ? (LocationTag) arg.object : LocationTag.valueOf(arg.getValue(), context)).getInventory();
            if (inv != null) {
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }
        else if (arg.object instanceof EntityTag || arg.object instanceof PlayerTag || arg.object instanceof NPCTag || (isElement && EntityTag.matches(arg.getValue()))) {
            InventoryTag inv = EntityTag.valueOf(arg.getValue(), context).getInventory();
            if (inv != null) {
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }
        ListTag asList = ListTag.getListFor(arg.object, context);
        if (asList.containsObjectsFrom(ItemTag.class) || asList.isEmpty()) {
            List<ItemTag> list = asList.filter(ItemTag.class, context);
            ItemStack[] items = convertItems(list).toArray(new ItemStack[list.size()]);
            InventoryTag inventory = new InventoryTag(Math.min(InventoryTag.maxSlots, (items.length / 9) * 9 + 9));
            inventory.setContents(items);
            return new AbstractMap.SimpleEntry<>(items.length, inventory);
        }
        return null;
    }
}
