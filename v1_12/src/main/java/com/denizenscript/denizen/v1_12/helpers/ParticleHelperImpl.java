package com.denizenscript.denizen.v1_12.helpers;

import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import org.bukkit.Effect;

public class ParticleHelperImpl extends ParticleHelper {

    public ParticleHelperImpl() {
        effectRemap.put("DRIP_WATER", Effect.WATERDRIP);
        effectRemap.put("DRIP_LAVA", Effect.LAVADRIP);
    }
}
