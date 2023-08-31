package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.utilities.packets.HideParticles;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_20_R1.CraftParticle;

import java.util.HashSet;

public class HideParticlesPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean processParticlesForPacket(Packet<?> packet) {
        if (HideParticles.hidden.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof ClientboundLevelParticlesPacket) {
                HashSet<Particle> hidden = HideParticles.hidden.get(player.getUUID());
                if (hidden == null) {
                    return false;
                }
                ParticleOptions particle = ((ClientboundLevelParticlesPacket) packet).getParticle();
                Particle bukkitParticle = CraftParticle.toBukkit(particle);
                if (hidden.contains(bukkitParticle)) {
                    return true;
                }
                return false;
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return false;
    }
}
