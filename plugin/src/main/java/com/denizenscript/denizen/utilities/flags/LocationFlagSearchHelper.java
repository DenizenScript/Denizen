package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;
import java.util.function.Consumer;

public class LocationFlagSearchHelper {

    public static void getFlaggedLocations(Chunk chunk, String flagName, Consumer<Location> handleLocation) {
        int subKeyIndex = flagName.indexOf('.');
        String fullPath = flagName;
        if (subKeyIndex != -1) {
            flagName = flagName.substring(0, subKeyIndex);
        }
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        Location ref = new Location(chunk.getWorld(), 0, 0, 0);
        for (NamespacedKey key : container.getKeys()) {
            if (key.getNamespace().equals("denizen") && key.getKey().startsWith("flag_tracker_") && key.getKey().endsWith(flagName)) {
                List<String> split = CoreUtilities.split(key.getKey(), '_', 6);
                if (split.size() == 6 && split.get(5).equals(flagName)) {
                    ref.setX(Integer.parseInt(split.get(2)));
                    ref.setY(Integer.parseInt(split.get(3)));
                    ref.setZ(Integer.parseInt(split.get(4)));
                    if (new LocationTag(ref).getFlagTracker().hasFlag(fullPath)) {
                        handleLocation.accept(ref);
                    }
                }
            }
        }
    }
}
