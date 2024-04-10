package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.Location;
import org.bukkit.entity.Sniffer;

public class EntityExploredLocations extends EntityProperty<ListTag> {

    // <--[property]
    // @object EntityTag
    // @name explored_locations
    // @input ListTag(LocationTag)
    // @description
    // If the entity is a sniffer, controls the locations that it has explored. 
    // Note that if the sniffer is not in the same world as the inputted LocationTag(s), then the LocationTag(s) will not be added to the list of explored locations.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Sniffer;
    }

    @Override
    public ListTag getPropertyValue() {
        return new ListTag(as(Sniffer.class).getExploredLocations(), LocationTag::new);
    }

    @Override
    public void setPropertyValue(ListTag param, Mechanism mechanism) {
        for (Location location : param.filter(LocationTag.class, mechanism.context)) {
            as(Sniffer.class).addExploredLocation(location);
        }
    }

    @Override
    public String getPropertyId() {
        return "explored_locations";
    }

    public static void register() {
        autoRegister("explored_locations", EntityExploredLocations.class, ListTag.class, false);
    }
}
