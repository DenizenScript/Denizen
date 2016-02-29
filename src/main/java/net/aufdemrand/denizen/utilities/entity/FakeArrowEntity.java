package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_9_R1.EntityArrow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

public class FakeArrowEntity extends EntityArrow {

    public FakeArrowEntity(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.addEntity(this);
        bukkitEntity = new CraftFakeArrow((CraftServer) Bukkit.getServer(), this);
    }

    @Override
    public void t_() {
        // Do nothing
    }
}
