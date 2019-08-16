package com.denizenscript.denizen.v1_14.helpers;

import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.v1_14.impl.Particle_v1_14_R1;
import com.denizenscript.denizen.nms.interfaces.Particle;

public class ParticleHelper_v1_14_R1 extends ParticleHelper {

    @Override
    public void register(String name, Particle particle) {
        super.register(name, new Particle_v1_14_R1(particle.particle));
    }
}
