package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Sniffer;

public class EntityExploredLocations extends EntityProperty<ListTag> {

    // <--[property]
    // @object EntityTag
    // @name explored_locations
    // @input ListTag(LocationTag)
    // @plugin Paper
    // @description
    // If the entity is a sniffer, controls the locations that this sniffer has explored.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Sniffer;
    }

    @Override
    public ListTag getPropertyValue() {
        ListTag locations = new ListTag();
        for (Location location : as(Sniffer.class).getExploredLocations()) {
            locations.addObject(new LocationTag(location));
        }
        return locations;
    }

    @Override
    public void setPropertyValue(LocationTag param, Mechanism mechanism) {
        as(Sniffer.class).addExploredLocation(param);
    }

    @Override
    public String getPropertyId() {
        return "explored_locations";
    }

    public static void register() {
        autoRegister("egg_lay_time", EntityExploredLocations.class, ListTag.class, false);
    }
}
