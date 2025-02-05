package com.denizenscript.denizen.utilities;


import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;

public class LegacyParticleNaming {

    public static final Map<String, Particle> legacyParticleNames = new HashMap<>();

    public static void registerLegacyName(String name, String particle) {
        legacyParticleNames.put(name, Particle.valueOf(particle));
    }
    
    static {
        registerLegacyName("SMOKE", "SMOKE_NORMAL");
        registerLegacyName("HUGE_EXPLOSION", "EXPLOSION_HUGE");
        registerLegacyName("LARGE_EXPLODE", "EXPLOSION_LARGE");
        registerLegacyName("BUBBLE", "WATER_BUBBLE");
        registerLegacyName("SUSPEND", "SUSPENDED");
        registerLegacyName("DEPTH_SUSPEND", "SUSPENDED_DEPTH");
        registerLegacyName("CRIT", "CRIT");
        registerLegacyName("MAGIC_CRIT", "CRIT_MAGIC");
        registerLegacyName("MOB_SPELL", "SPELL_MOB");
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            registerLegacyName("MOB_SPELL_AMBIENT", "SPELL_MOB_AMBIENT");
        }
        registerLegacyName("INSTANT_SPELL", "SPELL_INSTANT");
        registerLegacyName("WITCH_MAGIC", "SPELL_WITCH");
        registerLegacyName("EXPLODE", "EXPLOSION_NORMAL");
        registerLegacyName("SPLASH", "WATER_SPLASH");
        registerLegacyName("LARGE_SMOKE", "SMOKE_LARGE");
        registerLegacyName("RED_DUST", "REDSTONE");
        registerLegacyName("SNOWBALL_POOF", "SNOWBALL");
        registerLegacyName("ANGRY_VILLAGER", "VILLAGER_ANGRY");
        registerLegacyName("HAPPY_VILLAGER", "VILLAGER_HAPPY");
    }
}
