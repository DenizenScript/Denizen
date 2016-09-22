package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.abstracts.ParticleHelper;
import net.aufdemrand.denizen.nms.impl.effects.Effect_v1_8_R3;
import net.aufdemrand.denizen.nms.impl.effects.Particle_v1_8_R3;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Effect;

public class ParticleHelper_v1_8_R3 extends ParticleHelper {

    public ParticleHelper_v1_8_R3() {
        for (EnumParticle enumParticle : EnumParticle.values()) {
            register(enumParticle.name(), new Particle_v1_8_R3(enumParticle));
        }
        for (Effect effect : Effect.values()) {
            register(effect.name(), new Effect_v1_8_R3(effect));
        }
        register("DRIP_WATER", new Effect_v1_8_R3(Effect.WATERDRIP));
        register("DRIP_LAVA", new Effect_v1_8_R3(Effect.LAVADRIP));
        register("SMOKE", new Particle_v1_8_R3(EnumParticle.SMOKE_NORMAL));
        register("HUGE_EXPLOSION", new Particle_v1_8_R3(EnumParticle.EXPLOSION_HUGE));
        register("LARGE_EXPLODE", new Particle_v1_8_R3(EnumParticle.EXPLOSION_LARGE));
        register("BUBBLE", new Particle_v1_8_R3(EnumParticle.WATER_BUBBLE));
        register("SUSPEND", new Particle_v1_8_R3(EnumParticle.SUSPENDED));
        register("DEPTH_SUSPEND", new Particle_v1_8_R3(EnumParticle.SUSPENDED_DEPTH));
        register("CRIT", new Particle_v1_8_R3(EnumParticle.CRIT));
        register("MAGIC_CRIT", new Particle_v1_8_R3(EnumParticle.CRIT_MAGIC));
        register("MOB_SPELL", new Particle_v1_8_R3(EnumParticle.SPELL_MOB));
        register("MOB_SPELL_AMBIENT", new Particle_v1_8_R3(EnumParticle.SPELL_MOB_AMBIENT));
        register("INSTANT_SPELL", new Particle_v1_8_R3(EnumParticle.SPELL_INSTANT));
        register("WITCH_MAGIC", new Particle_v1_8_R3(EnumParticle.SPELL_WITCH));
        register("STEP_SOUND", new Particle_v1_8_R3(EnumParticle.HEART));
        register("EXPLODE", new Particle_v1_8_R3(EnumParticle.EXPLOSION_NORMAL));
        register("SPLASH", new Particle_v1_8_R3(EnumParticle.WATER_SPLASH));
        register("LARGE_SMOKE", new Particle_v1_8_R3(EnumParticle.SMOKE_LARGE));
        register("RED_DUST", new Particle_v1_8_R3(EnumParticle.REDSTONE));
        register("SNOWBALL_POOF", new Particle_v1_8_R3(EnumParticle.SNOWBALL));
        register("ANGRY_VILLAGER", new Particle_v1_8_R3(EnumParticle.VILLAGER_ANGRY));
        register("HAPPY_VILLAGER", new Particle_v1_8_R3(EnumParticle.VILLAGER_HAPPY));
    }
}
