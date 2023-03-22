package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

import java.util.Collection;

public class EntityFlags implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityFlags getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFlags((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
    };

    public static final String[] handledMechs = new String[] {
            "flag_map"
    };

    public EntityFlags(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        AbstractFlagTracker tracker = entity.getFlagTracker();
        if (!(tracker instanceof DataPersistenceFlagTracker)) {
            return null;
        }
        Collection<String> flagNames = tracker.listAllFlags();
        if (flagNames.isEmpty()) {
            return null;
        }
        MapTag flags = new MapTag();
        for (String name : flagNames) {
            flags.putObject(name, tracker.getRootMap(name));
        }
        return flags.toString();
    }

    @Override
    public String getPropertyId() {
        return "flag_map";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name flag_map
        // @input MapTag
        // @description
        // Internal setter for the EntityTag flag map.
        // Do not use this in scripts.
        // -->
        if (mechanism.matches("flag_map") && mechanism.requireObject(MapTag.class)) {
            MapTagFlagTracker flags = new MapTagFlagTracker(mechanism.valueAsType(MapTag.class));
            AbstractFlagTracker tracker = entity.getFlagTracker();
            if (!(tracker instanceof DataPersistenceFlagTracker)) {
                return;
            }
            for (String flagName : flags.map.keys()) {
                ((DataPersistenceFlagTracker) tracker).setRootMap(flagName, flags.getRootMap(flagName));
            }
            entity.reapplyTracker(tracker);
        }
    }
}
