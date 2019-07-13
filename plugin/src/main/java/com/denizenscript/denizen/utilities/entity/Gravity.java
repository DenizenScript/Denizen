package com.denizenscript.denizen.utilities.entity;

import org.bukkit.entity.EntityType;

public class Gravity {

    public static double getGravity(EntityType entityType) {
        switch (entityType) {
            case ARROW:
                return 0.118;
            case SNOWBALL:
                return 0.076;
            case THROWN_EXP_BOTTLE:
                return 0.157;
            case EGG:
                return 0.074;
            default:
                return 0.115;
        }
    }
}
