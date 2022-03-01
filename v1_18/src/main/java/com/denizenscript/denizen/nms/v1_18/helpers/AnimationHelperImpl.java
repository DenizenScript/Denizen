package com.denizenscript.denizen.nms.v1_18.helpers;

import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPolarBear;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;

public class AnimationHelperImpl extends AnimationHelper {

    public AnimationHelperImpl() {
        register("SKELETON_START_SWING_ARM", entity -> {
            if (entity.getType() == EntityType.SKELETON) {
                ((CraftSkeleton) entity).getHandle().setAggressive(true);
            }
        });
        register("SKELETON_STOP_SWING_ARM", entity -> {
            if (entity.getType() == EntityType.SKELETON) {
                ((CraftSkeleton) entity).getHandle().setAggressive(false);
            }
        });
        register("POLAR_BEAR_START_STANDING", entity -> {
            if (entity.getType() == EntityType.POLAR_BEAR) {
                ((CraftPolarBear) entity).getHandle().setStanding(true);
            }
        });
        register("POLAR_BEAR_STOP_STANDING", entity -> {
            if (entity.getType() == EntityType.POLAR_BEAR) {
                ((CraftPolarBear) entity).getHandle().setStanding(false);
            }
        });
        register("HORSE_START_STANDING", entity -> {
            if (entity instanceof Horse) {
                ((CraftHorse) entity).getHandle().setStanding(true);
            }
        });
        register("HORSE_STOP_STANDING", entity -> {
            if (entity instanceof Horse) {
                ((CraftHorse) entity).getHandle().setStanding(false);
            }
        });
        register("HORSE_BUCK", entity -> {
            if (entity instanceof Horse) {
                ((CraftHorse) entity).getHandle().makeMad();
            }
        });
        register("IRON_GOLEM_ATTACK", entity -> {
            if (entity instanceof IronGolem) {
                Entity nmsEntity = ((CraftEntity) entity).getHandle();
                nmsEntity.level.broadcastEntityEvent(nmsEntity, (byte) 4);
            }
        });
    }
}
