package net.aufdemrand.denizen.nms.abstracts;

import net.aufdemrand.denizen.nms.interfaces.Particle;
import org.bukkit.Effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParticleHelper {

    public HashMap<String, Effect> effectRemap = new HashMap<>();
    private final Map<String, Particle> particles = new HashMap<>();
    private final List<Particle> visibleParticles = new ArrayList<>();

    protected void register(String name, Particle particle) {
        particles.put(name.toUpperCase(), particle);
        if (particle.isVisible()) {
            visibleParticles.add(particle);
        }
    }

    public boolean hasParticle(String name) {
        return particles.containsKey(name.toUpperCase());
    }

    public Particle getParticle(String name) {
        return particles.get(name.toUpperCase());
    }

    public List<Particle> getVisibleParticles() {
        return visibleParticles;
    }
}
