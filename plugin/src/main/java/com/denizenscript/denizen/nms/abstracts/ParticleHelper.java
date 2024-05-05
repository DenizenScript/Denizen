package com.denizenscript.denizen.nms.abstracts;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.Particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleHelper {

    public ParticleHelper() {
        for (org.bukkit.Particle particle : org.bukkit.Particle.values()) {
            register(particle.name(), new Particle(particle));
        }
        // TODO: 1.20.6: enum names are different now, can change to valueOf and let Spigot's runtime updating handle it
        register("SMOKE", new Particle(org.bukkit.Particle.SMOKE_NORMAL));
        register("HUGE_EXPLOSION", new Particle(org.bukkit.Particle.EXPLOSION_HUGE));
        register("LARGE_EXPLODE", new Particle(org.bukkit.Particle.EXPLOSION_LARGE));
        register("BUBBLE", new Particle(org.bukkit.Particle.WATER_BUBBLE));
        register("SUSPEND", new Particle(org.bukkit.Particle.SUSPENDED));
        register("DEPTH_SUSPEND", new Particle(org.bukkit.Particle.SUSPENDED_DEPTH));
        register("CRIT", new Particle(org.bukkit.Particle.CRIT));
        register("MAGIC_CRIT", new Particle(org.bukkit.Particle.CRIT_MAGIC));
        register("MOB_SPELL", new Particle(org.bukkit.Particle.SPELL_MOB));
        // TODO: 1.20.6: this particle type was removed in favor of entity_effect now having a color option
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            register("MOB_SPELL_AMBIENT", new Particle(org.bukkit.Particle.SPELL_MOB_AMBIENT));
        }
        register("INSTANT_SPELL", new Particle(org.bukkit.Particle.SPELL_INSTANT));
        register("WITCH_MAGIC", new Particle(org.bukkit.Particle.SPELL_WITCH));
        register("STEP_SOUND", new Particle(org.bukkit.Particle.HEART));
        register("EXPLODE", new Particle(org.bukkit.Particle.EXPLOSION_NORMAL));
        register("SPLASH", new Particle(org.bukkit.Particle.WATER_SPLASH));
        register("LARGE_SMOKE", new Particle(org.bukkit.Particle.SMOKE_LARGE));
        register("RED_DUST", new Particle(org.bukkit.Particle.REDSTONE));
        register("SNOWBALL_POOF", new Particle(org.bukkit.Particle.SNOWBALL));
        register("ANGRY_VILLAGER", new Particle(org.bukkit.Particle.VILLAGER_ANGRY));
        register("HAPPY_VILLAGER", new Particle(org.bukkit.Particle.VILLAGER_HAPPY));
    }

    public final Map<String, Particle> particles = new HashMap<>();
    public final List<Particle> visibleParticles = new ArrayList<>();

    public void register(String name, Particle particle) {
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
