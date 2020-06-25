package com.denizenscript.denizen.nms.v1_16.impl;

import com.denizenscript.denizen.nms.interfaces.Particle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class ParticleImpl extends Particle {

    public ParticleImpl(org.bukkit.Particle particle) {
        super(particle);
    }

    @Override
    public <T> void playFor(Player player, Location location, int count, Vector offset, double extra, T data) {
        if (data instanceof MaterialData) {
            super.playFor(player, location, count, offset, extra, CraftBlockData.fromData(CraftMagicNumbers.getBlock((MaterialData) data)));
        }
        else {
            super.playFor(player, location, count, offset, extra, data);
        }
    }
}
