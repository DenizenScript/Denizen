package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.abstracts.AnimationHelper;
import net.aufdemrand.denizen.nms.interfaces.EntityAnimation;
import org.bukkit.entity.Entity;

public class AnimationHelper_v1_8_R3 extends AnimationHelper {

    public AnimationHelper_v1_8_R3() {
        register("SKELETON_START_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                // Not available in 1.8
            }
        });
        register("SKELETON_STOP_SWING_ARM", new EntityAnimation() {
            @Override
            public void play(Entity entity) {
                // Not available in 1.8
            }
        });
    }
}
