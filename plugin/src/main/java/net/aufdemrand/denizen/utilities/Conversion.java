package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.aH.Argument;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Conversion {

    /**
     * Turn a list of dColors into a list of Colors.
     *
     * @param colors The list of dColors
     * @return The list of Colors
     */

    public static List<Color> convertColors(List<dColor> colors) {

        List<Color> newList = new ArrayList<>();

        for (dColor color : colors) {
            newList.add(color.getColor());
        }

        return newList;
    }

    /**
     * Turn a list of dItems into a list of ItemStacks.
     *
     * @param items The list of dItems
     * @return The list of ItemStacks
     */

    public static List<ItemStack> convertItems(List<dItem> items) {

        List<ItemStack> newList = new ArrayList<>();

        for (dItem item : items) {
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

    public static List<Entity> convertEntities(List<dEntity> entities) {

        List<Entity> newList = new ArrayList<>();

        for (dEntity entity : entities) {
            newList.add(entity.getBukkitEntity());
        }

        return newList;
    }

    /**
     * Gets a dInventory from an Object, which can be a
     * dEntity, dLocation, dInventory, or a dList of dItems
     *
     * @param arg An argument to parse
     * @return The dInventory retrieved by parsing the argument
     */

    public static AbstractMap.SimpleEntry<Integer, dInventory> getInventory(Argument arg, ScriptEntry scriptEntry) {
        String string = arg.getValue();

        if (dInventory.matches(string)) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (data != null) {
                dInventory inv = dInventory.valueOf(string, data.getTagContext());
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
            else {
                dInventory inv = dInventory.valueOf(string, null);
                return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
            }
        }
        else if (arg.matchesArgumentList(dItem.class)) {
            List<dItem> list = dList.valueOf(string).filter(dItem.class, scriptEntry);
            ItemStack[] items = convertItems(list).toArray(new ItemStack[list.size()]);
            dInventory inventory = new dInventory(Math.max(dInventory.maxSlots, (items.length / 9) * 9 + 9));
            inventory.setContents(items);
            return new AbstractMap.SimpleEntry<>(items.length, inventory);
        }
        else if (dLocation.matches(string)) {
            dInventory inv = dLocation.valueOf(string).getInventory();
            return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
        }
        else if (dEntity.matches(string)) {
            dInventory inv = dEntity.valueOf(string).getInventory();
            return new AbstractMap.SimpleEntry<>(inv.getContents().length, inv);
        }

        return null;
    }
}
