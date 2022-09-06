package com.denizenscript.denizen.nms.abstracts;

import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Map;

public abstract class AnimationHelper {

    public static final Map<String, EntityAnimation> entityAnimations = new HashMap<>();

    static {
        entityAnimations.put("villager_shake_head", entity -> {
            if (entity instanceof Villager) {
                ((Villager) entity).shakeHead();
            }
        });
    }

    protected void register(String name, EntityAnimation animation) {
        entityAnimations.put(CoreUtilities.toLowerCase(name), animation);
    }

    public boolean hasEntityAnimation(String name) {
        return entityAnimations.containsKey(CoreUtilities.toLowerCase(name));
    }

    public EntityAnimation getEntityAnimation(String name) {
        return entityAnimations.get(CoreUtilities.toLowerCase(name));
    }
}
