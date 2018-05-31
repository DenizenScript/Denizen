package net.aufdemrand.denizen.nms.impl.effects;

import net.aufdemrand.denizen.nms.helpers.PacketHelper_v1_8_R3;
import net.aufdemrand.denizen.nms.interfaces.Particle;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Particle_v1_8_R3 implements Particle {

    private EnumParticle particle;

    public Particle_v1_8_R3(EnumParticle particle) {
        this.particle = particle;
    }

    @Override
    public void playFor(Player player, Location location, int count, Vector offset, double extra) {
        PacketHelper_v1_8_R3.sendPacket(player, new PacketPlayOutWorldParticles(particle, true,
                (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                (float) offset.getX(), (float) offset.getY(), (float) offset.getZ(), (float) extra, count));
    }

    @Override
    public <T> void playFor(Player player, Location location, int count, Vector offset, double extra, T data) {
        int[] dataArray;
        if (data instanceof ItemStack) {
            dataArray = new int[]{((ItemStack) data).getType().getId(), ((ItemStack) data).getDurability()};
        }
        else if (data instanceof MaterialData) {
            dataArray = new int[]{((MaterialData) data).getItemTypeId() + (((MaterialData) data).getData() << 12)};
        }
        PacketHelper_v1_8_R3.sendPacket(player, new PacketPlayOutWorldParticles(particle, true,
                (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                (float) offset.getX(), (float) offset.getY(), (float) offset.getZ(), (float) extra, count));
    }

    @Override
    public boolean isVisible() {
        return particle != EnumParticle.SUSPENDED && particle != EnumParticle.SUSPENDED_DEPTH
                && particle != EnumParticle.WATER_BUBBLE;
    }

    @Override
    public String getName() {
        return particle.name();
    }
}
