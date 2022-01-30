package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.SizedFireball;
import org.bukkit.entity.Turtle;

import java.util.UUID;

public class PaperEntityProperties implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static final String[] handledMechs = new String[] {
            "fireball_display_item"
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
        PropertyParser.<PaperEntityProperties, ElementTag>registerTag(ElementTag.class, "spawn_reason", (attribute, entity) -> {
            return new ElementTag(entity.entity.getBukkitEntity().getEntitySpawnReason().name());
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
        PropertyParser.<PaperEntityProperties, ElementTag>registerTag(ElementTag.class, "xp_spawn_reason", (attribute, entity) -> {
            if (!(entity.entity.getBukkitEntity() instanceof ExperienceOrb)) {
                attribute.echoError("Entity " + entity.entity + " is not an experience orb.");
                return null;
            }
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
        PropertyParser.<PaperEntityProperties, EntityTag>registerTag(EntityTag.class, "xp_trigger", (attribute, entity) -> {
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
        PropertyParser.<PaperEntityProperties, EntityTag>registerTag(EntityTag.class, "xp_source", (attribute, entity) -> {
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
            return new EntityTag(e);
        });

        // <--[tag]
        // @attribute <EntityTag.spawn_location>
        // @returns LocationTag
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the initial spawn location of this entity.
        // -->
        PropertyParser.<PaperEntityProperties, LocationTag>registerTag(LocationTag.class, "spawn_location", (attribute, entity) -> {
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
        PropertyParser.<PaperEntityProperties, ElementTag>registerTag(ElementTag.class, "from_spawner", (attribute, entity) -> {
            return new ElementTag(entity.entity.getBukkitEntity().fromMobSpawner());
        });

        // <--[tag]
        // @attribute <EntityTag.fireball_display_item>
        // @returns ItemTag
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is a fireball, returns its display item.
        // -->
        PropertyParser.<PaperEntityProperties, ItemTag>registerTag(ItemTag.class, "fireball_display_item", (attribute, entity) -> {
            if (!(entity.entity.getBukkitEntity() instanceof SizedFireball)) {
                attribute.echoError("Entity " + entity.entity + " is not a fireball.");
                return null;
            }
            SizedFireball fireball = (SizedFireball) entity.entity.getBukkitEntity();
            return new ItemTag(fireball.getDisplayItem());
        });

        // <--[tag]
        // @attribute <EntityTag.carrying_egg>
        // @returns ElementTag(Boolean)
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is a turtle, returns whether it is carrying an egg.
        // -->
        PropertyParser.<PaperEntityProperties, ElementTag>registerTag(ElementTag.class, "carrying_egg", (attribute, entity) -> {
            if (!(entity.entity.getBukkitEntity() instanceof Turtle)) {
                attribute.echoError("Entity " + entity.entity + " is not a turtle.");
                return null;
            }
            Turtle turtle = (Turtle) entity.entity.getBukkitEntity();
            return new ElementTag(turtle.hasEgg());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name fireball_display_item
        // @input ItemTag
        // @Plugin Paper
        // @description
        // If the entity is a fireball, sets its display item.
        // @tags
        // <EntityTag.fireball_display_item>
        // -->
        if (mechanism.matches("fireball_display_item") && mechanism.requireObject(ItemTag.class)) {
            if (!(entity.getBukkitEntity() instanceof SizedFireball)) {
                mechanism.echoError("Entity " + entity + " is not a fireball.");
                return;
            }
            SizedFireball fireball = (SizedFireball) entity.getBukkitEntity();
            fireball.setDisplayItem(mechanism.valueAsType(ItemTag.class).getItemStack());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name carrying_egg
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @description
        // If the entity is a turtle, sets whether it is carrying an egg.
        // @tags
        // <EntityTag.carrying_egg>
        // -->
        if (mechanism.matches("carrying_egg") && mechanism.requireBoolean()) {
            if (!(entity.getBukkitEntity() instanceof Turtle)) {
                mechanism.echoError("Entity " + entity.entity + " is not a turtle.");
                return;
            }
            Turtle turtle = (Turtle) entity.getBukkitEntity();
            turtle.setHasEgg(mechanism.getValue().asBoolean());
        }
    }
}
