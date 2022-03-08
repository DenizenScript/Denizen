package com.denizenscript.denizen.nms.abstracts;

import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Map;

public abstract class AnimationHelper {

    public static final Map<String, EntityAnimation> entityAnimations = new HashMap<>();

    static {
        entityAnimations.put("VILLAGER_SHAKE_HEAD", entity -> {
            if (entity instanceof Villager) {
                ((Villager) entity).shakeHead();
            }
        });
    }

    protected void register(String name, EntityAnimation animation) {
        entityAnimations.put(name.toUpperCase(), animation);
    }

    public boolean hasEntityAnimation(String name) {
        return entityAnimations.containsKey(name.toUpperCase());
    }

    public EntityAnimation getEntityAnimation(String name) {
        return entityAnimations.get(name.toUpperCase());
    }
}
