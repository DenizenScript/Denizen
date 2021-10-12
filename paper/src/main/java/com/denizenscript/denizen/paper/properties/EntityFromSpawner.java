package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class EntityFromSpawner implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    public static EntityFromSpawner getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityFromSpawner((EntityTag) entity);
    }

    private EntityFromSpawner(EntityTag entity) {
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
        // @attribute <EntityTag.from_spawner>
        // @returns ElementTag(Boolean)
        // @group properties
        // @Plugin Paper
        // @description
        // Returns whether the entity was spawned from a spawner.
        // -->
        PropertyParser.<EntityFromSpawner>registerTag("from_spawner", (attribute, entity) -> {
            return new ElementTag(entity.entity.getBukkitEntity().fromMobSpawner());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}
