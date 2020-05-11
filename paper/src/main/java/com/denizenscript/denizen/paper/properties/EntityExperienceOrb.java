package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;

import java.util.UUID;

public class EntityExperienceOrb implements Property {
    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ExperienceOrb;
    }

    public static EntityExperienceOrb getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityExperienceOrb((EntityTag) entity);
    }

    private EntityExperienceOrb(EntityTag entity) {
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
        // @attribute <EntityTag.xp_spawn_reason>
        // @returns ElementTag
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns its spawn reason.
        // Valid spawn reasons can be found at <@link url https://papermc.io/javadocs/paper/org/bukkit/entity/ExperienceOrb.SpawnReason.html>
        // -->
        PropertyParser.<EntityExperienceOrb>registerTag("xp_spawn_reason", (attribute, entity) -> {
            return new ElementTag(((ExperienceOrb) entity.entity.getBukkitEntity()).getSpawnReason().name());
        });

        // <--[tag]
        // @attribute <EntityTag.xp_trigger>
        // @returns EntityTag
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns the entity that triggered it spawning (if any).
        // For example, if a player killed an entity this would return the player.
        // -->
        PropertyParser.<EntityExperienceOrb>registerTag("xp_trigger", (attribute, entity) -> {
            UUID uuid = ((ExperienceOrb) entity.entity.getBukkitEntity()).getTriggerEntityId();
            if (uuid == null) {
                return null;
            }
            Entity e = EntityTag.getEntityForID(uuid);
            if (e == null) {
                return null;
            }
            return new EntityTag(e);
        });

        // <--[tag]
        // @attribute <EntityTag.xp_source>
        // @returns EntityTag
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns the entity that it was created from (if any).
        // For example, if the xp orb was spawned from breeding this would return the baby.
        // -->
        PropertyParser.<EntityExperienceOrb>registerTag("xp_source", (attribute, entity) -> {
            UUID uuid = ((ExperienceOrb) entity.entity.getBukkitEntity()).getSourceEntityId();
            if (uuid == null) {
                return null;
            }
            Entity e = EntityTag.getEntityForID(uuid);
            if (e == null) {
                return null;
            }
            return new EntityTag(e);
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}
