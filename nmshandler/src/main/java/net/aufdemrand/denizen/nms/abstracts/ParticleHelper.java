package net.aufdemrand.denizen.nms.abstracts;

import net.aufdemrand.denizen.nms.interfaces.Effect;
import net.aufdemrand.denizen.nms.interfaces.Particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParticleHelper {

    private final Map<String, Effect> effects = new HashMap<String, Effect>();
    private final List<Effect> visualEffects = new ArrayList<Effect>();
    private final Map<String, Particle> particles = new HashMap<String, Particle>();
    private final List<Particle> visibleParticles = new ArrayList<Particle>();

    protected void register(String name, Effect effect) {
        effects.put(name.toUpperCase(), effect);
        if (effect.isVisual()) {
            visualEffects.add(effect);
        }
    }

    protected void register(String name, Particle particle) {
        particles.put(name.toUpperCase(), particle);
        if (particle.isVisible()) {
            visibleParticles.add(particle);
        }
    }

    public boolean hasEffect(String name) {
        return effects.containsKey(name.toUpperCase());
    }

    public boolean hasParticle(String name) {
        return particles.containsKey(name.toUpperCase());
    }

    public Effect getEffect(String name) {
        return effects.get(name.toUpperCase());
    }

    public Particle getParticle(String name) {
        return particles.get(name.toUpperCase());
    }

    public List<Particle> getVisibleParticles() {
        return visibleParticles;
    }

    public List<Effect> getVisualEffects() {
        return visualEffects;
    }
}
