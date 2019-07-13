package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Particle {

    public org.bukkit.Particle particle;

    public Particle(org.bukkit.Particle particle) {
        this.particle = particle;
    }

    public void playFor(Player player, Location location, int count, Vector offset, double extra) {
        player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra);
    }

    public <T> void playFor(Player player, Location location, int count, Vector offset, double extra, T data) {
        player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data);
    }

    public boolean isVisible() {
        return particle != org.bukkit.Particle.SUSPENDED && particle != org.bukkit.Particle.SUSPENDED_DEPTH
                && particle != org.bukkit.Particle.WATER_BUBBLE;
    }

    public String getName() {
        return particle.name();
    }

    public Class neededData() {
        Class clazz = particle.getDataType();
        if (clazz == Void.class) {
            return null;
        }
        return clazz;
    }
}
