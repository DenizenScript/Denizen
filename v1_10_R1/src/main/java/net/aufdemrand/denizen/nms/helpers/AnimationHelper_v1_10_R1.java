package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.abstracts.AnimationHelper;
import net.aufdemrand.denizen.nms.interfaces.EntityAnimation;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPolarBear;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class AnimationHelper_v1_10_R1 extends AnimationHelper {

    public AnimationHelper_v1_10_R1() {
        register("SKELETON_START_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.SKELETON) {
                    ((CraftSkeleton) entity).getHandle().a(true);
                }
            }
        });
        register("SKELETON_STOP_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                if (entity.getType() == EntityType.SKELETON) {
                    ((CraftSkeleton) entity).getHandle().a(false);
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
