package com.denizenscript.denizen.nms.v1_21.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_21.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.utilities.packets.HideParticles;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_21_R1.CraftParticle;

import java.util.Set;

public class HideParticlesPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLevelParticlesPacket.class, HideParticlesPacketHandlers::processParticlesPacket);
    }

    public static ClientboundLevelParticlesPacket processParticlesPacket(DenizenNetworkManagerImpl networkManager, ClientboundLevelParticlesPacket particlesPacket) {
        if (HideParticles.hidden.isEmpty()) {
            return particlesPacket;
        }
        Set<Particle> hidden = HideParticles.hidden.get(networkManager.player.getUUID());
        if (hidden == null) {
            return particlesPacket;
        }
        Particle bukkitParticle = CraftParticle.minecraftToBukkit(particlesPacket.getParticle().getType());
        if (hidden.contains(bukkitParticle)) {
            return null;
        }
        return particlesPacket;
    }
}
