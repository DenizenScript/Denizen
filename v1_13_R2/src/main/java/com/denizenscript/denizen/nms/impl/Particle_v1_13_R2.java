package com.denizenscript.denizen.nms.impl;

import com.denizenscript.denizen.nms.interfaces.Particle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Particle_v1_13_R2 extends Particle {

    public Particle_v1_13_R2(org.bukkit.Particle particle) {
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
