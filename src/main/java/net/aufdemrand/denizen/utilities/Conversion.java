package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.objects.aH.Argument;
import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;

import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class Conversion {

    /**
     * Turn a list of dColors into a list of Colors.
     *
     * @param colors The list of dColors
     * @return The list of Colors
     */

    public static List<Color> convertColors(List<dColor> colors) {

        List<Color> newList = new ArrayList<Color>();

        for (dColor color : colors)
            newList.add(color.getColor());

        return newList;
    }

    /**
     * Turn a list of dItems into a list of ItemStacks.
     *
     * @param entities The list of dItems
     * @return The list of ItemStacks
     */

    public static List<ItemStack> convertItems(List<dItem> items) {

        List<ItemStack> newList = new ArrayList<ItemStack>();

        for (dItem item : items)
            newList.add(item.getItemStack());

        return newList;
    }

    /**
     * Turn a list of dEntities into a list of Entities.
     *
     * @param entities The list of dEntities
     * @return The list of Entities
     */

    public static List<Entity> convertEntities(List<dEntity> entities) {

        List<Entity> newList = new ArrayList<Entity>();

        for (dEntity entity : entities)
            newList.add(entity.getBukkitEntity());

        return newList;
    }

    /**
     * Gets a dInventory from an Object, which can be a
     * dEntity, dLocation, dInventory, or a dList of dItems
     *
     * @param arg An argument to parse
     * @return The dInventory retrieved by parsing the argument
     */

    public static dInventory getInventory(Argument arg) {
        String string = arg.getValue();

        if (arg.matchesArgumentList(dItem.class)) {
            Object list1 = dList.valueOf(string).filter(dItem.class);

            @SuppressWarnings("unchecked")
            List<ItemStack> list2 = convertItems((List<dItem>) list1);
            ItemStack[] items = list2.toArray(new ItemStack[list2.size()]);

            return new dInventory(dInventory.maxSlots).add(items);
        }
        else if (dInventory.matches(string)) {
            return dInventory.valueOf(string);
        }
        else if (dLocation.matches(string)) {
            return dLocation.valueOf(string).getInventory();
        }
        else if (dEntity.matches(string)) {
            return dEntity.valueOf(string).getInventory();
        }

        return null;
    }
}
