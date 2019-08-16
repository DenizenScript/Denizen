package com.denizenscript.denizen.nms.v1_13.helpers;

import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.nms.interfaces.Particle;
import com.denizenscript.denizen.nms.v1_13.impl.ParticleImpl;

public class ParticleHelperImpl extends ParticleHelper {

    @Override
    public void register(String name, Particle particle) {
        super.register(name, new ParticleImpl(particle.particle));
    }
}
