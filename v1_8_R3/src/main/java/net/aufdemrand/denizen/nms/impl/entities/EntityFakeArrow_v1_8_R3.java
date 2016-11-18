package net.aufdemrand.denizen.nms.impl.entities;

import net.minecraft.server.v1_8_R3.EntityArrow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class EntityFakeArrow_v1_8_R3 extends EntityArrow {

    public EntityFakeArrow_v1_8_R3(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        bukkitEntity = new CraftFakeArrow_v1_8_R3((CraftServer) Bukkit.getServer(), this);
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.addEntity(this);
    }

    @Override
    public void t_() {
        // Do nothing
    }

    @Override
    public CraftFakeArrow_v1_8_R3 getBukkitEntity() {
        return (CraftFakeArrow_v1_8_R3) bukkitEntity;
    }
}
