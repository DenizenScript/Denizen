package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;

public class EntityMobGoal implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Mob;
    }

    public static EntityMobGoal getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityMobGoal((EntityTag) entity);
    }

    public static final String[] handledMechs = new String[] {
            "remove_mob_goal"
    };

    private EntityMobGoal(EntityTag entity) {
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
        // @attribute <EntityTag.mob_goals>
        // @returns ListTag
        // @mechanism EntityTag.remove_mob_goal
        // @group properties
        // @Plugin Paper
        // @description
        // Returns all of the mob-specific goals applied to this entity.
        // Note that some of these may appear as magic unknown values, and are subject to change in future versions of Paper.
        // -->
        PropertyParser.<EntityMobGoal>registerTag("mob_goals", (attribute, entity) -> {
            ListTag goals = new ListTag();
            for (Goal<Mob> goal : Bukkit.getMobGoals().getAllGoals((Mob) entity.entity.getBukkitEntity())) {
                if (goal.getKey().getEntityClass().equals(Mob.class)) {
                    goals.add(goal.getKey().getNamespacedKey().getKey());
                }
            }
            return goals;
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name remove_mob_goal
        // @input ElementTag
        // @Plugin Paper
        // @description
        // Removes the specified goal from this entity.
        // Note that adding vanilla goals back to an entity is currently unsupported.
        // @tags
        // <EntityTag.mob_goals>
        // -->
        if (mechanism.matches("remove_mob_goal") && mechanism.hasValue()) {
            Bukkit.getMobGoals().removeGoal((Mob) entity.getBukkitEntity(), GoalKey.of(Mob.class, NamespacedKey.minecraft(mechanism.getValue().asString())));
        }
    }
}
