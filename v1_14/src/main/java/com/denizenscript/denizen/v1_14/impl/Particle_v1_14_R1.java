package com.denizenscript.denizen.v1_14.impl;

import com.denizenscript.denizen.nms.interfaces.Particle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Particle_v1_14_R1 extends Particle {

    public Particle_v1_14_R1(org.bukkit.Particle particle) {
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
