package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class EntityIsSleeping implements Property {

    public static boolean describes(ObjectTag e) {
        if (!(e instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) e).getBukkitEntity();
        return entity instanceof Player
                || entity instanceof Villager
                || entity instanceof Fox;
    }

    public static EntityIsSleeping getFrom(ObjectTag entity) {
        return describes(entity) ? new EntityIsSleeping((EntityTag) entity) : null;
    }

    public EntityIsSleeping(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (entity instanceof Fox) {
            return new ElementTag(((Fox) entity).isSleeping()).identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "is_sleeping";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.is_sleeping>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player, NPC, fox or villager is currently sleeping.
        // -->
        PropertyParser.registerTag(EntityIsSleeping.class, ElementTag.class, "is_sleeping", (attribute, object) -> {
            Entity entity = object.entity.getBukkitEntity();
            if (entity instanceof Fox) {
                return new ElementTag(((Fox) entity).isSleeping());
            }
            else if (entity instanceof Player) {
                return new ElementTag(((Player) entity).isSleeping());
            }
            else if (entity instanceof Villager) {
                return new ElementTag(((Villager) entity).isSleeping());
            }
            return null;
        });

        // <--[mechanism]
        // @object EntityTag
        // @name is_sleeping
        // @input ElementTag(boolean)
        // @description
        // Controls whether the fox is currently sleeping.
        // @tags
        // <EntityTag.is_sleeping>
        // -->
        PropertyParser.registerMechanism(EntityIsSleeping.class, ElementTag.class, "is_sleeping", (object, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                Entity entity = object.entity.getBukkitEntity();
                if (entity instanceof Fox) {
                    ((Fox) entity).setAI(!input.asBoolean());
                    ((Fox) entity).setSleeping(input.asBoolean());
                }
            }
        });

    }

}
