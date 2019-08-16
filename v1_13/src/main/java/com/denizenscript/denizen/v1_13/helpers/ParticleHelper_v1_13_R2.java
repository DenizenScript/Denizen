package com.denizenscript.denizen.v1_13.helpers;

import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.nms.interfaces.Particle;
import com.denizenscript.denizen.v1_13.impl.Particle_v1_13_R2;

public class ParticleHelper_v1_13_R2 extends ParticleHelper {

    @Override
    public void register(String name, Particle particle) {
        super.register(name, new Particle_v1_13_R2(particle.particle));
    }
}
