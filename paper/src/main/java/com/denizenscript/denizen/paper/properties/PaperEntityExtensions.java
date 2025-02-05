package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityFormObject;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import io.papermc.paper.entity.Shearable;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Goat;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class PaperEntityExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.spawn_reason>
        // @returns ElementTag
        // @group paper
        // @Plugin Paper
        // @description
        // Returns the entity's spawn reason.
        // Valid spawn reasons can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html>
        // -->
        EntityTag.tagProcessor.registerTag(ElementTag.class, "spawn_reason", (attribute, entity) -> {
            return new ElementTag(entity.getBukkitEntity().getEntitySpawnReason());
        });

        // <--[tag]
        // @attribute <EntityTag.xp_spawn_reason>
        // @returns ElementTag
        // @group paper
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns its spawn reason.
        // Valid spawn reasons can be found at <@link url https://papermc.io/javadocs/paper/1.17/org/bukkit/entity/ExperienceOrb.SpawnReason.html>
        // -->
        EntityTag.tagProcessor.registerTag(ElementTag.class, "xp_spawn_reason", (attribute, entity) -> {
            if (!(entity.getBukkitEntity() instanceof ExperienceOrb experienceOrb)) {
                attribute.echoError("Entity " + entity + " is not an experience orb.");
                return null;
            }
            return new ElementTag(experienceOrb.getSpawnReason());
        });

        // <--[tag]
        // @attribute <EntityTag.xp_trigger>
        // @returns EntityTag
        // @group paper
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns the entity that triggered it spawning (if any).
        // For example, if a player killed an entity this would return the player.
        // -->
        EntityTag.tagProcessor.registerTag(EntityFormObject.class, "xp_trigger", (attribute, entity) -> {
            if (!(entity.getBukkitEntity() instanceof ExperienceOrb experienceOrb)) {
                attribute.echoError("Entity " + entity + " is not an experience orb.");
                return null;
            }
            UUID uuid = experienceOrb.getTriggerEntityId();
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
        // @group paper
        // @Plugin Paper
        // @description
        // If the entity is an experience orb, returns the entity that it was created from (if any).
        // For example, if the xp orb was spawned from breeding this would return the baby.
        // -->
        EntityTag.registerSpawnedOnlyTag(EntityFormObject.class, "xp_source", (attribute, entity) -> {
            if (!(entity.getBukkitEntity() instanceof ExperienceOrb experienceOrb)) {
                attribute.echoError("Entity " + entity + " is not an experience orb.");
                return null;
            }
            UUID uuid = experienceOrb.getSourceEntityId();
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
        // @group paper
        // @Plugin Paper
        // @description
        // Returns the initial spawn location of this entity.
        // -->
        EntityTag.tagProcessor.registerTag(LocationTag.class, "spawn_location", (attribute, entity) -> {
            Location loc = entity.getBukkitEntity().getOrigin();
            return loc != null ? new LocationTag(loc) : null;
        });

        // <--[tag]
        // @attribute <EntityTag.from_spawner>
        // @returns ElementTag(Boolean)
        // @group paper
        // @Plugin Paper
        // @description
        // Returns whether the entity was spawned from a spawner.
        // -->
        EntityTag.tagProcessor.registerTag(ElementTag.class, "from_spawner", (attribute, entity) -> {
            return new ElementTag(entity.getBukkitEntity().fromMobSpawner());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name goat_ram
        // @input EntityTag
        // @Plugin Paper
        // @group paper
        // @description
        // Causes a goat to ram the specified entity.
        // -->
        EntityTag.registerSpawnedOnlyMechanism("goat_ram", false, EntityTag.class, (object, mechanism, input) -> {
            if (object.getBukkitEntity() instanceof Goat goat) {
                goat.ram(input.getLivingEntity());
            }
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {

            // <--[tag]
            // @attribute <EntityTag.collides_at[<location>]>
            // @returns ElementTag(Boolean)
            // @group paper
            // @Plugin Paper
            // @description
            // Returns whether the entity's bounding box would collide if the entity was moved to the given location.
            // This checks for any colliding entities (like boats and shulkers), the world border and regular blocks.
            // (Note that this won't load chunks at the location.)
            // -->
            EntityTag.tagProcessor.registerTag(ElementTag.class, LocationTag.class, "collides_at", (attribute, entity, location) -> {
                return new ElementTag(entity.getBukkitEntity().collidesAt(location));
            });

            // <--[mechanism]
            // @object EntityTag
            // @name damage_item
            // @input MapTag
            // @Plugin Paper
            // @group paper
            // @description
            // Damages the given equipment slot for the given amount.
            // This runs all vanilla logic associated with damaging an item like gamemode and enchantment checks, events, stat changes, advancement triggers, and notifying clients to play break animations.
            // Input is a map with "slot" as a valid equipment slot, and "amount" as the damage amount to be dealt.
            // Valid equipment slot values can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html>.
            //
            // @example
            // # Damages your precious boots! :(
            // - adjust <player> damage_item:[slot=feet;amount=45]
            // -->
            EntityTag.registerSpawnedOnlyMechanism("damage_item", false, MapTag.class, (object, mechanism, input) -> {
                ElementTag slot = input.getElement("slot");
                ElementTag amount = input.getElement("amount");
                if (slot == null || !slot.matchesEnum(EquipmentSlot.class)) {
                    mechanism.echoError("Must specify a valid equipment slot to damage.");
                    return;
                }
                if (amount == null || !amount.isInt()) {
                    mechanism.echoError("Must specify a valid amount to damage this item for.");
                    return;
                }
                object.getLivingEntity().damageItemStack(slot.asEnum(EquipmentSlot.class), amount.asInt());
            });

            // <--[mechanism]
            // @object EntityTag
            // @name shear
            // @Plugin paper
            // @group paper
            // @description
            // Shears entities in the same way as a player can do using shears, including drops.
            // If the entity is not ready to be sheared, there will be no drops but the sound will still play.
            //
            // This mech will:
            // - Shear a sheep
            // - harvest a bogged
            // - harvest a mushroom cow (note: entity data will be lost as Minecraft will remove the entity and spawn an entirely new cow instead)
            // - derp a snowman (i.e. remove the pumpkin)
            //
            // Optionally, specify a sound source to change the source of the sound.
            // Valid sound sources can be found here: <@link url https://jd.advntr.dev/api/latest/net/kyori/adventure/sound/Sound.Source.html>.
            //
            // @example
            // # Shears the entity you're looking at.
            // - adjust <player.target> shear
            //
            // -->
            EntityTag.registerSpawnedOnlyMechanism("shear", false, (object, mechanism) -> {
                if (!(object.getBukkitEntity() instanceof Shearable shearable)) {
                    return;
                }
                if (!mechanism.hasValue()) {
                    shearable.shear();
                    return;
                }
                ElementTag input = mechanism.getValue();
                if (!mechanism.requireEnum(Sound.Source.class)) {
                    mechanism.echoError("Invalid sound source specified: " + input);
                    return;
                }
                Sound.Source source = input.asEnum(Sound.Source.class);
                shearable.shear(source);
            });
        }
    }
}
