package com.denizenscript.denizen.utilities.entity;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;

public class Gravity {

    // TODO once 1.20 is the minimum supported version can reference the enum directly
    public static final EntityType EXPERIENCE_BOTTLE_ENTITY_TYPE = Registry.ENTITY_TYPE.get(NamespacedKey.minecraft("experience_bottle"));

    public static double getGravity(EntityType entityType) {
        if (entityType == EXPERIENCE_BOTTLE_ENTITY_TYPE) {
            return 0.157;
        }
        switch (entityType) {
            case ARROW:
                return 0.118;
            case SNOWBALL:
                return 0.076;
            case EGG:
                return 0.074;
            default:
                return 0.115;
        }
    }
}
