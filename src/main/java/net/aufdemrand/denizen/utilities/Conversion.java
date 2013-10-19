package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dLocation;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Entity;

public class Conversion {

    /**
     * Turn a List of dColors into a list of Colors.
     *
     * @param entities The list of dColors
     */

    public static List<Color> convertColors(List<dColor> colors) {

        List<Color> newList = new ArrayList<Color>();

        for (dColor color : colors)
            newList.add(color.getColor());

        return newList;
    }

    /**
     * Turn a List of dEntities into a list of Entities.
     *
     * @param entities The list of dEntities
     */

    public static List<Entity> convertEntities(List<dEntity> entities) {

        List<Entity> newList = new ArrayList<Entity>();

        for (dEntity entity : entities)
            newList.add(entity.getBukkitEntity());

        return newList;
    }

    /**
     * Gets a dInventory from an Object, which can be a
     * dEntity, dLocation or dInventory
     *
     * @param entities The list of dEntities
     */

    public static dInventory getInventory(String string) {
        if (dInventory.matches(string)) {
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
