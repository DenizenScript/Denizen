package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.utilities.packets.HideParticles;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_20_R1.CraftParticle;

import java.util.HashSet;

public class HideParticlesPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLevelParticlesPacket.class, HideParticlesPacketHandlers::processParticlesForPacket);
    }

    public static Packet<ClientGamePacketListener> processParticlesForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (HideParticles.hidden.isEmpty()) {
            return packet;
        }
        try {
            if (packet instanceof ClientboundLevelParticlesPacket) {
                HashSet<Particle> hidden = HideParticles.hidden.get(networkManager.player.getUUID());
                if (hidden == null) {
                    return packet;
                }
                ParticleOptions particle = ((ClientboundLevelParticlesPacket) packet).getParticle();
                Particle bukkitParticle = CraftParticle.toBukkit(particle);
                if (hidden.contains(bukkitParticle)) {
                    return null;
                }
                return packet;
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return packet;
    }
}
