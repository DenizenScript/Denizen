package com.denizenscript.denizen.nms.v1_13.helpers;

import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPolarBear;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class AnimationHelperImpl extends AnimationHelper {

    public AnimationHelperImpl() {
        register("SKELETON_START_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.SKELETON) {
                    ((CraftSkeleton) entity).getHandle().s(true);
                }
            }
        });
        register("SKELETON_STOP_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.SKELETON) {
                    ((CraftSkeleton) entity).getHandle().s(false);
                }
            }
        });
        register("POLAR_BEAR_START_STANDING", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.POLAR_BEAR) {
                    ((CraftPolarBear) entity).getHandle().s(true);
                }
            }
        });
        register("POLAR_BEAR_STOP_STANDING", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.POLAR_BEAR) {
                    ((CraftPolarBear) entity).getHandle().s(false);
                }
            }
        });
        register("HORSE_BUCK", entity -> {
            if (entity instanceof Horse) {
                ((CraftHorse) entity).getHandle().dZ();
            }
        });
    }
}
