package com.denizenscript.denizen.v1_12.helpers;

import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPolarBear;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class AnimationHelperImpl extends AnimationHelper {

    public AnimationHelperImpl() {
        register("SKELETON_START_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.SKELETON) {
                    ((CraftSkeleton) entity).getHandle().p(true);
                }
            }
        });
        register("SKELETON_STOP_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.SKELETON) {
                    ((CraftSkeleton) entity).getHandle().p(false);
                }
            }
        });
        register("POLAR_BEAR_START_STANDING", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.POLAR_BEAR) {
                    ((CraftPolarBear) entity).getHandle().p(true);
                }
            }
        });
        register("POLAR_BEAR_STOP_STANDING", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.POLAR_BEAR) {
                    ((CraftPolarBear) entity).getHandle().p(false);
                }
            }
        });
    }
}
