package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Location;

public class EntitySpawnLocation implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    public static EntitySpawnLocation getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntitySpawnLocation((EntityTag) entity);
    }

    private EntitySpawnLocation(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return null;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.spawn_location>
        // @returns LocationTag
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the initial spawn location of this entity.
        // -->
        PropertyParser.<EntitySpawnLocation>registerTag("spawn_location", (attribute, entity) -> {
            Location loc = entity.entity.getBukkitEntity().getOrigin();
            return loc != null ? new LocationTag(loc) : null;
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}
