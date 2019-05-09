package net.aufdemrand.denizen.nms.abstracts;

import net.aufdemrand.denizen.nms.interfaces.EntityAnimation;

import java.util.HashMap;
import java.util.Map;

public abstract class AnimationHelper {

    private final Map<String, EntityAnimation> entityAnimations = new HashMap<>();

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
