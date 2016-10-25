package net.aufdemrand.denizen.nms.enums;

import net.aufdemrand.denizencore.utilities.CoreUtilities;

public enum EntityAttribute {
    GENERIC_MAX_HEALTH("generic.maxHealth"),
    GENERIC_FOLLOW_RANGE("generic.followRange"),
    GENERIC_KNOCKBACK_RESISTANCE("generic.knockbackResistance"),
    GENERIC_MOVEMENT_SPEED("generic.movementSpeed"),
    GENERIC_ATTACK_DAMAGE("generic.attackDamage"),
    GENERIC_ATTACK_SPEED("generic.attackSpeed"),
    GENERIC_ARMOR("generic.armor"),
    GENERIC_LUCK("generic.luck"),
    HORSE_JUMP_STRENGTH("horse.jumpStrength"),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawnReinforcements");

    private String name;

    EntityAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EntityAttribute getByName(String name) {
        if (name == null) {
            return null;
        }
        name = CoreUtilities.toLowerCase(name);
        for (EntityAttribute entityAttribute : values()) {
            if (name.equals(CoreUtilities.toLowerCase(entityAttribute.name))) {
                return entityAttribute;
            }
        }
        return null;
    }
}
