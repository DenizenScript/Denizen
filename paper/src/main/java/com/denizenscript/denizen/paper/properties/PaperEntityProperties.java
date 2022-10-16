package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityFormObject;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Goat;

import java.util.UUID;

public class PaperEntityProperties implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static final String[] handledMechs = new String[] {
            "goat_ram"
    };

    public static PaperEntityProperties getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new PaperEntityProperties((EntityTag) entity);
    }

    private PaperEntityProperties(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "EntityPaperProperties";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.spawn_reason>
        // @returns ElementTag
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the entity's spawn reason.
        // Valid spawn reasons can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html>
        // -->
        PropertyParser.registerTag(PaperEntityProperties.class, ElementTag.class, "spawn_reason", (attribute, entity) -> {
            return new ElementTag(entity.entity.getBukkitEntity().getEntitySpawnReason());
        });

        // <--[tag]
        // @attribute <EntityTag.xp_spawn_reason>
        // @returns ElementTag
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns its spawn reason.
        // Valid spawn reasons can be found at <@link url https://papermc.io/javadocs/paper/1.17/org/bukkit/entity/ExperienceOrb.SpawnReason.html>
        // -->
        PropertyParser.registerTag(PaperEntityProperties.class, ElementTag.class, "xp_spawn_reason", (attribute, entity) -> {
            if (!(entity.entity.getBukkitEntity() instanceof ExperienceOrb)) {
                attribute.echoError("Entity " + entity.entity + " is not an experience orb.");
                return null;
            }
            return new ElementTag(((ExperienceOrb) entity.entity.getBukkitEntity()).getSpawnReason());
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
        PropertyParser.registerTag(PaperEntityProperties.class, EntityFormObject.class, "xp_trigger", (attribute, entity) -> {
            if (!(entity.entity.getBukkitEntity() instanceof ExperienceOrb)) {
                attribute.echoError("Entity " + entity.entity + " is not an experience orb.");
                return null;
            }
            UUID uuid = ((ExperienceOrb) entity.entity.getBukkitEntity()).getTriggerEntityId();
            if (uuid == null) {
                return null;
            }
            Entity e = EntityTag.getEntityForID(uuid);
            if (e == null) {
                return null;
            }
            return new EntityTag(e).getDenizenObject();
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
        PropertyParser.registerTag(PaperEntityProperties.class, EntityFormObject.class, "xp_source", (attribute, entity) -> {
            if (!(entity.entity.getBukkitEntity() instanceof ExperienceOrb)) {
                attribute.echoError("Entity " + entity.entity + " is not an experience orb.");
                return null;
            }
            UUID uuid = ((ExperienceOrb) entity.entity.getBukkitEntity()).getSourceEntityId();
            if (uuid == null) {
                return null;
            }
            Entity e = EntityTag.getEntityForID(uuid);
            if (e == null) {
                return null;
            }
            return new EntityTag(e).getDenizenObject();
        });

        // <--[tag]
        // @attribute <EntityTag.spawn_location>
        // @returns LocationTag
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the initial spawn location of this entity.
        // -->
        PropertyParser.registerTag(PaperEntityProperties.class, LocationTag.class, "spawn_location", (attribute, entity) -> {
            Location loc = entity.entity.getBukkitEntity().getOrigin();
            return loc != null ? new LocationTag(loc) : null;
        });

        // <--[tag]
        // @attribute <EntityTag.from_spawner>
        // @returns ElementTag(Boolean)
        // @group properties
        // @Plugin Paper
        // @description
        // Returns whether the entity was spawned from a spawner.
        // -->
        PropertyParser.registerTag(PaperEntityProperties.class, ElementTag.class, "from_spawner", (attribute, entity) -> {
            return new ElementTag(entity.entity.getBukkitEntity().fromMobSpawner());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name goat_ram
        // @input EntityTag
        // @Plugin Paper
        // @description
        // Causes a goat to ram the specified entity.
        // -->
        if (mechanism.matches("goat_ram") && mechanism.requireObject(EntityTag.class)
                && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)
                && entity.getBukkitEntity() instanceof Goat) {
            ((Goat) entity.getBukkitEntity()).ram(mechanism.valueAsType(EntityTag.class).getLivingEntity());
        }
    }
}
