package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import net.minecraft.server.v1_17_R1.Entity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPolarBear;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftSkeleton;
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
                ((CraftPolarBear) entity).getHandle().t(true);
            }
        });
        register("POLAR_BEAR_STOP_STANDING", entity -> {
            if (entity.getType() == EntityType.POLAR_BEAR) {
                ((CraftPolarBear) entity).getHandle().t(false);
            }
        });
        register("HORSE_BUCK", entity -> {
            if (entity instanceof Horse) {
                ((CraftHorse) entity).getHandle().fm();
            }
        });
        register("IRON_GOLEM_ATTACK", entity -> {
            if (entity instanceof IronGolem) {
                Entity nmsEntity = ((CraftEntity) entity).getHandle();
                nmsEntity.world.broadcastEntityEffect(nmsEntity, (byte) 4);
            }
        });
    }
}
