package com.denizenscript.denizen.nms.helpers;

import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import org.bukkit.Effect;

public class ParticleHelper_v1_12_R1 extends ParticleHelper {

    public ParticleHelper_v1_12_R1() {
        effectRemap.put("DRIP_WATER", Effect.WATERDRIP);
        effectRemap.put("DRIP_LAVA", Effect.LAVADRIP);
    }
}
